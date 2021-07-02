import org.jetbrains.kotlin.cfg.pseudocode.or
import org.jetbrains.kotlin.konan.properties.Properties

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.novoda:bintray-release:0.9.2")

    }
}


plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
     id("maven-publish")
}





val properties =  Properties()
properties.load(project.file("../local.properties").inputStream())
val pVersion = "2.0.0-release14"


val parentDir = project.rootDir.path
val orca_core = file(parentDir + File.separator + "orca-core")
val archivesBaseName = "Orca"
var jarFile = "build/libs/plugin-$pVersion.jar"
task("zipNative",Zip::class){
    destinationDirectory.set(project.file("build/libs"))
    archiveFileName.set("$archivesBaseName-${pVersion}.jarx")
    from(project.zipTree(jarFile))
    include("META-INF/**")
    include("com/**")
    from(orca_core.canonicalPath)
    include("src/main/**")
    exclude("CMakeLists.txt")
    exclude("src/main/AndroidManifest.xml")

    doLast {
        val originJar = project.file(jarFile)
        val xJar = project.file("build/libs/$archivesBaseName-${pVersion}.jarx")
        originJar.delete()
        xJar.renameTo(originJar)
    }
}


tasks.getByName("jar").finalizedBy("zipNative")

repositories {
    google()
    mavenCentral()
    jcenter()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    compileOnly("com.android.tools.build:gradle:4.0.1")
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.squareup:kotlinpoet:1.9.0")
}

gradlePlugin {
    plugins {
        create("Orca") {
            id = "Orca"
            implementationClass = "com.occ.orca.OrcaPlugin"
        }
    }
}


//plugins.apply("com.github.dcendents.android-maven")
group = "com.occ.orca"
version = pVersion


