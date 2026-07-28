package org.nongor.app.core

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer as BcEd25519Signer
import java.security.MessageDigest
import java.util.Base64

/**
 * Offline mesh: signed envelopes + multi-hop relay — Kotlin port of nongor/core/mesh.py
 * (inspired by MeshGemma). Signing is pluggable: [DevSigner] (SHA-256) is a non-cryptographic
 * stand-in used only by tests/CI; production uses [Ed25519Signer], a real asymmetric signature
 * that proves possession of a persistent private key — DevSigner's hash can be computed by
 * anyone who knows the claimed sender id, so it detects corruption but proves no identity.
 */

/** Deterministic canonical JSON (sorted keys, compact) so both sides hash identical bytes. */
object Canonical {
    fun of(value: Any?): String = when (value) {
        null -> "null"
        is String -> "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        is Boolean -> value.toString()
        is Int -> value.toString()
        is Long -> value.toString()
        is Double -> if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
        is Map<*, *> -> value.entries.sortedBy { it.key.toString() }
            .joinToString(",", "{", "}") { "\"${it.key}\":${of(it.value)}" }
        is List<*> -> value.joinToString(",", "[", "]") { of(it) }
        else -> "\"$value\""
    }
    fun bytes(value: Any?): ByteArray = of(value).toByteArray(Charsets.UTF_8)
}

interface Signer {
    val nodeId: String
    /** Envelope verification scheme this signer produces — see [SignedEnvelope.verify]. */
    val scheme: String get() = "dev-sha256"
    /** Base64 Ed25519 public key, required (non-blank) when [scheme] == "ed25519". */
    val publicKeyB64: String get() = ""
    fun sign(data: ByteArray): String
}

/** Deterministic NON-cryptographic stand-in (SHA-256) for tests/CI. Not for production. */
class DevSigner(override val nodeId: String) : Signer {
    override fun sign(data: ByteArray): String = digest(nodeId, data)
    companion object {
        fun digest(nodeId: String, data: ByteArray): String {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(nodeId.toByteArray(Charsets.UTF_8)); md.update('|'.code.toByte()); md.update(data)
            return md.digest().joinToString("") { "%02x".format(it) }
        }
        fun verify(nodeId: String, data: ByteArray, sigHex: String): Boolean =
            digest(nodeId, data) == sigHex
    }
}

/**
 * Real asymmetric signing. The signature can only be produced by whoever holds the private key
 * matching [publicKeyB64], which is carried in the envelope so any receiver can verify it without
 * prior key exchange (self-certifying identity) — unlike [DevSigner], nobody can forge a message
 * "from" this node without that private key.
 */
class Ed25519Signer(override val nodeId: String, seed: ByteArray) : Signer {
    private val privateKey = Ed25519PrivateKeyParameters(seed, 0)
    override val scheme = "ed25519"
    override val publicKeyB64: String =
        Base64.getEncoder().encodeToString(privateKey.generatePublicKey().encoded)

    override fun sign(data: ByteArray): String {
        val signer = BcEd25519Signer()
        signer.init(true, privateKey)
        signer.update(data, 0, data.size)
        return Base64.getEncoder().encodeToString(signer.generateSignature())
    }

    companion object {
        const val SEED_BYTES = 32
    }
}

private object Ed25519Verify {
    fun verify(publicKeyB64: String, data: ByteArray, sigB64: String): Boolean {
        if (publicKeyB64.isBlank() || sigB64.isBlank()) return false
        return try {
            val pub = Ed25519PublicKeyParameters(Base64.getDecoder().decode(publicKeyB64), 0)
            val verifier = BcEd25519Signer()
            verifier.init(false, pub)
            verifier.update(data, 0, data.size)
            verifier.verifySignature(Base64.getDecoder().decode(sigB64))
        } catch (_: Exception) {
            false
        }
    }
}

data class SignedEnvelope(
    val sender: String,
    val msgId: String,
    val lamport: Int,
    val payload: Map<String, Any?>,
    var sig: String = "",
    val type: String = "sos",
    val version: Int = 1,
    var ttl: Int = 4,        // relay metadata — NOT part of the signed content
    val scheme: String = "dev-sha256",
    val senderKey: String = "",   // base64 Ed25519 public key, when scheme == "ed25519"
) {
    fun signedContent(): Map<String, Any?> = mapOf(
        "version" to version, "sender" to sender, "msg_id" to msgId,
        "lamport" to lamport, "type" to type, "payload" to payload,
        "scheme" to scheme, "sender_key" to senderKey,
    )

    fun verify(): Boolean = when (scheme) {
        "ed25519" -> Ed25519Verify.verify(senderKey, Canonical.bytes(signedContent()), sig)
        else -> DevSigner.verify(sender, Canonical.bytes(signedContent()), sig)
    }

    /**
     * Whether a live mesh receiver should trust this envelope. Only "ed25519" (real asymmetric
     * signing) counts — "dev-sha256" is a testable stand-in with no secret key, so anyone can
     * compute a valid-looking signature for it just by knowing the sender id, no key required.
     * Trusting [verify] alone in production would let an attacker forge a "verified" envelope
     * simply by claiming that scheme on the wire (a downgrade attack).
     */
    fun isProductionTrusted(): Boolean = scheme == "ed25519" && verify()

    companion object {
        fun create(
            signer: Signer, payload: Map<String, Any?>, msgId: String, lamport: Int,
            type: String = "sos", ttl: Int = 4,
        ): SignedEnvelope {
            val env = SignedEnvelope(
                signer.nodeId, msgId, lamport, payload, "", type, 1, ttl,
                scheme = signer.scheme, senderKey = signer.publicKeyB64,
            )
            env.sig = signer.sign(Canonical.bytes(env.signedContent()))
            return env
        }
    }
}

/** A mesh node: Lamport clock + dedup + multi-hop flooding. */
class MeshNode(val nodeId: String) {
    var clock: Int = 0
    val seen: MutableSet<String> = mutableSetOf()
    val inbox: MutableList<Map<String, Any?>> = mutableListOf()

    /** Returns an envelope to re-broadcast (ttl-1), or null (dropped/duplicate/invalid). */
    fun receive(env: SignedEnvelope): SignedEnvelope? {
        if (!env.verify()) return null                       // tamper/forgery -> drop
        if (env.msgId in seen) return null                   // duplicate -> drop
        seen.add(env.msgId)
        clock = maxOf(clock, env.lamport) + 1                // Lamport update
        inbox.add(env.payload)
        if (env.ttl > 0) return env.copy(ttl = env.ttl - 1)  // sig still valid (ttl not signed)
        return null
    }
}
