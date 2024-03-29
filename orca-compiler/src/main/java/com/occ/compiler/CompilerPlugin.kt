package com.orcc.compiler

import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode


class CompilerPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        project.extensions.getByType(AndroidComponentsExtension::class.java).apply {
            this.onVariants { variant ->
                variant.instrumentation.transformClassesWith(
                    ClassVisitorFactory::class.java,
                    InstrumentationScope.PROJECT
                ) {
                    it.projectName.set(project.name)
                }

                variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
            }
        }
    }
}

interface InstrumentationImp : InstrumentationParameters {

    @get:Input
    val projectName: Property<String>

}

abstract class ClassVisitorFactory : AsmClassVisitorFactory<InstrumentationImp> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return CoreClassNode(nextClassVisitor,parameters.get().projectName.get())
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }
}


class CoreClassNode(private val nextVisitor: ClassVisitor, private val projectName: String) :
    ClassNode(Opcodes.ASM9) {

    private val defaultState = 0x0000
    private var state = defaultState
    private val hasAnnotation = 0x0002
    private val hasStatic = 0x0004
    private val hasNoStatic = 0x0008

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        return super.visitField(access, name, descriptor, signature, value) as FieldNode
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        fields.forEach {
            if (it.visibleAnnotations.isNullOrEmpty().not()) {
                it.visibleAnnotations.forEach { annotationNode ->
                    if (annotationNode.desc.contains(
                            "CoreDecryption",
                            true
                        )
                    ) {
                        state = state or hasAnnotation
                        state = if (it.access and Opcodes.ACC_STATIC == Opcodes.ACC_STATIC) {
                            state or hasStatic
                        } else {
                            state or hasNoStatic
                        }
                    }
                }
            }
        }
        val oldMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)

        val addInOnCreate = name.equals("onCreate")
        val hasAnnotation = state and hasAnnotation == hasAnnotation
        val hasStatic = state and hasStatic == hasStatic
        val hasNoStatic = state and hasNoStatic == hasNoStatic

        val inCinit = name.equals("<clinit>")
        val addInInit = name.equals("<init>") && hasAnnotation && hasNoStatic
        val addCinInit = inCinit && hasStatic
        val methodFind = hasAnnotation && (addInOnCreate || addInInit || addCinInit)
        val writeEnd = (addInInit || addCinInit) && addInOnCreate.not()

        if (methodFind) {
            val newMethodVisitor =
                object : AdviceAdapter(Opcodes.ASM9, oldMethodVisitor, access, name, descriptor) {
                    override fun onMethodEnter() {
                        super.onMethodEnter()
                        if (writeEnd.not()) {
                            write()
                        }
                    }

                    override fun onMethodExit(opcode: Int) {
                        super.onMethodExit(opcode)
                        if (writeEnd) {
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
                        if (inCinit) {
                            mv.visitFieldInsn(
                                GETSTATIC,
                                this@CoreClassNode.name,
                                "INSTANCE",
                                "L${this@CoreClassNode.name};"
                            )
                        } else {
                            mv.visitVarInsn(ALOAD, 0)
                        }
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
