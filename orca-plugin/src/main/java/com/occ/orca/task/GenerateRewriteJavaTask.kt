package com.occ.orca.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.Charset

open class GenerateRewriteJavaTask : DefaultTask() {

    @InputDirectory
    lateinit var dirFile: File


    @InputDirectory
    lateinit var md5File: File

    @TaskAction
    fun generate() {
        var sb = StringBuilder()
        val encryptionFile = dirFile.listFiles()!![0]
        val utf = Charset.forName("UTF-8")
        encryptionFile.reader(utf).use {
            it.readLines().forEachIndexed { index, s ->
                if (index == 0) {
                    sb.append(s.replace("encrypt","app"))
                } else {
                    sb.append(s)
                }
                sb.append("\r\n")
            }
        }
        encryptionFile.bufferedWriter()
            .use {
                it.write(sb.toString())
            }

        sb = StringBuilder()
        val encryptionMd5File = md5File.listFiles()!![0]
        encryptionMd5File.reader(utf).use {
            it.readLines().forEachIndexed { index, s ->
                if (index == 0) {
                    sb.append(s.replace("encrypt","app"))
                } else {
                    sb.append(s)
                }
                sb.append("\r\n")
            }
        }
        encryptionMd5File.bufferedWriter()
            .use {
                it.write(sb.toString())
            }
    }

}