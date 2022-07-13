package com.example.test

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.orcc.app.core.CoreClient


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val tv = findViewById<View>(R.id.tv)
        tv.setOnClickListener {
            val data = CoreClient.getData()
            Log.e("2222","data = $data")
        }

    }
}