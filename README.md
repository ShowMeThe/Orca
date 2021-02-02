  
   
   
   
   <p align="center"><a href="https://github.com/ShowMeThe/Orca" target="_blank"><img width="200"src="https://github.com/ShowMeThe/Orca/blob/master/logo_cover.png"></a></p>
   <h1 align="center">Orca.so</h1>
   <p align="center">A easy way to store secret string value data in .so file by adding plugin</p>
   
   <p align="center">
   <a href="https://github.com/ShowMeThe/Orca"><img src = "https://img.shields.io/badge/Project-Orca.So-orange"></a>
   <img src = "https://img.shields.io/badge/Verion-2.0.0%2B-blue"></a>
   </p>
   
   
   <p align = "center">
     <a href="https://github.com/ShowMeThe/Orca/blob/master/README-ZH.md">中文README</a>
   </p>
   
   ### How it works?
   
   Using the plugin , We can add C++ code into module build file, so that we can build it
   
   
  ### How to use it?
  
 1、 Add the following code into your project build.gradle
  ```gradle
  
  dependencies {
        classpath "com.occ.orca:orca.so:2.0.0-release06"
    }
  
  ```
 2、Add plguin into app or module build.gradle
 ```gradle
 plugins {
    id 'com.android.application'
    id 'Orca.So'
}
 
 ```
:boom::boom::boom:For Kotlin project please add the Orca.So before kotlin-android :exclamation:

For Kotlin Project:

```gradle
plugins {
    id 'com.android.application'
    id 'Orca.So'
    id 'kotlin-android'
}

```

#### Configuration

In your app or library module build.gradle, add the follow-like configs to save key-values.

```gradle

Orca.go{
        storeSet{
            "data"{
                value "123456"
            }
        }
        secretKey = "FTat46cvyia6758@243lid66wxzvwe23dgfhcfg76wsd@5as431aq1256dsaa211" //This is the default key , you must replace it
    }

```

### Call in your project(Java or Kotlin)

```kotlin

val data = CoreClient.getData()


```




 
