package com.example.test

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.testlibrary.Test
import com.occ.annotation.CoreDecryption
import com.occ.app.core.AppCore


@Keep
class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application))
            .get(AndroidViewModel::class.java)
    }

    @CoreDecryption("base")
    private var data2 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv = findViewById<View>(R.id.tv)
        tv.setOnClickListener {
            val startTime = System.currentTimeMillis()
            Log.e("222222","data2 = ${AppCore.getBase()} ${AppCore.getBase2()} ${AppCore.getBase3()} ${AppCore.getBase4()}")
            Log.e("222222","cost time = ${(System.currentTimeMillis() - startTime)}")
        }


    }
}