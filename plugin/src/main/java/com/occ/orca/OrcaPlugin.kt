package com.occ.orca

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.occ.orca.task.GenerateCMakeLists
import com.occ.orca.task.GenerateJavaClientFileTask
import com.occ.orca.task.GenerateOccSoHeaderTask
import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.kotlin.dsl.project
import java.io.File
import java.util.*

class OrcaPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.extensions.add("Orca", Orca(project))

        getPluginAttachProject(project)
    }

    private var localSignature = ""

    private fun getPluginAttachProject(project: Project) {
        project.plugins.whenPluginAdded {
            when (this) {
                is AppPlugin -> {
                    val android = project.extensions.getByType(AppExtension::class.java)
                    attach2App(android, project)
                }
                is LibraryPlugin -> {
                    val library = project.extensions.getByType(LibraryExtension::class.java)
                    attach2Lib(library, project)
                }
            }
        }
    }

    private fun attach2Lib(android: LibraryExtension, target: Project) {
        val nativeOriginPath = getNativeFile(target)
        copyNativeCode(nativeOriginPath, android, target)
        target.dependencies.apply {
            add("implementation", project(":orca-encrypt"))
        }
        target.afterEvaluate {
            android.libraryVariants.all {
                buildTask(this, target)
            }
        }
    }


    private fun attach2App(android: AppExtension, target: Project) {
        val nativeOriginPath = getNativeFile(target)
        copyNativeCode(nativeOriginPath, android, target)
        target.dependencies.apply {
            add("implementation", project(":orca-encrypt"))
        }
        target.afterEvaluate {
            android.applicationVariants.all {
                buildTask(this, target)
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
        val file = File(project.buildDir, "orca.so")
        if (!file.exists()) {
            project.copy {
                from(nativeOriginPath)
                include("src/main/cpp/**")
                into(File(project.buildDir, "orca.so"))
            }
        }

        GenerateCMakeLists(project).apply {
            lib_name = project.name
            build()
        }

        android.defaultConfig {
            externalNativeBuild {
                cmake {
                    cppFlags("")
                }
            }
        }
        val cmakeListsDir = project.buildDir.canonicalPath + File.separator + "orca.so"
        val cmakeListsPath = cmakeListsDir + File.separator + "CMakeLists.txt"
        android.externalNativeBuild {
            cmake {
                path = File(cmakeListsPath)
            }
        }
    }


    /**
     * build task
     */
    private fun buildTask(variant: BaseVariant, project: Project) {
        val cmakeListsDir = project.buildDir.canonicalPath + File.separator + "orca.so"
        val go = (project.extensions.findByName("Orca") as Orca).go
        if (localSignature.isEmpty()) {
            localSignature = go.signature
        }

        val task = project.tasks.create(
            "generate${StringUtils.substring(variant.name)}SoHeader",
            GenerateOccSoHeaderTask::class.java
        )
        if (go.secretKey.isNotEmpty()) {
            task.secretKey = go.secretKey
        }
        task.keys = go.keys
        task.header = project.name
        task.signature = localSignature
        task.inputFileDir = File("$cmakeListsDir/src/main/cpp/include")

        project.getTasksByName(
            "generateJsonModel${StringUtils.substring(variant.name)}",
            false
        ).forEach {
            it.dependsOn(task)
        }

        val outputDir = File(project.buildDir, "/generated/source/orca.so/${variant.name}")
        val generateJavaClientTask = project.tasks.create(
            "generate${StringUtils.substring(variant.name)}JavaClient",
            GenerateJavaClientFileTask::class.java
        )
        generateJavaClientTask.keys = go.keys
        generateJavaClientTask.soHeadName = project.name
        generateJavaClientTask.outputDir = outputDir
        variant.registerJavaGeneratingTask(generateJavaClientTask, outputDir)

    }

    /**
     * find C++ root
     */
    private fun getNativeFile(project: Project): Any? {
        if (project.rootProject.subprojects.find { it.name == "orca-core" } != null) {
            return project.rootProject.file("orca-core").canonicalPath
        } else {
            var fileProject = findNativeFromBuildScript(project)
            if (fileProject == null) {
                fileProject = findNativeFromBuildScript(project.rootProject)
            }
            return fileProject
        }
    }

    private fun findNativeFromBuildScript(project: Project): Any? {
        var result: Any? = null
        project.buildscript.configurations.forEach { config ->
            val file = config.files.find {
                it.name.toUpperCase(Locale.getDefault()).contains("ORCA.SO")
            }
            if(file!=null){
                result = project.zipTree(file)
            }
        }
        return result
    }


}