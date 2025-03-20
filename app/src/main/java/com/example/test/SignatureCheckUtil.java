package com.example.test;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class SignatureCheckUtil {

    public static String getApkSignature(PackageManager pm, String packageName) {
        try {
            Signature[] signatures = new Signature[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                signatures = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.getApkContentsSigners();
            }
            if (signatures.length == 0) {
                return null;
            }
           /* MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(signatures[0].toByteArray());
            return Base64.encodeToString(md.digest(), Base64.NO_WRAP);*/
            return String.valueOf(signatures[0].hashCode());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }



    public static String getApkSignatureHash(Context context) {
        try {
            File apkFile = new File(context.getApplicationInfo().sourceDir);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            FileInputStream fis = new FileInputStream(apkFile);
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(fis);
            fis.close();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] certBytes = cert.getEncoded();
            byte[] digest = md.digest(certBytes);
            return new String(digest);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
