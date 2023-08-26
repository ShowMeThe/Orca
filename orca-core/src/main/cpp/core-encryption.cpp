//
// Created by Ken on 2021/1/29.
//

#include "include/core_util.h"
#include "include/core-encryption.h"
#include "include/core-client.h"

using namespace std;

map<string, jobject> mKeyMap;

encryption::encryption(JNIEnv *jniEnv, jobject context) {
    this->jniEnv = jniEnv;
    this->_context = context;
}

jstring encryption::decrypt(const char *key, const char *data) {
    jstring cipherString = jniEnv->NewStringUTF(data);
    string storeKey = jstring2string(jniEnv,cipherString);
    if(CD){
        if (mKeyMap[storeKey] != nullptr) {
            return (jstring) mKeyMap[storeKey];
        }
    }
    string header = string(HEADER);
    string class_path = "com/occ/" + header + "/AESEncryption";
    string mode = MODE;
    if (mode == "AES") {
        class_path = "com/occ/" + header + "/aes/AESEncryption";
    } else if (mode == "DES") {
        class_path = "com/occ/" + header + "/des/DESEncryption";
    }
    jclass encrypt_clz = jniEnv->FindClass(class_path.data());
    if (encrypt_clz != nullptr) {
        jmethodID decrypt_method_id = jniEnv->GetStaticMethodID(encrypt_clz, "decrypt",
                                                                "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        if (decrypt_method_id != nullptr) {
            jstring keyString = jniEnv->NewStringUTF(key);
            auto result = (jstring) jniEnv->CallStaticObjectMethod(encrypt_clz,
                                                                   decrypt_method_id, keyString,
                                                                   cipherString);
//            char *resultChars = const_cast<char *>(jniEnv->GetStringUTFChars(result, JNI_FALSE));
            jniEnv->DeleteLocalRef(keyString);
            jniEnv->DeleteLocalRef(cipherString);
            if(CD){
                auto globalRef = jniEnv->NewGlobalRef(result);
                mKeyMap[storeKey] = globalRef;
                jniEnv->DeleteLocalRef(result);
                return (jstring) globalRef;
            }else{
                return result;
            }
        }
    }
    return nullptr;
}

