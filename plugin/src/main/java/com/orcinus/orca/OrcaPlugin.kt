package com.orcinus.orca

import org.gradle.api.Plugin
import org.gradle.api.Project

class OrcaPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.extensions.add("Orca",Orca(project))

    }
}