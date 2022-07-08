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

@Keep
class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application))
            .get(AndroidViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv = findViewById<TextView>(R.id.tv)
        tv.setOnClickListener {
            Log.e("222222","data = ${viewModel.getValue()} data2 = ${AppCore.getBase()}  data3 = ${Test.data}")
        }


    }
}