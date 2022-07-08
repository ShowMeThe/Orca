package com.orcc.compiler

import org.gradle.api.Project

class GoCompiler(val project: Project) {

    var includes = arrayListOf<String>()

    fun add(value:String){
        includes.add(value)
    }
}