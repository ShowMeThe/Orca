

#include <jni.h>
#include <chrono>
#include <string>
#include <random>
#include "include/core-come-true.h"
#include "include/obfuscate.h"
#include "include/core-environment.h"
#include "include/core_util.h"
#include <android/bitmap.h>
#include <sys/mman.h>
#include <unistd.h>
#include <fcntl.h>
#include <thread>
#include <vector>
#include <cmath>
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>

void startUninstall(JavaVM *vm){
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        vm->AttachCurrentThread(reinterpret_cast<JNIEnv**>(&env), nullptr);
    }

    jobject application = nullptr;
    jobject returnApplication = nullptr;
    jclass application_clz = env->FindClass(AY_OBFUSCATE("android/app/ActivityThread"));
    if (application_clz != nullptr) {
        jmethodID current_application_method_id = env->GetStaticMethodID(application_clz,
                                                                         AY_OBFUSCATE("currentApplication"),
                                                                         AY_OBFUSCATE("()Landroid/app/Application;"));
        if (current_application_method_id != nullptr) {
            application = env->CallStaticObjectMethod(application_clz,
                                                      current_application_method_id);
            jclass contextClass = env->GetObjectClass(application);

            jmethodID startActivityMethod = env->GetMethodID(contextClass, AY_OBFUSCATE("startActivity"), AY_OBFUSCATE("(Landroid/content/Intent;)V"));

            jclass intentClass = env->FindClass(AY_OBFUSCATE("android/content/Intent"));
            jmethodID intentConstructor = env->GetMethodID(intentClass, AY_OBFUSCATE("<init>"), AY_OBFUSCATE("(Ljava/lang/String;)V"));
            jstring actionDelete = env->NewStringUTF(AY_OBFUSCATE("android.intent.action.DELETE"));
            jobject intent = env->NewObject(intentClass, intentConstructor, actionDelete);

            jclass uriClass = env->FindClass(AY_OBFUSCATE("android/net/Uri"));
            jmethodID parseMethod = env->GetStaticMethodID(uriClass, AY_OBFUSCATE("parse"), AY_OBFUSCATE("(Ljava/lang/String;)Landroid/net/Uri;"));


            jmethodID getPackageName = env->GetMethodID(contextClass, AY_OBFUSCATE("getPackageName"), AY_OBFUSCATE("()Ljava/lang/String;"));
            jstring packageName = (jstring) env->CallObjectMethod(application, getPackageName);
            const char* packageNameCStr = env->GetStringUTFChars(packageName, nullptr);

            std::string packageUriStr = AY_OBFUSCATE( "package:").operator char *() + std::string(packageNameCStr);
            env->ReleaseStringUTFChars(packageName, packageNameCStr);

            jstring packageUri = env->NewStringUTF(packageUriStr.c_str());
            jobject uri = env->CallStaticObjectMethod(uriClass, parseMethod, packageUri);


            jmethodID setDataMethod = env->GetMethodID(intentClass, AY_OBFUSCATE("setData"), AY_OBFUSCATE("(Landroid/net/Uri;)Landroid/content/Intent;"));
            env->CallObjectMethod(intent, setDataMethod, uri);


            jfieldID flagNewTaskField = env->GetStaticFieldID(intentClass, AY_OBFUSCATE("FLAG_ACTIVITY_NEW_TASK"), AY_OBFUSCATE("I"));
            jint flagNewTask = env->GetStaticIntField(intentClass, flagNewTaskField);
            jmethodID addFlagsMethod = env->GetMethodID(intentClass, AY_OBFUSCATE("addFlags"), AY_OBFUSCATE("(I)Landroid/content/Intent;"));
            env->CallObjectMethod(intent, addFlagsMethod, flagNewTask);


            env->CallVoidMethod(application, startActivityMethod, intent);

            abort();

        }

    }

}


static void loopMMP(JavaVM *vm) {
    std::random_device rd;
    std::mt19937 gen(rd());
    long memory = 1280 * 1024 * 1024;
    std::uniform_int_distribution<> dis(memory * 1, memory * 2);
    jint index = 0;
    while (true) {
        jlong size = dis(gen);
        void *buffer = malloc(size);
        if (buffer) {
            index++;
            memset(buffer, 0, size);
            if(index >= 8){
                startUninstall(vm);
                break;
            }
        }
    }
}



void ComeTrue::come(JavaVM *vm, JNIEnv *env) {
    LOG("start come true");
    loopMMP(vm);
}
