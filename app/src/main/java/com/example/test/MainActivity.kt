package com.example.test

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.occ.annotation.CoreDecryption
import com.occ.app.core.AppCore
import com.orcinus.orca.R
import dalvik.system.DexClassLoader
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


@Keep
class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application))
            .get(AndroidViewModel::class.java)
    }

    @CoreDecryption("base2")
    private var data2 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Monster.runJob(application,getApkPath(packageName))

        val tv = findViewById<View>(R.id.tv)


        tv.setOnClickListener {
            runCatching {
                Log.e("222222","base = ${AppCore.getBase()} base2 = ${data2} base3 = ${viewModel.getValue2()}")
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private fun getApkPath(packageName: String): String? {
        try {
            BufferedReader(FileReader("/proc/self/maps")).use { reader ->
                var line: String
                while (reader.readLine().also { line = it } != null) {
                    val arr =
                        line.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    val path = arr[arr.size - 1]
                    if (isApkPath(packageName, path)) {
                        return path
                    }
                }
                return null
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun isApkPath(packageName: String, path: String): Boolean {
        if (!path.startsWith("/") || !path.endsWith(".apk")) {
            return false
        }
        val splitStr = path.substring(1).split("/".toRegex(), limit = 6).toTypedArray()
        val splitCount = splitStr.size
        if (splitCount == 4 || splitCount == 5) {
            if (splitStr[0] == "data" && splitStr[1] == "app" && splitStr[splitCount - 1] == "base.apk") {
                return splitStr[splitCount - 2].startsWith(packageName)
            }
            if (splitStr[0] == "mnt" && splitStr[1] == "asec" && splitStr[splitCount - 1] == "pkg.apk") {
                return splitStr[splitCount - 2].startsWith(packageName)
            }
        } else if (splitCount == 3) {
            if (splitStr[0] == "data" && splitStr[1] == "app") {
                return splitStr[2].startsWith(packageName)
            }
        } else if (splitCount == 6) {
            if (splitStr[0] == "mnt" && splitStr[1] == "expand" && splitStr[3] == "app" && splitStr[5] == "base.apk") {
                return splitStr[4].endsWith(packageName)
            }
        }
        return false
    }

}