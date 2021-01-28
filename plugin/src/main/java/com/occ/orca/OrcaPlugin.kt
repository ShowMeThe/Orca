package com.occ.orca

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.ide.common.symbols.getPackageNameFromManifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class OrcaPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.extensions.add("Orca", Orca(project))

        getPluginAttachProject(project)
    }


    private fun getPluginAttachProject(project: Project) {
        project.plugins.whenPluginAdded {
            when(this){
                is AppPlugin ->{
                    val android = project.extensions.getByType(AppExtension::class.java)
                    attach2App(android,project)
                }
            }
        }
    }


    private fun attach2App(android: AppExtension,project: Project) {
       project.afterEvaluate {
           android.applicationVariants.all {
               print("app id = $applicationId")



           }

       }
    }

}