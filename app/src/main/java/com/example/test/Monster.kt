package com.example.test

import android.content.Context
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.File
import java.util.concurrent.Executors


object Monster {

    private val pool by lazy {
        Executors.newFixedThreadPool(1)
    }

    fun runJob(context: Context,apkPath:String?) {
        apkPath?:return
        pool.submit {
            val assetTarDir = File(context.filesDir, "monster")
            if (!assetTarDir.exists()) {
                assetTarDir.mkdirs()
            }
            val classJar = File(assetTarDir, "classes.dex")
            if (classJar.exists()) {
                classJar.delete()
            }
            val ots = classJar.outputStream().buffered()
            val ins = context.assets.open("classes.dex").buffered()
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
            try {
                val dexClassLoader = DexClassLoader(
                    classJar.absolutePath,
                    assetTarDir.absolutePath,
                    null,
                    ClassLoader.getSystemClassLoader()
                )
                val clazz = dexClassLoader.loadClass("com.android.apksigner.ApkSignerTool")
                Log.d("222222222","load $clazz")
                if (clazz != null) {
                    Log.d("222222222","load")
                    val method = clazz.getDeclaredMethod("verify",String::class.java,Boolean::class.java)
                    Log.d("222222222","find method $method")
                    val result = method.invoke(clazz,apkPath,true)
                    Log.d("222222222","find result $result")
                    val getmd5 = getMd5Signature(context.packageName,context.packageManager)
                    Log.d("222222222","find getmd5 $getmd5")
                }
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }

}