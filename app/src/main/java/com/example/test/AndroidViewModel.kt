package com.example.test

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.occ.annotation.CoreDecryption

class AndroidViewModel(application: Application) : AndroidViewModel(application) {

    @CoreDecryption("base")
    private var data2 = ""

    fun getValue2() = data2

}