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
        project.extensions.add(containerType, "KeyExt", type)
    }

    val keys: NamedDomainObjectContainer<KeyExt> by lazy { project.container(KeyExt::class.java) }

    var signature = ""

    var secretKey = ""

    var isDebug = true

    var encryptMode = "AES"

    var isBuildKotlin = true

    var cacheValue = false

    fun storeSet(closure: Closure<NamedDomainObjectContainer<KeyExt>>) {
        println("encrypt start closure")
        keys.configure(closure)
    }

    fun storeSet(action: Action<NamedDomainObjectContainer<KeyExt>>) {
        println("encrypt start action")
        action.execute(keys)
    }

    fun storeSetDslBlock(action: Action<BlockContainer>) {
        println("encrypt start action")
        val block = BlockContainer()
        action.execute(block)
        keys.addAll(block.store)
    }


    fun printAll() {
        keys.forEach {
            print(it.toString())
        }
    }


    inner class BlockContainer {
        val store = ArrayList<KeyExt>()
        infix fun String.to(value: String) = KeyExt(this).apply {
            value(value)
        }.also {
            store.add(it)
        }
    }

}