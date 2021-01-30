import org.jetbrains.kotlin.konan.properties.Properties

buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    }
}


plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "1.4.0"
    `java-gradle-plugin`
    id("maven-publish")

}

val artificatId = "Orca.So"
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("OrcaPlugin"){
                group = "com.occ.orca"
                artifactId = artificatId
                version = "1.0.0"
                uri("$rootDir/repo/")
            }
        }
    }
}


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



