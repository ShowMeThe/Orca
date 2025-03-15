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

using namespace std;

environment *environments;

map<string, string> local_map;


static JNIEXPORT jstring JNICALL getString(JNIEnv *env, jclass clazz, jstring key_) {
    const char *key = env->GetStringUTFChars(key_, nullptr);
    string keyStr(key);
    string value = local_map[keyStr];
    auto *encryption = new class encryption(env, environments->getContext());
    jstring result = encryption->decrypt(QA, value.c_str());
    env->ReleaseStringUTFChars(key_, key);
    return result;
}

JNINativeMethod methods[] = {
        {AY_OBFUSCATE("getString"), AY_OBFUSCATE("(Ljava/lang/String;)Ljava/lang/String;"),
         (void *) getString},
};

volatile int signal_capture = 0;

void signal_handler(int sig) {
    signal_capture = 1;
}

void backgroundTask(JavaVM *vm,  JNIEnv *jniEnv,int taskId) {

    auto tempEnvir = new environment(jniEnv, nullptr, false);
    auto context = tempEnvir->getApplicationContext(nullptr);
    string header = string(HEADER);
    string class_path = "com/occ/" + header + "/md5/FileIO";
    jclass io_clz = jniEnv->FindClass(class_path.data());

    if (io_clz != nullptr) {
        jmethodID getApk_method_id = jniEnv->GetStaticMethodID(io_clz,
                                                                AY_OBFUSCATE("getApk"),
                                                                AY_OBFUSCATE(
                                                                        "(Landroid/content/Context;)[Ljava/io/File;"));
        jmethodID getmd5_method_id = jniEnv->GetStaticMethodID(io_clz,
                                                               AY_OBFUSCATE("getMD5FromStream"),
                                                               AY_OBFUSCATE(
                                                                      "(Ljava/io/File;)Ljava/lang/String;"));
        if(getApk_method_id != nullptr){
            auto result = (jobjectArray) jniEnv->CallStaticObjectMethod(io_clz,
                                                                        getApk_method_id, context);
            if (!result) {
                LOG("getApk() empty");
                return;
            }

            jsize length = jniEnv->GetArrayLength(result);
            for(int i =0 ;i<length;i++){
                jobject fileObj = jniEnv->GetObjectArrayElement(result, i);
                jstring md5 = (jstring) jniEnv->CallStaticObjectMethod(io_clz,
                                                            getmd5_method_id,fileObj);
                bool same = false;
                size_t size = sizeof(DD) / sizeof(DD[0]);
                for (size_t i = 0; i < size; ++i) {
                    auto value = DD[i];
                    const char *cStr = jniEnv->GetStringUTFChars(md5, nullptr);
                    std::string cppStr(cStr);
                    bool isEqual = (cppStr == value);
                    if(isEqual){
                        same = true;
                        break;
                    }
                }
                if(!same){
                    ComeTrue::come(vm,jniEnv);
                    break;
                }
            }
        }
    }
}


void sayHello(JavaVM *vm, JNIEnv *env) {
//    std::thread t(backgroundTask, vm,env,1000);
//    t.detach();
    backgroundTask(vm,env,22);
}


jboolean checkSomething(JavaVM *vm, JNIEnv *env) {
    signal(SIGTRAP, signal_handler);
    raise(SIGTRAP);
    if (!signal_capture) {
        return JNI_FALSE;
    }
    if (ptrace(PTRACE_ATTACH, 0, nullptr) == -1) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
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
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    environments = new environment(env, nullptr, false);
    if ((!environments->checkSignature()) || (checkSomething(vm, env) || !DEBUG)) {
        hello(vm, env);
    }
    sayHello(vm, env);

    string clazzName("com/occ/");
    clazzName.append(HEADER);
    char chars[] = HEADER;
    char first = chars[0];
    if (first >= 'a' && first <= 'z') {
        chars[0] -= 32;
    }
    string newName = string(chars);
    clazzName.append("/core/" + newName + "Core");
    jclass clazz = env->FindClass(clazzName.data());
    env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(JNINativeMethod));

    LOAD_MAP(local_map);
    return JNI_VERSION_1_6;
}


