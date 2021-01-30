// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    val kotlin_version by extra("1.4.10")
    rootProject.extra.apply {
        set("kotlin_version", "1.4.20")
    }
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProject.extra["kotlin_version"]}")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:${rootProject.extra["kotlin_version"]}")
    }

}
allprojects {
    repositories {
        google()
        jcenter()
        maven { setUrl("https://jitpack.io") }

    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

