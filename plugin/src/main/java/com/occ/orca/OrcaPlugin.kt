package com.occ.orca

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import com.occ.orca.task.GenerateCMakeLists
import com.occ.orca.task.GenerateJavaClientFileTask
import com.occ.orca.task.GenerateOccSoHeaderTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
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
            if (testedExtension is AppExtension) {
                println("AppExtension")
                attach2App(testedExtension, project)
            }
        }
        project.plugins.withId("com.android.library") {
            val testedExtension = project.extensions.getByType(TestedExtension::class.java)
            if (testedExtension is LibraryExtension) {
                println("LibraryExtension")
                attach2Lib(testedExtension, project)
            }
        }
    }

    private fun attach2Lib(android: LibraryExtension, target: Project) {
        val nativeOriginPath = getNativeFile(target)
        copyNativeCode(nativeOriginPath, android, target)
        target.afterEvaluate {
            android.libraryVariants.all {
                buildTask(nativeOriginPath, this, target)
            }
        }
    }


    private fun attach2App(android: AppExtension, target: Project) {
        val nativeOriginPath = getNativeFile(target)
        copyNativeCode(nativeOriginPath, android, target)
        target.afterEvaluate {
            android.applicationVariants.all {
                buildTask(nativeOriginPath, this, target)
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
        println("copyNativeCode  $nativeOriginPath nativeOriginPath file ${File(nativeOriginPath as String).exists()}")
        val file = File(project.buildDir.canonicalPath, "orca.so")
        println("copyNativeCode copy to target $file")
        if (!file.exists()) {
            file.mkdirs()
            project.copy {
                from(nativeOriginPath)
                include("src/main/cpp/**")
                into(file)
            }
            println("listFiles = ${file.listFiles()?.size}")
            file.listFiles()?.forEach {
                println("file in list path = ${it.path}")
            }
            println("copyNativeCode copy to target $file")
        }

        GenerateCMakeLists(project).apply {
            libName = project.name
            val cmakeListsDir = project.buildDir.canonicalPath + File.separator + "orca.so"
            val cmakeListsPath = cmakeListsDir + File.separator + "CMakeLists.txt"
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
    private fun buildTask(nativeOriginPath: Any?, variant: BaseVariant, project: Project) {
        val cmakeListsDir = project.buildDir.canonicalPath + File.separator + "orca.so"
        val go = (project.extensions.findByName("Orca") as Orca).go
        if (localSignature.isEmpty()) {
            localSignature = go.signature
        }
        val inputFile = File("$cmakeListsDir/src/main/cpp/include")
        val task = project.tasks.create(
            "generate${StringUtils.substring(variant.name)}SoHeader",
            GenerateOccSoHeaderTask::class.java
        )
        if (go.secretKey.isNotEmpty()) {
            task.secretKey = go.secretKey
        }
        task.keys = go.keys
        task.debug = go.isDebug
        task.header = project.name
        task.signature = localSignature
        task.encryptMode = go.encryptMode.toUpperCase(Locale.ENGLISH)
        task.cmakeListsDir = cmakeListsDir
        task.cmakeListsDir = nativeOriginPath as String
        task.inputFileDir = inputFile

        project.getTasksByName(
            "generateJsonModel${StringUtils.substring(variant.name)}",
            false
        ).forEach {
            it.dependsOn(task)
        }

        val outputDir = File(project.buildDir, "/generated/source/orca/${variant.name}")
        val generateJavaClientTask = project.tasks.create(
            "generate${StringUtils.substring(variant.name)}JavaClient",
            GenerateJavaClientFileTask::class.java
        )

        generateJavaClientTask.keys = go.keys
        generateJavaClientTask.soHeadName = project.name
        generateJavaClientTask.outputDir = outputDir
        generateJavaClientTask.go = go
        variant.registerJavaGeneratingTask(generateJavaClientTask, outputDir)


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

        val copyAESEncryptionTask = project.tasks.create(
            "copyJavaCode${StringUtils.substring(variant.name)}",
            Copy::class.java
        ) {
            from(nativeOriginPath)
            include("src/main/java/com/occ/encrypt/${path}/**")
            into(outputDir)
        }
        generateJavaClientTask.dependsOn(copyAESEncryptionTask)


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
                it.name.toUpperCase(Locale.getDefault()).contains("ORCA")
            }
            if (file != null) {
                result = project.zipTree(file)
            }
        }
        return result
    }


}