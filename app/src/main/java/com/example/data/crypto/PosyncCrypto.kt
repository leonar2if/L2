package com.example.data.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object PosyncCrypto {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"

    private fun deriveKey(bossUuid: String): SecretKeySpec {
        val input = bossUuid + DrmEngine.MASTER_KEY
        val md = MessageDigest.getInstance("SHA-256")
        val keyBytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptPayload(plainText: String, bossUuid: String): String {
        val keySpec = deriveKey(bossUuid)
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decryptPayload(encryptedBase64: String, bossUuid: String): String {
        val keySpec = deriveKey(bossUuid)
        val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
        if (combined.size < 16) throw IllegalArgumentException("Payload inválido o corrupto")

        val iv = ByteArray(16)
        System.arraycopy(combined, 0, iv, 0, 16)
        val ciphertext = ByteArray(combined.size - 16)
        System.arraycopy(combined, 16, ciphertext, 0, ciphertext.size)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
