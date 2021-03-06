  
   
   
   
   <p align="center"><a href="https://github.com/ShowMeThe/Orca" target="_blank"><img width="200"src="https://github.com/ShowMeThe/Orca/blob/master/logo_cover.png"></a></p>
   <h1 align="center">Orca.so</h1>
   <p align="center">简单的利用plugin添加C++代码实现把需要加密的字符串存储在.So文件内</p>
   
   <p align="center">
   <a href="https://github.com/ShowMeThe/Orca"><img src = "https://img.shields.io/badge/Project-Orca.So-orange"></a>
   <img src = "https://img.shields.io/badge/Verion-2.0.0%2B-blue"></a>
   </p>
   
   
   <p align = "center">
     <a href="https://github.com/ShowMeThe/Orca/edit/master/README.md">英文README</a>
   </p>
   
   ### 如何实现?
   
   利用gradle plugin实现C++的代码拷贝和环境配置，把加密内容添加到C++的头文件生成对应的.So文件，并对应生成JAVA的访问类
   
   
  ### 如何使用?
   
 1、 先在项目的build.gradle的buildscript下的dependencies添加以下内容:
  ```gradle
  
    //根目录的build.gradle
    buildscript {
  		repositories {
  			...
  			maven { url 'https://jitpack.io' }
  		}
  	}

    dependencies {
          classpath "com.github.ShowMeThe:Orca:2.0.0-release09"
      }
  
  ```
 2、进入你的项目build.gradle添加plugin的配置，能独立兼容:app项目及:Library项目，但会生成各自不同的.So，建议把加密的内容添加到单独Library中，让主项目引入此Library
 ```gradle
 plugins {
    id 'com.android.application'
    id 'Orca' //记得大写
}
 
 ```
:boom::boom::boom:提示 Kotlin项目需要如下配置 :exclamation:

For Kotlin Project:

```gradle
plugins {
    id 'com.android.application'
    id 'Orca'
    id 'kotlin-android'
}

```

#### :app和:library的build.gradle进行配置

```gradle

Orca.go{
        storeSet{
            "data"{
                value "123456"
            }
        }
        encryptMode = "AES" or "DES" 选择不同的加密方法，不区分大小写
        isDebug = true // isDebug 为true时候，将会跳过判断signature的处理，默认为false
	signature = "your .jks signature" 你的jks签名
        secretKey = "FTat46cvyia6758@243lid66wxzvwe23dgfhcfg76wsd@5as431aq1256dsaa211" //默认加密的加密秘钥
    }

```

### 在项目中调用CoreClient解密后的内容

```kotlin

val data = CoreClient.getData()


```




 
