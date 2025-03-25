//
// Created by Ken on 2021/1/29.
//

#include "include/core_util.h"
#include "include/core-encryption.h"
#include "include/core-client.h"
#include "include/obfuscate.h"

using namespace std;

map<string, jobject> mKeyMap;

encryption::encryption(JNIEnv *jniEnv, jobject context) {
    this->jniEnv = jniEnv;
    this->_context = context;

}

jstring encryption::decrypt(const char *key, const char *data) {
    jstring cipherString = jniEnv->NewStringUTF(data);
    string storeKey = jstring2string(jniEnv, cipherString);
    if (CD) {
        if (mKeyMap[storeKey] != nullptr) {
            return (jstring) mKeyMap[storeKey];
        }
    }

    string class_path = AY_OBFUSCATE("com/occ/app/aes/AESEncryption").operator char *();
    string mode = MODE;
    if (mode == AY_OBFUSCATE("DES").operator char *()) {
        class_path = AY_OBFUSCATE("com/occ/app/des/DESEncryption").operator char *();
    }
    jclass encrypt_clz = jniEnv->FindClass(class_path.data());
    if (encrypt_clz != nullptr) {
        jmethodID decrypt_method_id = jniEnv->GetStaticMethodID(encrypt_clz,
                                                                AY_OBFUSCATE("decrypt"),
                                                                AY_OBFUSCATE(
                                                                        "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"));
        if (decrypt_method_id != nullptr) {
            jstring keyString = jniEnv->NewStringUTF(key);
            auto result = (jstring) jniEnv->CallStaticObjectMethod(encrypt_clz,
                                                                   decrypt_method_id, keyString,
                                                                   cipherString);
            jniEnv->DeleteLocalRef(keyString);
            jniEnv->DeleteLocalRef(cipherString);
            if (CD) {
                auto globalRef = jniEnv->NewGlobalRef(result);
                mKeyMap[storeKey] = globalRef;
                jniEnv->DeleteLocalRef(result);
                return (jstring) globalRef;
            } else {
                return result;
            }
        }
    }
    return nullptr;
}

