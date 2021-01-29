package com.occ.orca

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

class Go(val project: Project) {


    val keys: NamedDomainObjectContainer<KeyExt> by lazy { project.container(KeyExt::class.java) }

    var signature = ""


    fun encrypt(closure: Closure<NamedDomainObjectContainer<KeyExt>>) {
        println("encrypt start closure")
        keys.configure(closure)
    }

    fun encrypt(action: Action<NamedDomainObjectContainer<KeyExt>>) {
        println("encrypt start action")
        action.execute(keys)
    }


    fun printAll(){
        keys.forEach {
            print(it.toString())
        }
    }
}