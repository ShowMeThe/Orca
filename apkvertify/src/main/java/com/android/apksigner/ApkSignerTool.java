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

package com.android.apksigner;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

import com.android.apksig.ApkVerifier;

/**
 * Command-line tool for signing APKs and for checking whether an APK's signature are expected to
 * verify on Android devices.
 */
public class ApkSignerTool {

    private static final int VERSION_CODE = 6;
    private static final String VERSION_NAME = "2.0";
    private static final String HELP_PAGE_GENERAL = "help.txt";
    private static final String VERSION_PAGE_GENERAL = "help_version.txt";


    public static final String KEY_RESULT_RET = "ret";
    public static final String KEY_RESULT_MSG = "msg";
    public static final String KEY_RESULT_IS_V3 = "isV3";
    public static final String KEY_RESULT_IS_V3_OK = "isV3OK";
    public static final String KEY_RESULT_IS_V2 = "isV2";
    public static final String KEY_RESULT_IS_V2_OK = "isV2OK";
    public static final String KEY_RESULT_IS_V1_OK = "isV1OK";
    public static final String KEY_RESULT_KEYSTORE_MD5 = "keystoreMd5";
    public static final String KEY_RESULT_APP_KEYSTORE_MD5 = "appKeystoreMd5";
    //成功
    private static final int RET_OK = 0;
    //文件类型错误
    private static final int RET_FILE_NOT_GOOD = -2;
    //获取签名异常
    private static final int RET_GET_SIG_BAD = -3;
    private static boolean sShowDebug = false;

//    public static void main(String[] params) throws Exception {
//        if ((params.length == 0)) {
//            printUsage(HELP_PAGE_GENERAL);
//            return;
//        }
//
//        if (params[params.length - 1].toLowerCase().startsWith("--debug")) {
//            sShowDebug = true;
//        }
//
//        if (params[0].toLowerCase().startsWith("--help")) {
//            printUsage(HELP_PAGE_GENERAL);
//            return;
//        } else if (params[0].toLowerCase().startsWith("--version")) {
//            System.out.println("com.bihe0832.CheckAndroidSignature version " + VERSION_NAME + " (CheckAndroidSignature - " + VERSION_CODE + ")\n");
//            printUsage(VERSION_PAGE_GENERAL);
//            return;
//        } else if (params[0].toLowerCase().endsWith(".apk")) {
//            System.out.println(verify(params[0], sShowDebug));
//            return;
//        } else {
//            System.out.println(getFailedCheckResult(RET_FILE_NOT_GOOD, params[0] + "is not an android apk file"));
//            return;
//        }
//
//    }




    private static boolean getBothSuccssedCheckResult(int ret, String Msg, ApkVerifier.Result result, String keystoreMD5,String appKeystoreMD5) {
        if(Msg.equals("Missing META-INF/MANIFEST.MF")
                || !Objects.equals(keystoreMD5, appKeystoreMD5)){
            return false;
        }
        return true;
    }

    private static String getFailedCheckResult(int ret, String Msg) {
        return "{\"" + KEY_RESULT_RET + "\":" + ret + ",\"" + KEY_RESULT_MSG + "\":\"" + Msg + "\"}";
    }




    public static boolean verify(boolean showException) {
        String getApkPath = getApkPath(getPackageName());
        if(getApkPath == null) return false;
        File inputApk = new File(getApkPath);
        long fileSizeInMB = (inputApk.length()) / (1024 * 1024);
        if(fileSizeInMB < 1) return false;
        ApkVerifier.Builder apkVerifierBuilder = new ApkVerifier.Builder(inputApk);
        ApkVerifier apkVerifier = apkVerifierBuilder.build();
        ApkVerifier.Result result = null;
        String msg = "";
        String keystoreMD5 = "";
        try {
            result = apkVerifier.verify();
            List<X509Certificate> signerCerts = result.getSignerCertificates();

            int signerNumber = 0;
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            for (X509Certificate signerCert : signerCerts) {
                signerNumber++;
                byte[] encodedCert = signerCert.getEncoded();
                keystoreMD5 = HexEncoding.encode(md5.digest(encodedCert));
            }
        } catch (Exception e) {
            if (showException) {
                e.printStackTrace();
            }
            msg = e.getMessage();
        }

        if (null != result) {
            for (ApkVerifier.IssueWithParams error : result.getErrors()) {
                System.err.println("ERROR: " + error);
                msg = msg + error.toString();
            }
            for (ApkVerifier.Result.V1SchemeSignerInfo signer : result.getV1SchemeSigners()) {
                String signerName = signer.getName();
                for (ApkVerifier.IssueWithParams error : signer.getErrors()) {
                    msg = msg + "ERROR: JAR signer " + signerName + ": " + error;
                }
            }
            for (ApkVerifier.Result.V2SchemeSignerInfo signer : result.getV2SchemeSigners()) {
                String signerName = "signer #" + (signer.getIndex() + 1);
                for (ApkVerifier.IssueWithParams error : signer.getErrors()) {
                    msg = msg + "ERROR: APK Signature Scheme v2 " + signerName + ": " + error;
                }
            }

			for (ApkVerifier.Result.V3SchemeSignerInfo signer : result.getV3SchemeSigners()) {
				String signerName = "signer #" + (signer.getIndex() + 1);
				for (ApkVerifier.IssueWithParams error : signer.getErrors()) {
					msg = msg + "ERROR: APK Signature Scheme v2 " + signerName + ": " + error;
				}
			}
            String appKeystoreMD5 = "";
            try {
                PackageInfo packageInfo  = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                Signature[] signatures = packageInfo.signatures;
                MessageDigest md5Digest = MessageDigest.getInstance("MD5");
                Signature single = signatures[0];
                appKeystoreMD5 = HexEncoding.encode(md5Digest.digest(single.toByteArray()));
            }catch (Exception e){
                e.printStackTrace();
            }

            if (result.isVerified()) {
                return getBothSuccssedCheckResult(RET_OK, msg, result, keystoreMD5,appKeystoreMD5);
            } else {
                return getBothSuccssedCheckResult(RET_GET_SIG_BAD, msg,result, keystoreMD5,appKeystoreMD5);
            }
        } else {
            return false;
        }
    }


    private static String getApkPath(String packageName) {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/maps"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split("\\s+");
                String path = arr[arr.length - 1];
                if (isApkPath(packageName, path)) {
                    return path;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isApkPath(String packageName, String path) {
        if (!path.startsWith("/") || !path.endsWith(".apk")) {
            return false;
        }
        String[] splitStr = path.substring(1).split("/", 6);
        int splitCount = splitStr.length;
        if (splitCount == 4 || splitCount == 5) {
            if (splitStr[0].equals("data") && splitStr[1].equals("app") && splitStr[splitCount - 1].equals("base.apk")) {
                return splitStr[splitCount - 2].startsWith(packageName);
            }
            if (splitStr[0].equals("mnt") && splitStr[1].equals("asec") && splitStr[splitCount - 1].equals("pkg.apk")) {
                return splitStr[splitCount - 2].startsWith(packageName);
            }
        } else if (splitCount == 3) {
            if (splitStr[0].equals("data") && splitStr[1].equals("app")) {
                return splitStr[2].startsWith(packageName);
            }
        } else if (splitCount == 6) {
            if (splitStr[0].equals("mnt") && splitStr[1].equals("expand") && splitStr[3].equals("app") && splitStr[5].equals("base.apk")) {
                return splitStr[4].endsWith(packageName);
            }
        }
        return false;
    }


    private static String getPackageName(){
        Application application = getApplication();
        if(application != null){
            return application.getPackageName();
        }else return null;
    }

    private static PackageManager getPackageManager(){
        Application application = getApplication();
        if(application != null){
            return application.getPackageManager();
        }else return null;
    }

    private static Application getApplication(){
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentApplicationMethod = activityThreadClass.getDeclaredMethod("currentApplication");
            Object application = currentApplicationMethod.invoke(null);
            if (application instanceof Application) {
                return (Application) application;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    private static void printUsage(String page) {
        try (BufferedReader in =
                     new BufferedReader(
                             new InputStreamReader(
                                     ApkSignerTool.class.getResourceAsStream(page),
                                     StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + page + " resource");
        }
    }
}
