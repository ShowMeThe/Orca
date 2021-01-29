package com.occ.orca.task

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.lang.model.element.Modifier

open class GenerateJavaClientFileTask : DefaultTask() {

    @Input
    lateinit var outputDir:File

    @TaskAction
    fun generate(){
        val classBuilder = TypeSpec.classBuilder("CoreClient")
            .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addException(IllegalAccessException::class.java)
                    .addStatement("throw new IllegalAccessException()")
                    .build())

        classBuilder.addStaticBlock(CodeBlock.builder().addStatement("System.loadLibrary(\"\$L\")","core-client").build())

        classBuilder.addMethod(MethodSpec.methodBuilder("getString")
            .addModifiers(Modifier.NATIVE,Modifier.STATIC,Modifier.PUBLIC)
            .returns(String::class.java)
            .build())

        JavaFile.builder("com.orcc.core", classBuilder.build()).build().writeTo(outputDir)

    }

}