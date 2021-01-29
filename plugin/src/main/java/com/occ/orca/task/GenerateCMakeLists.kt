package com.occ.orca.task

import org.gradle.api.Project
import java.io.File

class GenerateCMakeLists(val project: Project) {

    fun build(){
        val cmakeListsDir = File(project.buildDir, "orca.so")
        val cmakeFileWriter = File(cmakeListsDir, "CMakeLists.txt").bufferedWriter()

        val targetPaht = cmakeListsDir.path.toString().replace("\\","/")

        val lines =  ArrayList<String>()
        lines.add("cmake_minimum_required(VERSION 3.4.1)\n")
        lines.add("add_library(\n" +
                "        core-client\n" +
                "\n" +
                "        SHARED\n" +
                "\n" +
                "        ${targetPaht}/src/main/cpp/core-client.cpp\n" +
                "        ${targetPaht}/src/main/cpp/core-enviroment.cpp\n" +
                ")")
        lines.add("\nfind_library(\n" +
                "              log-lib\n" +
                "              log )")
        lines.add("\ntarget_link_libraries(\n" +
                "        core-client\n" +
                "                     \${log-lib} )")

        lines.forEach {
            cmakeFileWriter.write(it)
        }
        cmakeFileWriter.flush()
        cmakeFileWriter.close()
    }

}