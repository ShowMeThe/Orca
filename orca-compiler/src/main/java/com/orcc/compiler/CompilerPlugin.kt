package com.orcc.compiler

import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode


var projectName = "app"
var goCompiler: GoCompiler? = null

class CompilerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        goCompiler =  GoCompiler(project)
        project.extensions.add("WhiteList", requireNotNull(goCompiler))

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
        return requireNotNull(goCompiler).includes.any { classData.className.contains(it) }
    }
}


class CoreClassNode(private val nextVisitor: ClassVisitor) : ClassNode(Opcodes.ASM9) {

    private var hasAnnotation = false
    private val staticField = arrayListOf<FieldVisitor>()
    private val notStaticField = arrayListOf<FieldVisitor>()

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        val oldVisitor = super.visitField(access, name, descriptor, signature, value) as FieldNode
        return oldVisitor
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        fields.forEach {
            if(it.visibleAnnotations.isNullOrEmpty().not()){
                it.visibleAnnotations.forEach { annotationNode ->
                    if(annotationNode.desc.contains(
                            "CoreDecryption",
                            true
                        )){
                        hasAnnotation = true
                        if (it.access and Opcodes.ACC_STATIC == Opcodes.ACC_STATIC) {
                            staticField.add(it)
                        }else{
                            notStaticField.add(it)
                        }
                    }
                }
            }
        }
        println("method == name:${name} descriptor = :${descriptor}")
        val oldMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val addInOnCreate = name.equals("onCreate")
        val addInInit = name.equals("<init>") && notStaticField.isNotEmpty()
        val addCinInit = name.equals("<clinit>") && staticField.isNotEmpty()
        val methodFind = hasAnnotation && (addInOnCreate || addInInit || addCinInit)
        val writeEnd = (addInInit || addCinInit) && addInOnCreate.not()
        if (methodFind) {
            val inCinit = name.equals("<clinit>")
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
                        if(inCinit){
                            println("class name = $name")
                            mv.visitFieldInsn(GETSTATIC, this@CoreClassNode.name, "INSTANCE", "L${this@CoreClassNode.name};")
                        }else{
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
