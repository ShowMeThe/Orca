package com.example.test

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.testlibrary.Test
import com.occ.annotation.CoreDecryption
import com.occ.annotation.CoreInject
import com.occ.app.core.AppCore

import android.view.View
import androidx.appcompat.app.AppCompatActivity


@Keep
class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application))
            .get(AndroidViewModel::class.java)
    }

    @CoreDecryption("data")
    private var data = ""

    @CoreDecryption("base")
    private var data2 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv = findViewById<TextView>(R.id.tv)
        tv.setOnClickListener {
            Log.e("222222","value1 = $data value2 = ${data2}")
            Log.e("222222","value1 = ${viewModel.getValue()}  value2 = ${viewModel.getValue2()}  data2 = ${AppCore.getBase()}  data3 = ${Test.data}")
        }


    }
}