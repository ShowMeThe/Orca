import org.jetbrains.kotlin.konan.properties.Properties

buildscript {

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    }

}

repositories {
    google()
    mavenCentral()
}


plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
     id("maven-publish")
}


val properties =  Properties()
properties.load(project.file("../local.properties").inputStream())

val pVersion = "2.4.2.2"

val parentDir = project.rootDir.path
val archivesBaseName = "orca-plugin"
val orca_core = file(parentDir + File.separator + "orca-core")
var jarFile = "build/libs/${archivesBaseName}-${pVersion}.jar"
println("orca_core path = $orca_core")
task("zipNative",Zip::class){
    destinationDirectory.set(project.file("build/libs"))
    archiveFileName.set("$archivesBaseName-$pVersion.jarx")
    from(project.zipTree(jarFile))
    include("META-INF/**")
    include("com/**")
    from(orca_core.path)
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


tasks.getByName("inspectClassesForKotlinIC").finalizedBy("zipNative")


dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    compileOnly("com.android.tools.build:gradle:8.1.2")
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation("org.javassist:javassist:3.20.0-GA")
    annotationProcessor("com.google.auto.service:auto-service:1.0")
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("org.ow2.asm:asm-tree:9.2")
    implementation("org.ow2.asm:asm-util:9.2")
}

gradlePlugin {
    plugins {
        create("Orca-core") {
            id = "Orca-core"
            implementationClass = "com.occ.orca.OrcaPlugin"
        }
        create("Orca-compiler") {
            id = "Orca-compiler"
            implementationClass = "com.occ.orca.CompilerPlugin"
        }
    }
}

kotlin{
    jvmToolchain(17)
}

afterEvaluate {
    publishing{
        publications {
            create("release",MavenPublication::class.java){
                from(components.getAt("java"))
                groupId = "com.occ.orca"
                artifactId = "orca-core"
            }
        }
    }
}

version = pVersion