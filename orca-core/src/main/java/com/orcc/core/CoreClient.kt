package com.orcc.core

class CoreClient {

    companion object{
        private val instant by lazy { CoreClient() }

        @JvmStatic
        fun getClient() = instant
    }

    init {
        System.loadLibrary("core-client")
    }

    external fun getString(): String

}