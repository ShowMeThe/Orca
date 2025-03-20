package com.occ.encrypt.md5

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Keep
class FileIO{

    companion object{

        @JvmStatic
        fun getMD5FromStream(file: File): String {
            val input = file.inputStream().buffered()
            val md = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
            input.close()
            val value = md.digest().joinToString("") { "%02x".format(it) }
            return value
        }


        @JvmStatic
        fun getApk(context: Context):Array<File>{
            val files = java.util.ArrayList<File>()
            val packageName = context.packageName
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            val targetApkPath = appInfo.sourceDir
            val cacheDexDir = File(context.cacheDir,"temp/dex")
            if(cacheDexDir.listFiles()?.isNotEmpty() == true){
                cacheDexDir.deleteRecursively()
            }
            if(!cacheDexDir.exists()){
                cacheDexDir.mkdirs()
            }
            try {
                val apkFile = File(targetApkPath)
                val zipInputStream = ZipInputStream(FileInputStream(apkFile))
                var entry: ZipEntry?

                while (zipInputStream.nextEntry.also { entry = it } != null) {
                    if ((entry!!.name.startsWith("classes") && entry!!.name.endsWith(".dex"))) {
                        val outputFile = File(cacheDexDir, entry!!.name)
                        FileOutputStream(outputFile).use { fos ->
                            zipInputStream.copyTo(fos)
                        }
                    }
                }
                zipInputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cacheDexDir.listFiles()?.onEach {
                files.add(it)
                it.deleteOnExit()
            }
         return files.toTypedArray()
        }

    }

}





