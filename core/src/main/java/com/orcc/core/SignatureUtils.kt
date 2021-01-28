package com.orcc.core

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.os.Build


object SignatureUtils {

    @JvmStatic
    fun getSignature(context: Context): String? {
        try {
            val signatures: Array<Signature>
            signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo: SigningInfo = packageInfo.signingInfo
                signingInfo.apkContentsSigners
            } else {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                packageInfo.signatures
            }
            val builder = StringBuilder()
            for (signature in signatures) {
                builder.append(signature.toCharsString())
            }
            return builder.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}