//
// Created by Ken on 2021/1/29.
//

#include "include/core-encryption.h"
#include "include/core-client.h"
using namespace std;

encryption::encryption(JNIEnv *jniEnv, jobject context) {
    this->jniEnv = jniEnv;
    this->_context = context;
}

const char *encryption::decrypt(const char *key, const char *data) {
    string class_path = "com/occ/encrypt/AESEncryption";
    string mode = MODE;
    if(mode == "AES"){
        class_path = "com/occ/encrypt/aes/AESEncryption";
    }else if(mode == "DES"){
        class_path = "com/occ/encrypt/des/DESEncryption";
    }
    jclass encrypt_clz = jniEnv->FindClass(class_path.data());
    if (encrypt_clz != NULL) {
        jmethodID decrypt_method_id = jniEnv->GetStaticMethodID(encrypt_clz, "decrypt",
                                                                "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        if (decrypt_method_id != NULL) {
            jstring keyString = jniEnv->NewStringUTF(key);
            jstring cipherString = jniEnv->NewStringUTF(data);
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