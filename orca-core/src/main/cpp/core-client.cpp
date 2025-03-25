#include <jni.h>
#include <string>


//
// Created by Ken on 2021/1/28.
//
#include "include/core_util.h"
#include "include/core-client.h"
#include "include/core-environment.h"
#include "include/core-encryption.h"
#include "include/core-come-true.h"
#include <thread>
#include <chrono>
#include <string>
#include <random>
#include <csignal>
#include <sys/ptrace.h>
#include <unistd.h>
#include "include/obfuscate.h"
#include <fstream>

using namespace std;

environment *environments;

map<string, string> local_map;

static JavaVM *gJvm = nullptr;

volatile int signal_capture = 0;
void signal_handler(int sig) {
    signal_capture = 1;
}


JNIEnv *getEnv() {
    JNIEnv *env;
    int status = gJvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        status = gJvm->AttachCurrentThread(&env, nullptr);
        if (status < 0) {
            return nullptr;
        }
    }
    return env;
}

jboolean checkSomething(JavaVM *vm, JNIEnv *env) {
    signal(SIGTRAP, signal_handler);
    raise(SIGTRAP);
    if (!signal_capture) {
        LOG("core in debug signal");
        return JNI_FALSE;
    }
    if (ptrace(PTRACE_ATTACH, 0, nullptr) == -1) {
        LOG("core in debug");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}


static JNIEXPORT jstring JNICALL getString(JNIEnv *env, jclass clazz, jstring key_) {
    const char *key = env->GetStringUTFChars(key_, nullptr);
    string keyStr(key);
    string value = local_map[keyStr];
    auto *encryption = new class encryption(env, environments->getContext());
    jstring result = encryption->decrypt(QA, value.c_str());
    env->ReleaseStringUTFChars(key_, key);
    return result;
}

static JNIEXPORT jboolean JNICALL check(JNIEnv *env,jclass clazz) {
    auto envir = new environment(env, nullptr,false);
    if ((!envir->checkSignature()) || (!DEBUG && !checkSomething(gJvm, env))) {
        return false;
    }
    return true;
}

JNIEXPORT void JNICALL see(JNIEnv *env, jclass clazz, jstring a, jstring b, jstring c) {
    LOG("see load");
    jclass dexClassLoaderClass = env->FindClass(AY_OBFUSCATE("dalvik/system/DexClassLoader"));
    if (dexClassLoaderClass == nullptr) {
        return;
    }
    jmethodID constructor = env->GetMethodID(
            dexClassLoaderClass,
    AY_OBFUSCATE("<init>"),
    AY_OBFUSCATE("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V"));
    if (constructor == nullptr) {
        return;
    }
    jstring jDexPath = a;
    jstring jOptimizedDir = b;
    jstring jApkPath = c;
    jstring jLibraryPath = env->NewStringUTF("");
    jobject parentClassLoader = nullptr;

    jobject dexClassLoader = env->NewObject(
            dexClassLoaderClass,
            constructor,
            jDexPath,
            jOptimizedDir,
            jLibraryPath,
            parentClassLoader
    );
    if(dexClassLoader == nullptr){
        return;
    }

    jclass targetClass = (jclass)env->CallObjectMethod(
            dexClassLoader,
            env->GetMethodID(env->FindClass(AY_OBFUSCATE("java/lang/ClassLoader")),
    AY_OBFUSCATE("loadClass"), AY_OBFUSCATE("(Ljava/lang/String;)Ljava/lang/Class;")),
            env->NewStringUTF(AY_OBFUSCATE("com.android.apksigner.ApkSignerTool"))
    );
    if(targetClass == nullptr){
        return;
    }

    jmethodID methodID = env->GetStaticMethodID(
            targetClass,
    AY_OBFUSCATE("verify"),
    AY_OBFUSCATE("(Z)Z")
    );
    if (methodID == nullptr) {
        env->DeleteLocalRef(targetClass);
        return;
    }

    auto jResult = env->CallStaticBooleanMethod(
            targetClass,
            methodID,
            JNI_FALSE
    );
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        env->DeleteLocalRef(targetClass);
        env->DeleteLocalRef(jApkPath);
        return;
    }
    LOG("background check %i",jResult == JNI_FALSE);
    if(jResult == JNI_FALSE){
        ComeTrue::come(gJvm,env);
    }
}


JNINativeMethod methods[] = {
        {AY_OBFUSCATE("getString"), AY_OBFUSCATE("(Ljava/lang/String;)Ljava/lang/String;"),
         (void *) getString},
};
JNINativeMethod check_methods[] = {
        {AY_OBFUSCATE("check"), AY_OBFUSCATE("()Z"),
         (void *) check},
};

JNINativeMethod check_sig_methods[] = {
        {AY_OBFUSCATE("see"), AY_OBFUSCATE("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"),
         (void *) see},
};

static jobject gClassLoader;
static jmethodID gFindClassMethod;

jclass findClass(const char *name) {
    return static_cast<jclass>(getEnv()->CallObjectMethod(gClassLoader, gFindClassMethod,
                                                          getEnv()->NewStringUTF(name)));
}

void files_delete(JNIEnv *jniEnv,jobjectArray array){
    jsize length = jniEnv->GetArrayLength(array);
    jclass file_clz = jniEnv->FindClass(AY_OBFUSCATE("java/io/File"));
    if(file_clz != nullptr){
        jmethodID delete_method = jniEnv->GetMethodID(file_clz, AY_OBFUSCATE("delete"),"()Z");
        if(delete_method != nullptr){
            for (int i = 0; i < length; i++) {
                jobject fileObj = jniEnv->GetObjectArrayElement(array, i);
                jniEnv->CallBooleanMethod(fileObj,delete_method);
            }
        }
        jniEnv->DeleteLocalRef(file_clz);
    }
}

bool isXposedClassLoaded(JNIEnv* env) {
    try{
        jclass xposedClass = env->FindClass(AY_OBFUSCATE("de/robv/android/xposed/XposedBridge"));
        if(env->ExceptionCheck()) {
            env->ExceptionClear();
            return false;
        }
        if (xposedClass != nullptr) {
            env->DeleteLocalRef(xposedClass);
            return true;
        }
    }catch (char *err){

    }
    return false;
}

bool isXposedLoaded() {
    std::ifstream maps(AY_OBFUSCATE("/proc/self/maps"));
    std::string line;
    while (std::getline(maps, line)) {
        if (line.find(AY_OBFUSCATE("XposedBridge.jar")) != std::string::npos ||
            line.find(AY_OBFUSCATE("libxposed.so")) != std::string::npos) {
            return true;
        }
    }
    return false;
}

bool isXposedPropertySet() {
    const char* prop = std::getenv(AY_OBFUSCATE("persist.sys.dalvik.vm.lib"));
    if (prop != nullptr && std::string(prop).find(AY_OBFUSCATE("xposed")) != std::string::npos) {
        return true;
    }
    return false;
}

bool isXposedInstallerPresent() {
    FILE* fp = popen(AY_OBFUSCATE("pm list packages"), "r");
    if (fp == nullptr) {
        return false;
    }
    char buffer[128];
    while (fgets(buffer, sizeof(buffer), fp) != nullptr) {
        if (strstr(buffer, AY_OBFUSCATE("de.robv.android.xposed.installer")) != nullptr) {
            pclose(fp);
            return true;
        }
    }
    pclose(fp);
    return false;
}

void startTask(JavaVM *vm) {

    JNIEnv *jniEnv = getEnv();
    if (jniEnv == nullptr) {
        return;
    }

    if(isXposedInstallerPresent() || isXposedPropertySet() || isXposedClassLoaded(jniEnv) || isXposedLoaded()){
        ComeTrue::come(gJvm,jniEnv);
        return;
    }

    auto temp = new environment(getEnv(), nullptr, true);
    auto context = temp->getContext();

    if (context == nullptr) {
        LOG("context == null");
        return;
    }

    jclass io_clz = findClass(AY_OBFUSCATE("com/occ/app/md5/FileIO"));
    if (io_clz != nullptr) {
        jmethodID getApk_method_id = jniEnv->GetStaticMethodID(io_clz,
                                                               AY_OBFUSCATE("getApk"),
                                                               AY_OBFUSCATE(
                                                                       "(Landroid/content/Context;)[Ljava/io/File;"));
        jmethodID getmd5_method_id = jniEnv->GetStaticMethodID(io_clz,
                                                               AY_OBFUSCATE("getMD5FromStream"),
                                                               AY_OBFUSCATE(
                                                                       "(Ljava/io/File;)Ljava/lang/String;"));


        if (getApk_method_id != nullptr) {
            auto result = (jobjectArray) jniEnv->CallStaticObjectMethod(io_clz,
                                                                        getApk_method_id, context);
            jsize length = jniEnv->GetArrayLength(result);
            for (int i = 0; i < length; i++) {
                jobject fileObj = jniEnv->GetObjectArrayElement(result, i);
                jstring md5 = (jstring) jniEnv->CallStaticObjectMethod(io_clz,
                                                                       getmd5_method_id, fileObj);
                bool same = false;
                size_t size = sizeof(DD) / sizeof(DD[0]);
                for (size_t i = 0; i < size; ++i) {
                    auto value = DD[i];
                    auto md5Str = jstring2string(jniEnv,md5);
                    bool isEqual = (md5Str == value);
                    if (isEqual) {
                        same = true;
                        break;
                    }
                }
                if (!same) {
                    LOG("core dex find not same");
                    ComeTrue::come(gJvm,jniEnv);
                    break;
                }
            }
            files_delete(jniEnv,result);
        }
    }
}

void sayHello(JavaVM *vm, JNIEnv *env) {
    jclass io_clz = env->FindClass(AY_OBFUSCATE("com/occ/app/md5/FileIO"));
    if(io_clz == nullptr){
        LOG("clazz not found");
        return;
    }
    jclass classClass = env->GetObjectClass(io_clz);

    auto classLoaderClass = env->FindClass(AY_OBFUSCATE("java/lang/ClassLoader"));
    auto getClassLoaderMethod = env->GetMethodID(classClass, AY_OBFUSCATE("getClassLoader"),
                                                 AY_OBFUSCATE("()Ljava/lang/ClassLoader;"));
    gClassLoader = env->NewGlobalRef(env->CallObjectMethod(io_clz, getClassLoaderMethod));
    gFindClassMethod = env->GetMethodID(classLoaderClass, AY_OBFUSCATE("findClass"),
                                        AY_OBFUSCATE("(Ljava/lang/String;)Ljava/lang/Class;"));

    std::thread t(startTask, vm);
    t.detach();
}

void delayedTask(JavaVM *vm, JNIEnv *env, int taskId) {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(100, 300);
    int delayMs = dis(gen);
    std::this_thread::sleep_for(std::chrono::milliseconds(delayMs));
    ComeTrue::come(vm, env);
}

void hello(JavaVM *vm, JNIEnv *env) {
    std::thread t(delayedTask, vm, env, 100);
    t.detach();
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    gJvm = vm;
    JNIEnv *env = getEnv();

    environments = new environment(env, nullptr, false);
    if ((!environments->checkSignature()) || (!DEBUG && !checkSomething(vm, env))) {
        LOG("core failed check");
        hello(vm, env);
    }

    sayHello(vm, env);

    string clazzName(AY_OBFUSCATE("com/occ/app/core/AppCore"));
    jclass clazz = env->FindClass(clazzName.data());
    env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(JNINativeMethod));
    env->RegisterNatives(clazz, check_methods, sizeof(check_methods) / sizeof(JNINativeMethod));
    env->RegisterNatives(clazz, check_sig_methods, sizeof(check_sig_methods) / sizeof(JNINativeMethod));

    LOAD_MAP(local_map);
    return JNI_VERSION_1_6;
}


