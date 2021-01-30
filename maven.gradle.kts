
buildscript {

    repositories {
        google()
        jcenter()
    }

    dependencies{
        classpath("com.novoda:bintray-release:0.9.2")
    }

}
plugins{
    id("com.novoda.bintray-release")
}

configure<com.novoda.gradle.release.PublishExtension>{
    userOrg = "Orca"
    groupId = "com.occ"
    artifactId = "orca-release"
    publishVersion = "1.0.0"
    desc = "A easy way to store secure message in .so library"
    website = "https://github.com/ShowMeThe/Orca"
}
val parentDir = project.rootDir.path
val orca_core = file(parentDir + File.separator + "orca-core")
val orca_encrypt = file(parentDir + File.separator + "orca_encrypt")


val archivesBaseName = "orca.so"
task("zipNative",Zip::class){
    destinationDir = project.file("build/libs")
    archiveName  = "$archivesBaseName-1.0.jar"
    from(project.zipTree("plugin/build/libs/plugin.jar"))
    include("META-INF/**")
    include("com/**")
    from(orca_core.canonicalPath)
    include("src/main/**")
    include("CMakeLists.txt")
    exclude("src/main/AndroidManifest.xml")
    from(orca_encrypt.canonicalPath)
    include("src/main/**")
    exclude("src/main/AndroidManifest.xml")
}