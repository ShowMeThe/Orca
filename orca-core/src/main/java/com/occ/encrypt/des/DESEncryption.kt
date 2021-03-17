package com.occ.encrypt.des

import android.util.Base64
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec

class DESEncryption {

    companion object {

        private const val CHARSET = "UTF-8"
        private const val DES_MODE = "DES/CBC/PKCS5Padding"
        private val iv = byteArrayOf(
            0x4f,
            0x15,
            0x7f,
            0x30,
            0x22,
            0x3f,
            0x4d,
            0x1a
        )

        @JvmStatic
        fun encrypt(key: String, message: String): String? {
            val src = message.toByteArray(charset(CHARSET))
            val out = encrypt(key, src)
            return Base64.encodeToString(out,Base64.DEFAULT)
        }

        @JvmStatic
        fun decrypt(key: String, message: String): String? {
            val src = Base64.decode(message.toByteArray(charset(CHARSET)),Base64.DEFAULT)
            val out = decrypt(key, src)
            return String(out!!, charset(CHARSET))
        }

        private fun decrypt(key: String, src: ByteArray): ByteArray? {
            var result: ByteArray? = null
            try {
                val keys = key.toByteArray(charset(CHARSET))
                val iv = IvParameterSpec(iv)
                val desKey = DESKeySpec(keys)
                val factory = SecretKeyFactory.getInstance("DES")
                val secretKey = factory.generateSecret(desKey)
                val cipher = Cipher.getInstance(DES_MODE)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
                result = cipher.doFinal(src)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        private fun encrypt(key: String, src: ByteArray): ByteArray? {
            var result: ByteArray? = null
            try {
                val keys = key.toByteArray(charset(CHARSET))
                val iv = IvParameterSpec(iv)
                val desKey = DESKeySpec(keys)
                val factory = SecretKeyFactory.getInstance("DES")
                val secretKey = factory.generateSecret(desKey)
                val cipher = Cipher.getInstance(DES_MODE)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
                result = cipher.doFinal(src)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }


    }


}