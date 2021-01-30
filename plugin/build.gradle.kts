import org.jetbrains.kotlin.konan.properties.Properties

buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.novoda:bintray-release:0.9.2")
    }
}


plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "1.4.0"
    `java-gradle-plugin`
     maven
}

val properties =  Properties()
properties.load(project.file("../local.properties").inputStream())

plugins.apply("com.novoda.bintray-release")
configure<com.novoda.gradle.release.PublishExtension> {
    userOrg = "yejiaken"
    repoName = "Orca"
    groupId = "com.occ.orca"
    artifactId = "orca.so"
    publishVersion = "1.0.0"
    desc = ""
    website = "https://github.com/ShowMeThe/Orca"
    bintrayUser = properties.getProperty("username")
    bintrayKey = properties.getProperty("password")
    dryRun = false
}


val parentDir = project.rootDir.path
val orca_core = file(parentDir + File.separator + "orca-core")
val orca_encrypt = file(parentDir + File.separator + "orca_encrypt")
val archivesBaseName = "orca.so"
task("zipNative",Zip::class){
    destinationDir = project.file("build/libs")
    archiveName  = "$archivesBaseName-1.0.0.jar"
    from(project.zipTree("build/libs/plugin.jar"))
    include("META-INF/**")
    include("com/**")
    from(orca_core.canonicalPath)
    include("src/main/**")
    exclude("CMakeLists.txt")
    exclude("src/main/AndroidManifest.xml")
    from(orca_encrypt.canonicalPath)
    include("src/main/**")
    exclude("src/main/AndroidManifest.xml")
}

tasks.getByName("jar").finalizedBy("zipNative")

repositories {
    mavenCentral()
    google()
    jcenter()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    compileOnly("com.android.tools.build:gradle:4.0.1")
    implementation("com.squareup:javapoet:1.13.0")
}

gradlePlugin {
    plugins {
        create("Orca.So") {
            id = "Orca.So"
            implementationClass = "com.occ.orca.OrcaPlugin"
        }
    }
}



