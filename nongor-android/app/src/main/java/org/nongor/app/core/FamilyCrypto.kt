package org.nongor.app.core

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Family Reunion identity. Everything a phone puts on the air is derived from a shared family code
 * that only the family knows:
 *
 *  - [tag]  = a one-way hash of the code. Two phones can tell "same family" without ever revealing
 *             the code, and a stranger sniffing the mesh just sees an opaque tag.
 *  - [seal] = the member's name encrypted (AES-GCM) with a key derived from the same code, so only
 *             family members can read who was seen. A stranger gets ciphertext.
 *
 * This keeps the broadcast useful to the family and meaningless to everyone else.
 */
object FamilyCrypto {

    private const val GCM_TAG_BITS = 128
    private const val IV_BYTES = 12

    private fun normalise(code: String) = code.trim().lowercase()

    private fun sha256(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(bytes)

    /** Public, non-reversible family identifier that goes on the air. */
    fun tag(code: String): String =
        sha256(normalise(code).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }.take(16)

    private fun key(code: String) =
        SecretKeySpec(sha256(("nongor-family:" + normalise(code)).toByteArray(Charsets.UTF_8)), "AES")

    /** Encrypt the member name for the family. Returns base64(iv || ciphertext). */
    fun seal(code: String, plain: String): String {
        val iv = ByteArray(IV_BYTES).also { SecureRandom().nextBytes(it) }
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, key(code), GCMParameterSpec(GCM_TAG_BITS, iv))
        val ct = c.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + ct)
    }

    /** Decrypt a sealed name. Returns null if this phone isn't in that family (wrong key). */
    fun open(code: String, sealed: String): String? = runCatching {
        val all = Base64.getDecoder().decode(sealed)
        if (all.size <= IV_BYTES) return null
        val iv = all.copyOfRange(0, IV_BYTES)
        val ct = all.copyOfRange(IV_BYTES, all.size)
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, key(code), GCMParameterSpec(GCM_TAG_BITS, iv))
        String(c.doFinal(ct), Charsets.UTF_8)
    }.getOrNull()
}
