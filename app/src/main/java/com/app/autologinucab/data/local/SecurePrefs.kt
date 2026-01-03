package com.app.autologinucab.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurePrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCredentials(username: String, password: String, url: String) {
        prefs.edit {
            putString(KEY_USERNAME, encrypt(username))
            putString(KEY_PASSWORD, encrypt(password))
            putString(KEY_URL, encrypt(url))
        }
    }

    fun readCredentials(): Credentials? {
        val uEnc = prefs.getString(KEY_USERNAME, null) ?: return null
        val pEnc = prefs.getString(KEY_PASSWORD, null) ?: return null
        val urlEnc = prefs.getString(KEY_URL, null) ?: return null

        return Credentials(
            username = decrypt(uEnc),
            password = decrypt(pEnc),
            url = decrypt(urlEnc)
        )
    }

    fun clearCredentials() {
        prefs.edit {
            remove(KEY_USERNAME)
            remove(KEY_PASSWORD)
            remove(KEY_URL)
        }
    }

    fun hasCredentials(): Boolean {
        return prefs.contains(KEY_USERNAME) || prefs.contains(KEY_PASSWORD) || prefs.contains(KEY_URL)
    }

    data class Credentials(
        val username: String,
        val password: String,
        val url: String
    )

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        // Persistimos IV + ciphertext (Base64)
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        require(combined.size > IV_SIZE_BYTES) { "Payload inválido" }

        val iv = combined.copyOfRange(0, IV_SIZE_BYTES)
        val ciphertext = combined.copyOfRange(IV_SIZE_BYTES, combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(TAG_SIZE_BITS, iv)
        )

        val plain = cipher.doFinal(ciphertext)
        return String(plain, StandardCharsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM, ANDROID_KEYSTORE)
        val spec = android.security.keystore.KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_URL = "url"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "autologinucab_aes_key"

        private const val KEY_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        // AES-GCM estándar
        private const val IV_SIZE_BYTES = 12
        private const val TAG_SIZE_BITS = 128
    }
}
