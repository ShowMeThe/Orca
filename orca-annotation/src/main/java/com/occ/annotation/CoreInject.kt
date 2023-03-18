package com.occ.annotation

import android.util.ArrayMap
import android.util.Log

class CoreInject private constructor(private val projectName: String) {

    companion object {
        private val instants = ArrayMap<String, CoreInject>()

        @JvmStatic
        fun getInstant(projectName: String): CoreInject {
            val core = instants[projectName] ?: CoreInject(projectName).also {
                instants[projectName] = it
            }
            return core
        }
    }

    private val clazzName by lazy {
        projectName.substring(0, 1).uppercase() + projectName.substring(
            1
        )
    }

    private val coreClazz by lazy { Class.forName("com.occ.${projectName}.core.${clazzName}Core") }

    private val methods by lazy { coreClazz.declaredMethods }

    private val coreInstant by lazy {
      kotlin.runCatching {
            coreClazz.getDeclaredField("INSTANCE").let {
                it.isAccessible = true
                it.get(coreClazz)
            }
        }.getOrDefault(coreClazz)
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