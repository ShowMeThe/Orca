package com.orcc.compiler

import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.tree.ClassNode


var projectName = "app"

class CompilerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.getByType(AndroidComponentsExtension::class.java).apply {
            this.onVariants { variant ->
                projectName = project.name
                variant.instrumentation.transformClassesWith(
                    ClassVisitorFactory::class.java,
                    InstrumentationScope.PROJECT
                ) {}
                variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
            }
        }
    }
}

abstract class ClassVisitorFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return CoreClassNode(nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }
}


class CoreClassNode(private val nextVisitor: ClassVisitor) : ClassNode(Opcodes.ASM9) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val oldMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val methodFind = name.equals("onCreate") || name.equals("<init>")
        val writeEnd = name.equals("<init>")
        if (methodFind) {
            val newMethodVisitor =
                object : AdviceAdapter(Opcodes.ASM9, oldMethodVisitor, access, name, descriptor) {
                    override fun onMethodEnter() {
                        super.onMethodEnter()
                        if(writeEnd.not()){
                            write()
                        }
                    }

                    override fun onMethodExit(opcode: Int) {
                        super.onMethodExit(opcode)
                        if(writeEnd){
                            write()
                        }
                    }

                    private fun write() {
                        mv.visitFieldInsn(
                            GETSTATIC,
                            "com/occ/annotation/CoreInject",
                            "Companion",
                            "Lcom/occ/annotation/CoreInject\$Companion;"
                        )
                        mv.visitLdcInsn(projectName)
                        mv.visitMethodInsn(
                            INVOKEVIRTUAL,
                            "com/occ/annotation/CoreInject\$Companion",
                            "getInstant",
                            "(Ljava/lang/String;)Lcom/occ/annotation/CoreInject;",
                            false
                        )
                        mv.visitVarInsn(ALOAD, 0)
                        mv.visitMethodInsn(
                            INVOKEVIRTUAL,
                            "com/occ/annotation/CoreInject",
                            "inject",
                            "(Ljava/lang/Object;)V",
                            false
                        )
                    }
                }
            return newMethodVisitor
        } else {
            return oldMethodVisitor
        }
    }

    override fun visitEnd() {
        super.visitEnd()
        accept(nextVisitor)
    }
}
