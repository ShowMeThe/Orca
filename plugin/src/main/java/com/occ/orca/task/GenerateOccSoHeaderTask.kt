package com.occ.orca.task

import com.occ.orca.AESEncryption
import com.occ.orca.DESEncryption
import com.occ.orca.KeyExt
import com.occ.orca.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.*
import java.io.File

open class GenerateOccSoHeaderTask : DefaultTask() {

    @InputDirectory
    lateinit var inputFileDir: File

    @Input
    var nativeOriginPath = ""

    @Input
    var cmakeListsDir = ""

    @Input
    var signature = ""

    @Input
    var header = ""

    @Input
    var debug = false

    @Input
    var encryptMode = "AES" //AES,DES

    @Input
    var secretKey = "FTat46cvyia6758@243lid66wxzvwe23dgfhcfg76wsd@5as431aq1256dsaa211"

    @Input
    lateinit var keys: NamedDomainObjectContainer<KeyExt>

    @TaskAction
    fun generate() {
        println("GenerateOccSoHeaderTask before ${inputFileDir.exists()}")
        if (inputFileDir.exists().not()) {
            val file = File(cmakeListsDir)
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
        }
        println("GenerateOccSoHeaderTask after ${inputFileDir.exists()}")
        val file = File(inputFileDir, "core-client.h")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val lines = ArrayList<String>()
        lines.add(
            "#ifndef ORCA_CORE_CLIENT_H\n" +
                    "#define ORCA_CORE_CLIENT_H\n"
        )

        lines.add(
            "#include <jni.h>\n" +
                    "#include <map>\n" +
                    "#include <string>\n"
        )

        lines.add("#define CA \"$signature\"\n")

        lines.add("#define HEADER \"$header\"\n")

        lines.add("#define MODE \"$encryptMode\"\n")

        lines.add("#define DEBUG $debug \n")

        lines.add("#define QA \"$secretKey\"\n")

        lines.add("#define LOAD_MAP(_map) \\\n")
        keys.forEach {
            val value = when (encryptMode) {
                "AES" -> {
                    AESEncryption.encrypt(secretKey, it.value)
                }
                "DES" -> {
                    DESEncryption.encrypt(secretKey, it.value)
                }
                else -> {
                    AESEncryption.encrypt(secretKey, it.value)
                }
            }
            lines.add("    _map[\"${StringUtils.md5(it.name)}\"] = \"${value}\"; \\\n")
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