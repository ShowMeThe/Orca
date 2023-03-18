#include <jni.h>
#include <string>


//
// Created by Ken on 2021/1/28.
//
#include "include/core_util.h"
#include "include/core-client.h"
#include "include/core-environment.h"
#include "include/core-encryption.h"
#include <string>

using namespace std;

environment *environments;

map<string, string> local_map;

extern "C"
JNIEXPORT jstring JNICALL getString(JNIEnv *env,jclass clazz,jstring key_){
    const char *key = env->GetStringUTFChars(key_, nullptr);
    string keyStr(key);
    string value = local_map[keyStr];
    auto *encryption = new class encryption(env, environments->getContext());
    const char *result = encryption->decrypt(QA, value.c_str());
    env->ReleaseStringUTFChars(key_, key);
    return env->NewStringUTF(result);
}

JNINativeMethod methods[] = {
        { "getString", "(Ljava/lang/String;)Ljava/lang/String;",(void*)getString},
};


jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    environments = new environment(env,nullptr);
    if (!environments->checkSignature()) {
        return JNI_ERR;
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
    LOG("%s",clazzName.data());
    jclass clazz = env->FindClass(clazzName.data());
    env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod));

    LOAD_MAP(local_map);
    return JNI_VERSION_1_6;
}





