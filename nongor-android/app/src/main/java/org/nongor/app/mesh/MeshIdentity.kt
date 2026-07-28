package org.nongor.app.mesh

import android.content.Context
import org.nongor.app.core.Ed25519Signer
import java.security.SecureRandom
import java.util.Base64

/**
 * Loads this device's persistent Ed25519 mesh identity, generating one on first use. The private
 * key never leaves the device — only the public key (embedded in every envelope) is shared.
 */
object MeshIdentity {
    private const val PREFS = "mesh_identity"
    private const val KEY_SEED = "ed25519_seed"

    fun loadOrCreateSigner(context: Context, displayName: String): Ed25519Signer {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_SEED, null)
        val seed = if (existing != null) {
            Base64.getDecoder().decode(existing)
        } else {
            ByteArray(Ed25519Signer.SEED_BYTES).also { s ->
                SecureRandom().nextBytes(s)
                prefs.edit().putString(KEY_SEED, Base64.getEncoder().encodeToString(s)).apply()
            }
        }
        return Ed25519Signer(displayName, seed)
    }
}
