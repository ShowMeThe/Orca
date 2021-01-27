package com.orcinus.orca

import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class AESEncryption private constructor() {

    companion object {
        private const val CHARSET = "UTF-8"
        private const val HASH_ALGORITHM = "MD5"
        private const val AES_MODE = "AES/CBC/PKCS5Padding"
        private var iv = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x10, 0x2F, 0x3F, 0x4A, 0x5F, 0x66, 0x78, 0x2d, 0x3c, 0x69)


        fun encrypt(key: String, message: String): String {
            var result: ByteArray? = null
            try {
                val keySpec = generateKeySpec(key)
                result = encrypt(keySpec, iv, message.toByteArray(charset(CHARSET)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return if (result == null) {
                ""
            } else Base64.getEncoder().encodeToString(result)
        }

        fun decrypt(key: String, cipherMessage: String?): String {
            var result = ""
            try {
                val keySpec = generateKeySpec(key)
                val cipherBytes = Base64.getDecoder().decode(cipherMessage)
                result = String(decrypt(keySpec, iv, cipherBytes), charset(CHARSET))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
        private fun generateKeySpec(key: String): SecretKeySpec {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            digest.update(key.toByteArray(charset(CHARSET)))
            val keyBytes = digest.digest()
            return SecretKeySpec(keyBytes, "AES")
        }

        @Throws(
            NoSuchPaddingException::class,
            NoSuchAlgorithmException::class,
            InvalidAlgorithmParameterException::class,
            InvalidKeyException::class,
            BadPaddingException::class,
            IllegalBlockSizeException::class
        )
        private fun encrypt(keySpec: SecretKeySpec, iv: ByteArray, message: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(AES_MODE)
            val ivParameterSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)
            return cipher.doFinal(message)
        }

        @Throws(
            NoSuchPaddingException::class,
            NoSuchAlgorithmException::class,
            InvalidAlgorithmParameterException::class,
            InvalidKeyException::class,
            BadPaddingException::class,
            IllegalBlockSizeException::class
        )
        private fun decrypt(
            keySpec: SecretKeySpec,
            iv: ByteArray,
            cipherMessage: ByteArray
        ): ByteArray {
            val cipher = Cipher.getInstance(AES_MODE)
            val ivParameterSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
            return cipher.doFinal(cipherMessage)
        }
    }

    init {
        throw IllegalAccessException()
    }
}