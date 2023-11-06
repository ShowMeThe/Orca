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

val pVersion = "2.4.0"

val parentDir = project.rootDir.parentFile.path
val orca_core = file(parentDir + File.separator + "orca-core")
val archivesBaseName = "orca-plugin"
var jarFile = "build/libs/${archivesBaseName}-${pVersion}.jar"
println("orca_core path = $orca_core")
task("zipNative",Zip::class){
    destinationDirectory.set(project.file("build/libs"))
    archiveFileName.set("$archivesBaseName-$pVersion.jarx")
    from(project.zipTree(jarFile))
    include("META-INF/**")
    include("com/**")
    from(orca_core)
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


tasks.getByName("assemble").finalizedBy("zipNative")


dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    compileOnly("com.android.tools.build:gradle:8.1.2")
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation("org.javassist:javassist:3.20.0-GA")
}

gradlePlugin {
    plugins {
        create("Orca-core") {
            id = "Orca-core"
            implementationClass = "com.occ.orca.OrcaPlugin"
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