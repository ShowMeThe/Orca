package com.occ.orca.task

import com.occ.orca.Go
import com.occ.orca.KeyExt
import com.occ.orca.StringUtils
import com.squareup.javapoet.*
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.lang.model.element.Modifier

open class GenerateJavaClientFileTask : DefaultTask() {

    @InputDirectory
    lateinit var outputDir: File

    @Input
    lateinit var soHeadName: String

    @Input
    lateinit var keys: List<KeyExt>

    @Input
    var buildWithKotlin : Boolean = false

    @TaskAction
    fun generate() {
        if (buildWithKotlin) {
            buildKotlin()
        } else {
            buildJava()
        }
    }

    private fun buildKotlin() {
        val classes = com.squareup.kotlinpoet.TypeSpec.objectBuilder("CoreClient")
            .addInitializerBlock(
                com.squareup.kotlinpoet.CodeBlock.builder()
                    .addStatement("System.loadLibrary(%S)", "${soHeadName}-core-client")
                    .build()
            )

        val nativeFunction = FunSpec.builder("getString")
            .addModifiers(KModifier.EXTERNAL)
            .addParameter(ParameterSpec.builder("key", String::class).build())
            .returns(String::class)
            .build()

        classes.addFunction(nativeFunction)

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
            classes.addFunction(
                FunSpec.builder("get$name")
                    .addModifiers(KModifier.PUBLIC)
                    .addStatement("return getString(%S)", StringUtils.md5(it.name))
                    .returns(String::class)
                    .build()
            )
        }


        val file = FileSpec.builder("com.orcc.${soHeadName}.core", "CoreClient")
            .addType(classes.build())
            .build()
        file.writeTo(outputDir)
    }

    private fun buildJava() {
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