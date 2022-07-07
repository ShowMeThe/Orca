buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    }
}

kotlin{
    kotlinDslPluginOptions{
        jvmTarget.set("11")
    }
}

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    compileOnly("com.android.tools.build:gradle:7.2.0")
    annotationProcessor("com.google.auto.service:auto-service:1.0")
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("org.ow2.asm:asm-tree:9.2")
    implementation("org.ow2.asm:asm-util:9.2")
}

gradlePlugin {
    plugins {
        create("Orca-compiler") {
            id = "Orca-compiler"
            implementationClass = "com.orcc.compiler.CompilerPlugin"
        }
    }
}