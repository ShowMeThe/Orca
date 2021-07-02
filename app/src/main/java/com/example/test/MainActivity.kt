package com.example.test

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.orcc.app.core.CoreClient


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<TextView>(R.id.tv).apply {
            text = CoreClient.getData()
        }



    }
}