package com.occ.orca

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.reflect.TypeOf

class Go(val project: Project) {

    init {
        val type = project.container(KeyExt::class.java)
        val containerType = object : TypeOf<NamedDomainObjectContainer<KeyExt>>() {}
        project.extensions.add(containerType,"KeyExt",type)
    }

    val keys: NamedDomainObjectContainer<KeyExt> by lazy { project.container(KeyExt::class.java) }

    var signature = ""

    var secretKey = ""

    var isDebug = false

    fun storeSet(closure: Closure<NamedDomainObjectContainer<KeyExt>>) {
        println("encrypt start closure")
        keys.configure(closure)
    }

    fun storeSet(action: Action<NamedDomainObjectContainer<KeyExt>>) {
        println("encrypt start action")
        action.execute(keys)
    }



    fun printAll(){
        keys.forEach {
            print(it.toString())
        }
    }
}