#include <jni.h>
#include <string>


//
// Created by Ken on 2021/1/28.
//
#include "include/core_util.h"
#include "include/core-client.h"
#include "include/core-enviroment.h"
#include <string>

using namespace std;

environment *environments;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    environments = new environment(env,nullptr);
    if (!environments->checkSignature()) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}

extern "C" jstring
Java_com_orcc_core_CoreClient_getString(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(SIGNATURE);
}