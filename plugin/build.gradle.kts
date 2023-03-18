import org.jetbrains.kotlin.konan.properties.Properties

buildscript {

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        //classpath("com.novoda:bintray-release:0.9.2")
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

val pVersion = "2.3.0"

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


dependencies {
    compileOnly(gradleApi())
    //implementation("androidx.annotation:annotation:1.3.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    compileOnly("com.android.tools.build:gradle:7.2.0")
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation("org.javassist:javassist:3.20.0-GA")
}

gradlePlugin {
    plugins {
        create("Orca") {
            id = "Orca"
            implementationClass = "com.occ.orca.OrcaPlugin"
        }
    }
}

kotlin{
    kotlinDslPluginOptions{
        jvmTarget.set("11")
    }
}

afterEvaluate {
    publishing{
        publications {
            create("release",MavenPublication::class.java){
                from(components.getAt("java"))
                groupId = "com.occ.orca"
                artifactId = "orca"
                version = pVersion
            }
        }
    }
}

/*plugins.apply("com.github.dcendents.android-maven")*/
//group = "com.occ.orca"
version = pVersion


