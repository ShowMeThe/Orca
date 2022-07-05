package com.occ.orca

import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.occ.orca.task.GenerateCMakeLists
import com.occ.orca.task.GenerateJavaClientFileTask
import com.occ.orca.task.GenerateOccSoHeaderTask
import javassist.ClassPool
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.*

class OrcaPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.extensions.add("Orca", Orca(project))

        getPluginAttachProject(project)
    }

    private var localSignature = ""

    private fun getPluginAttachProject(project: Project) {
        println("getPluginAttachProject ${project.name}")
        project.plugins.withId("com.android.application") {
            val testedExtension = project.extensions.getByType(TestedExtension::class.java)
            attach(testedExtension, project)
        }
        project.plugins.withId("com.android.library") {
            val testedExtension = project.extensions.getByType(TestedExtension::class.java)
            attach(testedExtension, project)
        }
    }

    private fun attach(android: TestedExtension, project: Project) {
        val nativeOriginPath = getNativeFile(project)
        copyNativeCode(nativeOriginPath, android, project)
        project.extensions.getByType(AndroidComponentsExtension::class.java).onVariants {
            android.sourceSets{
                val outputDir = File(project.buildDir, "/generated/source/orca/${it.name}")
                findByName(it.name)?.apply {
                    println("add sourceSet path = $outputDir")
                    java.srcDir(outputDir)
                    kotlin.srcDir(outputDir)
                }
            }
            project.afterEvaluate {
                buildTask(nativeOriginPath, it, project,android)
            }
        }
    }

    /**
     * copy Native code
     */
    private fun copyNativeCode(
        nativeOriginPath: Any?,
        android: TestedExtension,
        project: Project
    ) {
        println("copyNativeCode  $nativeOriginPath")
        val file = File(project.buildDir, "orca.so")
        if (!file.exists()) {
            file.mkdirs()
            project.copy {
                from(nativeOriginPath)
                include("src/main/cpp/**")
                into(file)
            }
        }

        GenerateCMakeLists(project).apply {
            libName = project.name
            val cmakeListsDir = project.buildDir.canonicalPath + File.separator + "orca.so"
            val cmakeListsPath = cmakeListsDir + File.separator + "CMakeLists.txt"
            println("GenerateCMakeLists = $cmakeListsPath")
            if (!File(cmakeListsPath).exists()) {
                build {
                    setUpCmake(cmakeListsPath, android)
                }
            } else {
                setUpCmake(cmakeListsPath, android)
            }
        }

    }

    /**
     * setUp cmake
     */
    private fun setUpCmake(cmakeListsPath: String, android: TestedExtension) {
        android.defaultConfig {
            externalNativeBuild {
                cmake {
                    cppFlags("")
                }
            }
        }
        android.externalNativeBuild {
            cmake {
                path = File(cmakeListsPath)
            }
        }
    }


    /**
     * build task
     */
    private fun buildTask(nativeOriginPath: Any?, variant: Variant, project: Project,android: TestedExtension) {
        val cmakeListsDir = project.buildDir.canonicalPath + File.separator + "orca.so"
        val go = (project.extensions.findByName("Orca") as Orca).go
        if (localSignature.isEmpty()) {
            localSignature = go.signature
        }

        val task = project.tasks.create(
            "generate${StringUtils.substring(variant.name)}SoHeader",
            GenerateOccSoHeaderTask::class.java
        ) {
            if (go.secretKey.isNotEmpty()) {
                this.secretKey = go.secretKey
            }
            this.keys = go.keys
            this.debug = go.isDebug
            this.header = project.name
            this.signature = localSignature
            this.encryptMode = go.encryptMode.toUpperCase(Locale.ENGLISH)
            this.inputFileDirPath = File("$cmakeListsDir/src/main/cpp/include").path
            this.nativeOriginPath = nativeOriginPath
        }

        val variantName = StringUtils.substring(variant.name)

        val configTask = project.tasks.filter {
            it.name.startsWith("configureCMake${variantName}")
        }
        println("configureCMake $configTask")
        configTask.forEach {
            it.dependsOn(task)
        }

        val outputDir = File(project.buildDir, "/generated/source/orca/${variant.name}")

        val mode = go.encryptMode.toUpperCase(Locale.ENGLISH)
        val path = when (mode) {
            "AES" -> {
                "aes"
            }
            "DES" -> {
                "des"
            }
            else -> {
                "aes"
            }
        }

        val copyAESEncryptionTask = project.tasks.register(
            "copy${variantName}EncryptionJavaCode",
            Copy::class.java
        ) {
            from(nativeOriginPath)
            include("src/main/java/com/occ/encrypt/${path}/**")
            into(outputDir)
        }


        val generateJavaClientTask = project.tasks.register(
            "generate${variantName}JavaClient",
            GenerateJavaClientFileTask::class.java
        ){
            this.keys = go.keys.toMutableList()
            this.soHeadName = project.name
            this.outputDir = outputDir
            this.buildWithKotlin = go.isBuildKotlin
        }

        generateJavaClientTask.dependsOn(copyAESEncryptionTask)

        val generateSourceTask = project.tasks.findByName("generate${variantName}Resources")
        generateSourceTask?.dependsOn(generateJavaClientTask)
    }


    /**
     * find C++ root
     */
    private fun getNativeFile(project: Project): Any? {
        return if (project.rootProject.subprojects.find { it.name == "orca-core" } != null) {
            project.rootProject.file("orca-core").canonicalPath
        } else {
            var fileProject = findNativeFromBuildScript(project)
            if (fileProject == null) {
                fileProject = findNativeFromBuildScript(project.rootProject)
            }
            fileProject
        }
    }

    private fun findNativeFromBuildScript(project: Project): Any? {
        var result: Any? = null
        project.buildscript.configurations.forEach { config ->
            val file = config.files.find {
                it.path.contains("orca", true) && it.name.contains("plugin", true)
            }
            if (file != null) {
                result = project.zipTree(file)
            }
        }
        return result
    }


}