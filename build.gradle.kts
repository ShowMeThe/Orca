import java.net.URI

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    val kotlin_version by extra("1.4.10")
    rootProject.extra.apply {
        set("kotlin_version", "1.4.20")
    }
    repositories {
        maven{setUrl("https://maven.aliyun.com/nexus/content/groups/public/")}
        mavenCentral()
        google()
        maven { setUrl("https://jitpack.io") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:${rootProject.extra["kotlin_version"]}")
        //classpath("com.github.ShowMeThe.Orca:plugin:2.3.0")
        //classpath("com.github.ShowMeThe.Orca:orca-compiler:2.3.0")

    }

}
allprojects {
    repositories {
        maven{setUrl("https://maven.aliyun.com/nexus/content/groups/public/")}
        mavenCentral()
        google()
        maven { setUrl("https://jitpack.io") }
        apply(plugin = "maven-publish")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

