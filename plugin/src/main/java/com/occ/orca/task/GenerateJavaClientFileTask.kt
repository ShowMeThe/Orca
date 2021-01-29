package com.occ.orca.task

import com.occ.orca.KeyExt
import com.occ.orca.StringUtils
import com.squareup.javapoet.*
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.lang.model.element.Modifier

open class GenerateJavaClientFileTask : DefaultTask() {

    @Input
    lateinit var outputDir: File

    @Input
    lateinit var soHeadName: String

    @Input
    lateinit var keys: NamedDomainObjectContainer<KeyExt>

    @TaskAction
    fun generate() {
        val classBuilder = TypeSpec.classBuilder("CoreClient")
            .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addException(IllegalAccessException::class.java)
                    .addStatement("throw new IllegalAccessException()")
                    .build()
            )

        classBuilder.addStaticBlock(
            CodeBlock.builder()
                .addStatement("System.loadLibrary(\"\$L\")", "${soHeadName}-core-client").build()
        )

        classBuilder.addMethod(
            MethodSpec.methodBuilder("getString")
                .addModifiers(Modifier.NATIVE, Modifier.STATIC, Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .returns(String::class.java)
                .build()
        )

        keys.forEach {
            val name = it.name.let { name ->
                return@let if (name.first().isDigit()) {
                    "_$name"
                } else {
                    val newName = name.toCharArray()
                    val char = newName[0]
                    newName[0] = char.toUpperCase()
                    String(newName)
                }
            }
            classBuilder.addMethod(
                MethodSpec.methodBuilder("get$name")
                    .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                    .addStatement("return getString(\"\$L\")", StringUtils.md5(it.name))
                    .returns(String::class.java)
                    .build()
            )
        }


        JavaFile.builder("com.orcc.${soHeadName}.core", classBuilder.build()).build()
            .writeTo(outputDir)

    }

}