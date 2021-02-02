package com.occ.orca

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import java.io.Closeable

class Orca(val project: Project) {

    val go = Go(project)

    fun go(closure : Closure<Go>){
        project.configure(go,closure)
    }

    fun go(closure : Action<Go>){
        closure.invoke(go)
    }

}