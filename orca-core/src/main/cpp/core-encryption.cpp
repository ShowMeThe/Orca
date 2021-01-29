//
// Created by Ken on 2021/1/29.
//

#include "include/core-encryption.h"


encryption::encryption(JNIEnv *jniEnv, jobject context) {
    this->jniEnv = jniEnv;
    this->context = context;
}

const char *encryption::decrypt(const char *key, const char *cipher_message) {

    jclass encrypt_clz = jniEnv->FindClass("com/occ/encrypt/AESEncryption");
    if (encrypt_clz != NULL) {
        jmethodID decrypt_method_id = jniEnv->GetStaticMethodID(encrypt_clz, "decrypt",
                                                                "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        if (decrypt_method_id != NULL) {
            jstring keyString = jniEnv->NewStringUTF(key);
            jstring cipherString = jniEnv->NewStringUTF(cipher_message);
            jstring result = (jstring) jniEnv->CallStaticObjectMethod(encrypt_clz,
                                                                      decrypt_method_id, keyString,
                                                                      cipherString);
            const char *resultChars = jniEnv->GetStringUTFChars(result, JNI_FALSE);
            jniEnv->DeleteLocalRef(keyString);
            jniEnv->DeleteLocalRef(cipherString);
            jniEnv->DeleteLocalRef(result);
            return resultChars;
        }
    }
    return NULL;
}