package com.occ.orca.task

import org.gradle.api.Project
import java.io.File

class GenerateCMakeLists(val project: Project) {

     var libName = ""
       set(value) {
           field = "$value-core-client"
       }

    fun build(call:()->Unit){
        val cmakeListsDir = File(project.buildDir, "orca.so")
        val cmakeFileWriter = File(cmakeListsDir, "CMakeLists.txt").bufferedWriter()

        val targetPath = cmakeListsDir.path.toString().replace("\\","/")

        val lines =  ArrayList<String>()

        lines.add("cmake_minimum_required(VERSION 3.4.1)\n")
        lines.add("add_library(\n" +
                "        $libName\n" +
                "\n" +
                "        SHARED\n" +
                "\n" +
                "        ${targetPath}/src/main/cpp/core-client.cpp\n" +
                "        ${targetPath}/src/main/cpp/core-environment.cpp\n" +
                "        ${targetPath}/src/main/cpp/core-encryption.cpp\n" +
                ")")
        lines.add("\nfind_library(\n" +
                "              log-lib\n" +
                "              log )")
        lines.add("\ntarget_link_libraries(\n" +
                "        $libName\n" +
                "                     \${log-lib} )")

        lines.forEach {
            cmakeFileWriter.write(it)
        }
        cmakeFileWriter.flush()
        cmakeFileWriter.close()
        call.invoke()
    }

}