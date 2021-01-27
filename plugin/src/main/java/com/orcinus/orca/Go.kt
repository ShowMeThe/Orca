package com.orcinus.orca

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

class Go(val project: Project) {


    val keys: NamedDomainObjectContainer<KeyExt> by lazy { project.container(KeyExt::class.java) }


    fun encrypt(closure: Closure<NamedDomainObjectContainer<KeyExt>>) {
        print("encrypt start closure")
        keys.configure(closure)
    }

    fun encrypt(action: Action<NamedDomainObjectContainer<KeyExt>>) {
        print("encrypt start action")
        action.execute(keys)
    }


    fun printAll(){
        keys.forEach {
            print(it.toString())
        }
    }
}