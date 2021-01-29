package com.occ.orca.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateOccSoHeaderTask : DefaultTask() {

    @Input
    lateinit var inputFileDir :File

    @Input
    var signature = ""

    @TaskAction
    fun generate() {
        val file = File(inputFileDir,"core-client.h")
        if(file.exists()){
            file.delete()
        }
        file.createNewFile()
        val lines =  ArrayList<String>()
        lines.add("#ifndef ORCA_CORE_CLIENT_H\n" +
                "#define ORCA_CORE_CLIENT_H\n")

        lines.add("#include <jni.h>\n" +
                "#include <map>\n" +
                "#include <string>\n")

        lines.add("#define SIGNATURE \"$signature\"\n")


        lines.add("\n#endif //ORCA_CORE_CLIENT_H")
        val writer = file.bufferedWriter()
        lines.forEach {
            writer.write(it)
        }
        writer.flush()
        writer.close()
    }

}