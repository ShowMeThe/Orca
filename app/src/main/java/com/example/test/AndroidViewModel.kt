package com.example.test

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.occ.annotation.CoreDecryption

class AndroidViewModel(application: Application) : AndroidViewModel(application) {

    @CoreDecryption("data")
    private var data = ""


    fun getValue() = data
}