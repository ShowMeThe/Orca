package com.occ.orca.task

import java.io.File
import java.security.MessageDigest

class DexMd5Task(private val inputFileDirPath: String) {


    fun generate(): List<String> {
        println("DexMd5Task  inputFileDirPath = $inputFileDirPath")
        val inputFile = File(inputFileDirPath)
        val dexFile = inputFile.walkTopDown().toMutableList()
            .filter { it.extension.contains("dex") }

        if (dexFile.isEmpty()) {
            println("dex not found")
            return emptyList()
        }

        val dexMd5List = mutableListOf<String>()
        dexFile.onEach {
            val inputStream = it.inputStream().buffered()
            dexMd5List.add(getMD5FromStream(inputStream))
        }

        println("DexMd5Task dex md5 = $dexMd5List")

        return dexMd5List
    }


    private fun getMD5FromStream(input: java.io.InputStream): String {
        val md = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            md.update(buffer, 0, bytesRead)
        }
        input.close()
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}