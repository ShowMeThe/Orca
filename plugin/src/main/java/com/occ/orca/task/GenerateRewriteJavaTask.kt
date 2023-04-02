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

    @Input
    lateinit var soHeaderName: String

    @TaskAction
    fun generate() {
        val sb = StringBuilder()
        val encryptionFile = dirFile.listFiles()!![0]
        val utf = Charset.forName("UTF-8")
        encryptionFile.reader(utf).use {
            it.readLines().forEachIndexed { index, s ->
                if (index == 0) {
                    sb.append(s.replace("encrypt",soHeaderName))
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
    }

}