/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.test

import java.nio.ByteBuffer

/**
 * Hexadecimal encoding where each byte is represented by two hexadecimal digits.
 */
object HexEncoding {
    private val HEX_DIGITS = "0123456789abcdef".toCharArray()
    /**
     * Encodes the provided data as a hexadecimal string.
     */
    /**
     * Encodes the provided data as a hexadecimal string.
     */
    @JvmStatic
    fun encode(data: ByteArray, offset: Int = 0, length: Int = data.size): String {
        val result = StringBuilder(length * 2)
        for (i in 0 until length) {
            val b = data[offset + i]
            result.append(HEX_DIGITS[b.toInt() ushr 4 and 0x0f])
            result.append(HEX_DIGITS[b.toInt() and 0x0f])
        }
        return result.toString()
    }

    /**
     * Encodes the remaining bytes of the provided [ByteBuffer] as a hexadecimal string.
     */
    fun encodeRemaining(data: ByteBuffer): String {
        return encode(data.array(), data.arrayOffset() + data.position(), data.remaining())
    }
}