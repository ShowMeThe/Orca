import java.net.URI

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    val kotlin_version by extra("1.4.10")
    rootProject.extra.apply {
        set("kotlin_version", "1.4.20")
    }
    repositories {
        google()
        mavenCentral()
        maven{setUrl("https://maven.aliyun.com/nexus/content/groups/public/")}
        maven { setUrl("https://jitpack.io") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProject.extra["kotlin_version"]}")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:${rootProject.extra["kotlin_version"]}")
        //classpath("com.github.ShowMeThe:Orca:2.0.0-release11")
    }

}
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven{setUrl("https://maven.aliyun.com/nexus/content/groups/public/")}
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

