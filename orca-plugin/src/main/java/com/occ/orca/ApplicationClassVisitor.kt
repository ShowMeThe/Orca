package com.occ.orca

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.tree.ClassNode


interface ApplicationInstrumentationImp : InstrumentationParameters {

    @get:Input
    val projectName: Property<String>

    @get:Input
    val classMap: MapProperty<String, String>

    @get:Input
    @get:Optional
    val findMethodName : Property<String?>
}

abstract class ApplicationClassVisitorFactory() :
    AsmClassVisitorFactory<ApplicationInstrumentationImp> {



    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return CheckClassNode(nextClassVisitor,parameters.get().findMethodName.get(),parameters.get().projectName.get())
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val clazz = classData.className
        val map = parameters.get().classMap.get()
        val keys = map.keys
        var isInstrumentable = false
        if (keys.contains(clazz)) {
            val findMethodName = map[clazz] ?: ""
            parameters.get().findMethodName.set(findMethodName)
            isInstrumentable = findMethodName.isNotBlank()
        }
        return isInstrumentable
    }
}

class CheckClassNode(private val nextVisitor: ClassVisitor,
                     private val findMethodName:String?,
                     private val projectName: String) :
    ClassNode(Opcodes.ASM9) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val oldMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val addInOnCreate = name.equals(findMethodName)
        println("application run $addInOnCreate")
        if (addInOnCreate) {
            val newMethodVisitor =
                object : AdviceAdapter(Opcodes.ASM9, oldMethodVisitor, access, name, descriptor) {
                    override fun onMethodEnter() {
                        super.onMethodEnter()
                    }

                    override fun onMethodExit(opcode: Int) {
                        super.onMethodExit(opcode)
                        write()
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
                        //mv.visitVarInsn(ALOAD, 0)
                        mv.visitMethodInsn(
                            INVOKEVIRTUAL,
                            "com/occ/annotation/CoreInject",
                            "check",
                            "()V",
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