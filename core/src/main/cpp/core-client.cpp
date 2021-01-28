#include <jni.h>


//
// Created by Ken on 2021/1/28.
//




jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

//    if (!environments->check()) {
//        return JNI_ERR;
//    }
    return JNI_VERSION_1_6;
}

extern "C" jstring
Java_com_orcc_core_CoreClient_getString(JNIEnv *env, jobject thiz) {

    return env->NewStringUTF("1232");
}