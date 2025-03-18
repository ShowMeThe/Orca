package com.example.test

class MyApplication : NextApplication() {
    companion object {
        init {
            run2()
        }
        private fun run2() {}
    }

    override fun onCreate() {
        super.onCreate()
    }
}