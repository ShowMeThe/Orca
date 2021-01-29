package com.occ.orca

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.ide.common.symbols.getPackageNameFromManifest
import com.occ.orca.task.GenerateCMakeLists
import com.occ.orca.task.GenerateJavaClientFileTask
import com.occ.orca.task.GenerateOccSoHeaderTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.io.File

class OrcaPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.extensions.add("Orca", Orca(project))

        getPluginAttachProject(project)
    }


    private fun getPluginAttachProject(project: Project) {
        project.plugins.whenPluginAdded {
            when (this) {
                is AppPlugin -> {
                    val android = project.extensions.getByType(AppExtension::class.java)
                    attach2App(android, project)
                }
            }
        }
    }


    private fun attach2App(android: AppExtension, project: Project) {
        val nativeOriginPath = copyNativePath(project)
        project.copy {
            from(nativeOriginPath)
            include("src/main/cpp/**")
            into(File(project.buildDir, "orca.so"))
        }

        GenerateCMakeLists(project).build()

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

        project.afterEvaluate {

            val signature = (extensions.findByName("Orca") as Orca).go.signature


            android.applicationVariants.all {

                val task = project.tasks.create(
                    "generate${StringUtils.substring(name)}SoHeader",
                    GenerateOccSoHeaderTask::class.java
                )

                task.signature = signature
                task.inputFileDir = File("$cmakeListsDir/src/main/cpp/include")

                project.getTasksByName(
                    "generateJsonModel${StringUtils.substring(name)}",
                    false
                ).forEach {
                    it.dependsOn(task)
                }

                val outputDir = File(project.buildDir, "/generated/source/orca.so/${name}")
                val generateJavaClientTask = project.tasks.create(
                    "generate${StringUtils.substring(name)}JavaClient",
                    GenerateJavaClientFileTask::class.java
                )
                generateJavaClientTask.outputDir = outputDir
                registerJavaGeneratingTask(generateJavaClientTask, outputDir)


            }
        }
    }

    /**
     * find C++ root
     */
    private fun getNativeRelatePath(project: Project) =
        project.rootProject.subprojects.find { it.name == "orca-core" }

    /**
     * Copy C++ code
     */
    private fun copyNativePath(project: Project): String? {
        val findProject = getNativeRelatePath(project)
        return findProject?.buildDir?.canonicalPath?.substringBeforeLast("\\build")
    }

}