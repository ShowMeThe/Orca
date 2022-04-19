package com.example.test

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.orcc.app.core.CoreClient


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var lastTime = System.nanoTime()
        var data = CoreClient.getData()
        Log.e("MainActivity","${data} ${System.nanoTime() - lastTime}")
        lastTime = System.nanoTime()
        data = CoreClient.getData()
        Log.e("MainActivity","${data} ${System.nanoTime() - lastTime}")


    }
}