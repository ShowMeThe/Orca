package com.example.test

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider


@Keep
class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application))
            .get(AndroidViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv = findViewById<View>(R.id.tv)
        tv.setOnClickListener {
          //  Log.e("2222222","${AppCore.getAes_key()} ${AppCore.getHttp_url_test()}")
        }


    }
}