package com.example.test

import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun getMd5Signature(packageName: String, packageManager: PackageManager): String? {
    return try {
        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        val signatures = packageInfo.signatures
        val md5Digest = MessageDigest.getInstance("MD5")
        HexEncoding.encode(signatures.first().toByteArray().let { md5Digest.digest(it) })
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        null
    }
}
