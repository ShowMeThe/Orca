package com.occ.orca.task

import com.occ.orca.AESEncryption
import com.occ.orca.KeyExt
import com.occ.orca.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateOccSoHeaderTask : DefaultTask() {

    @Input
    lateinit var inputFileDir :File

    @Input
    var signature = ""

    @Input
    var header = ""

    @Input
    var secretKey = "FTat46cvyia6758@243lid66wxzvwe23dgfhcfg76wsd@5as431aq1256dsaa211"

    @Input
    lateinit var keys: NamedDomainObjectContainer<KeyExt>

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

        lines.add("#define HEADER \"$header\"\n")

        lines.add("#define SECRET_KEY \"$secretKey\"\n")

        lines.add("#define LOAD_MAP(_map) \\\n")
        keys.forEach {
            lines.add("    _map[\"${StringUtils.md5(it.name)}\"] = \"${AESEncryption.encrypt(secretKey, it.value)}\"; \\\n")
        }

        lines.add("\n#endif //ORCA_CORE_CLIENT_H")
        val writer = file.bufferedWriter()
        lines.forEach {
            writer.write(it)
        }
        writer.flush()
        writer.close()
    }

}