package com.occ.orca

import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and

object StringUtils {


    @JvmStatic
    fun md5(string: String): String {
        val md5: MessageDigest?
        try {
            md5 = MessageDigest.getInstance("MD5")
        } catch (e: Exception) {
            println(e.toString())
            e.printStackTrace()
            return ""
        }

        val charArray = string.toCharArray()
        val byteArray = ByteArray(charArray.size)

        for (i in charArray.indices)
            byteArray[i] = charArray[i].toByte()
        val md5Bytes = md5!!.digest(byteArray)
        val hexValue = StringBuffer()
        for (i in md5Bytes.indices) {
            val value = md5Bytes[i].toInt() and 0xff
            if (value < 16)
                hexValue.append("0")
            hexValue.append(Integer.toHexString(value))
        }
        return hexValue.toString()
    }

    @JvmStatic
    fun substring(str: String):String =  str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(
        1
    )

}