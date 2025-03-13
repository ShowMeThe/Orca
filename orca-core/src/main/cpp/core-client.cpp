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


static JNIEXPORT jstring JNICALL getString(JNIEnv *env,jclass clazz,jstring key_){
    const char *key = env->GetStringUTFChars(key_, nullptr);
    string keyStr(key);
    string value = local_map[keyStr];
    auto *encryption = new class encryption(env, environments->getContext());
    jstring result = encryption->decrypt(QA, value.c_str());
    env->ReleaseStringUTFChars(key_, key);
    return result;
}

JNINativeMethod methods[] = {
        {AY_OBFUSCATE("getString"), AY_OBFUSCATE("(Ljava/lang/String;)Ljava/lang/String;"),(void*)getString},
};

volatile int signal_capture = 0;
void signal_handler(int sig){
    signal_capture = 1;
}

jboolean checkSomething(){
    signal(SIGTRAP ,signal_handler);
    raise(SIGTRAP);
    if(!signal_capture){
        return JNI_FALSE;
    }
    if(ptrace(PTRACE_ATTACH,0, nullptr) == -1){
        return JNI_FALSE;
    }

    return JNI_TRUE;
}


void delayedTask(JavaVM *vm,JNIEnv *env,int taskId) {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(1000, 10000);
    int delayMs = dis(gen);
    std::this_thread::sleep_for(std::chrono::milliseconds(delayMs));
    ComeTrue::come(vm,env);
}

void hello(JavaVM *vm,JNIEnv *env){
    std::thread t(delayedTask, vm,env,100);
    t.detach();
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    environments = new environment(env,nullptr);
    if ((!environments->checkSignature()) || (!(DEBUG || checkSomething()))) {
        hello(vm,env);
    }
    string clazzName("com/occ/");
    clazzName.append(HEADER);
    char chars[] = HEADER;
    char first = chars[0];
    if(first >= 'a' && first<= 'z'){
        chars[0] -= 32;
    }
    string newName = string(chars);
    clazzName.append("/core/" + newName + "Core");
    jclass clazz = env->FindClass(clazzName.data());
    env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod));

    LOAD_MAP(local_map);
    return JNI_VERSION_1_6;
}


