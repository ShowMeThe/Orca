buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    }
}

kotlin{
    jvmToolchain(17)
}

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("maven-publish")
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    compileOnly("com.android.tools.build:gradle:8.1.2")
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
            implementationClass = "com.occ.compiler.CompilerPlugin"
        }
    }
}

afterEvaluate {
    publishing{
        publications {
            create("release",MavenPublication::class.java){
                from(components.getAt("java"))
                groupId = "com.occ.orca"
                artifactId = "orca-compiler"
                version = "1.0.0"
            }
        }
    }
}
