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

    @Input
    var inputFileDirPath = ""

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
        val inputFileDir = File(inputFileDirPath)
        println("GenerateOccSoHeaderTask before ${inputFileDir.exists()}")
        GenerateCMakeLists(project).apply {
            libName = project.name
            val cmakeListsDir = project.buildDir.canonicalPath + File.separator + "orca.so"
            val cmakeListsDirFile = File(cmakeListsDir)
            if(cmakeListsDirFile.exists().not()){
                cmakeListsDirFile.mkdirs()
            }
            val cmakeListsPath = cmakeListsDir + File.separator + "CMakeLists.txt"
            val cmakeListFile = File(cmakeListsPath)
            if (cmakeListFile.exists()) {
                cmakeListFile.delete()
            }
            if (cmakeListFile.exists().not()) {
                build {}
            }
        }
        if (inputFileDir.exists().not()) {
            val file = File(cmakeListsDir)
            println("copyNativeCode copy to target $file")
            if (!File(file.path + "src").exists()) {
                project.copy {
                    from(nativeOriginPath)
                    include("src/main/cpp/**")
                    exclude("src/main/cpp/include/core-client.h")
                    into(file)
                }
                println("GenerateOccSoHeaderTask copyNativeCode copy to target $file")
            }
        }
        println("GenerateOccSoHeaderTask after ${inputFileDir}")
        val file = File(inputFileDir.path + File.separator + "core-client.h")
        println("GenerateOccSoHeaderTask core-client ${file.path}")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        println("GenerateOccSoHeaderTask core-client file ${file.path}")
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