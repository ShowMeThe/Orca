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
        classpath("com.android.tools.build:gradle:7.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:${rootProject.extra["kotlin_version"]}")
        classpath("com.github.ShowMeThe.Orca:plugin:2.3.0")
        classpath("com.github.ShowMeThe.Orca:orca-compiler:2.3.0")
    }

}
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven{setUrl("https://maven.aliyun.com/nexus/content/groups/public/")}
        apply(plugin = "maven-publish")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

