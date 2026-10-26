package com.example.campusrelay.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Tokens never enter preferences in plaintext. Biometric tokens require a per-use
 * Keystore operation and are available only in memory until the app backgrounds. */
class SessionVault(context: Context) {
    private val storage = context.getSharedPreferences("session_vault", Context.MODE_PRIVATE)
    private val keys = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    @Volatile private var unlocked: String? = null
    val protected: Boolean get() = storage.getBoolean("biometric", false)
    val hasSession: Boolean get() = storage.contains("ciphertext")

    private fun key(biometric: Boolean): SecretKey {
        val alias = if (biometric) "campus_session_biometric" else "campus_session"
        (keys.getKey(alias, null) as? SecretKey)?.let { return it }
        val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(biometric)
        if (biometric) spec.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            .setInvalidatedByBiometricEnrollment(true)
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply { init(spec.build()) }.generateKey()
    }
    fun enrollmentCipher(): Cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key(true)) }
    fun unlockCipher(): Cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
        init(Cipher.DECRYPT_MODE, key(true), GCMParameterSpec(128, Base64.decode(storage.getString("iv", null), Base64.NO_WRAP)))
    }
    @Synchronized fun enable(cipher: Cipher) {
        val token = token() ?: error("Sign in before enabling biometrics")
        persist(token, cipher, true)
    }
    @Synchronized fun unlock(cipher: Cipher) {
        unlocked = String(cipher.doFinal(Base64.decode(storage.getString("ciphertext", null), Base64.NO_WRAP)), Charsets.UTF_8)
    }
    @Synchronized fun token(): String? {
        unlocked?.let { return it }
        if (protected || !hasSession) return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(false), GCMParameterSpec(128, Base64.decode(storage.getString("iv", null), Base64.NO_WRAP)))
        }
        return String(cipher.doFinal(Base64.decode(storage.getString("ciphertext", null), Base64.NO_WRAP)), Charsets.UTF_8)
    }
    @Synchronized fun save(token: String) {
        check(!protected) { "Unlock and disable biometric protection before replacing a session" }
        persist(token, Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key(false)) }, false)
    }
    @Synchronized fun disable() {
        val token = token() ?: error("Unlock first")
        persist(token, Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key(false)) }, false)
    }
    private fun persist(token: String, cipher: Cipher, biometric: Boolean) {
        val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        check(storage.edit().putString("ciphertext", Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP)).putBoolean("biometric", biometric).commit())
        unlocked = token
    }
    fun lock() { unlocked = null }
    @Synchronized fun clear() { unlocked = null; storage.edit().clear().commit(); keys.deleteEntry("campus_session_biometric") }
}
