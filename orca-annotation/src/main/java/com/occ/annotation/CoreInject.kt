package com.occ.annotation

import android.app.Application
import android.util.ArrayMap
import android.util.Log

class CoreInject private constructor() {

    companion object {

        private val _instant by lazy { CoreInject() }

        @JvmStatic
        fun getInstant(): CoreInject {
            return _instant
        }
    }


    private val coreClazz by lazy { Class.forName("com.occ.app.core.AppCore") }

    private val methods by lazy { coreClazz.declaredMethods }

    private val coreInstant by lazy {
      kotlin.runCatching {
          coreClazz.declaredFields.first{ it. name == "INSTANCE"}
              .let {
                  it.isAccessible = true
                  it.get(coreClazz)
              }
        }.getOrDefault(coreClazz)
    }

    fun a(a:String,b:String,c:String){
        kotlin.runCatching {
            val getMethod = methods.firstOrNull { it.name == "see" }
            getMethod?.isAccessible = true
            getMethod?.invoke(coreInstant,a,b,c)
        }.onFailure {
            it.printStackTrace()
        }
    }


    fun take(application: Application){
        kotlin.runCatching {
            val getMethod = methods.firstOrNull { it.name == "check" }
            getMethod?.isAccessible = true
            getMethod?.invoke(coreInstant)
            Monster.runJob(application)
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun inject(any: Any) {
        any::class.java.declaredFields.forEach {
            if (it.isAnnotationPresent(CoreDecryption::class.java) && it.type == String::class.java) {
                kotlin.runCatching {
                    it.isAccessible = true
                    val annotationClazz =
                        requireNotNull(it.getAnnotation(CoreDecryption::class.java))
                    val methodName = "get${annotationClazz.keyName.getMethodName()}"
                    val method = methods.singleOrNull { mt -> mt.name.equals(methodName) }
                    val value = requireNotNull(method).invoke(coreInstant)
                    it.set(any, value)
                }.onFailure { ex ->
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun String.getMethodName() = this.let { name ->
        return@let if (name.first().isDigit()) {
            "_$name"
        } else {
            val newName = name.toCharArray()
            val char = newName[0]
            newName[0] = char.uppercaseChar()
            String(newName)
        }
    }

}