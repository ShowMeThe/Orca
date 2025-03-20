package com.example.test;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cmd {

    public static void main() {
        try {
            ProcessBuilder builder = new ProcessBuilder("keytool -list -printcert -jarfile");
            builder.redirectErrorStream(true); // 合并标准输出和错误输出
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d("2222222222","line = " + line);
            }
            reader.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
