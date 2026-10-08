package com.izida.wallet.core.security

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

class AuthSession(context: Context) {
    private val prefs = context.getSharedPreferences("izida_auth", Context.MODE_PRIVATE)
    private var token: String? = null

    init { token = prefs.getString("token", null) }

    fun start(serverToken: String) {
        token = serverToken
        prefs.edit().putString("token", DeviceKeyStore.encrypt(serverToken)).apply()
    }

    fun getToken(): String? {
        val encrypted = prefs.getString("token", null) ?: return null
        return runCatching { DeviceKeyStore.decrypt(encrypted) }.getOrNull()
    }

    fun end() {
        token = null
        prefs.edit().remove("token").apply()
    }

    fun isActive(): Boolean = getToken() != null
}

object PinHasher {
    fun generateSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }
    fun hash(pin: CharArray, salt: ByteArray): ByteArray {
        val spec = javax.crypto.spec.PBEKeySpec(pin, salt, 120_000, 256)
        return try { javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }
}

object DeviceKeyStore {
    private const val ALIAS = "izida_device_key"
    fun getOrCreate(): SecretKey {
        val ks = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = ks.getKey(ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        return generator.generateKey()
    }
    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreate())
        return Base64.encodeToString(cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }
    fun decrypt(value: String): String {
        val data = Base64.decode(value, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreate(), javax.crypto.spec.GCMParameterSpec(128, data.copyOfRange(0, 12)))
        return String(cipher.doFinal(data.copyOfRange(12, data.size)), Charsets.UTF_8)
    }
}
