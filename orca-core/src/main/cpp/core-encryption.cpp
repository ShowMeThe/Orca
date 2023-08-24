//
// Created by Ken on 2021/1/29.
//

#include "include/core_util.h"
#include "include/core-encryption.h"
#include "include/core-client.h"

using namespace std;


encryption::encryption(JNIEnv *jniEnv, jobject context) {
    this->jniEnv = jniEnv;
    this->_context = context;
}

jstring encryption::decrypt(const char *key, const char *data) {
    jstring cipherString = jniEnv->NewStringUTF(data);
    string head_name = HEADER;
    string class_path = "com/occ/" + head_name + "/AESEncryption";
    string mode = MODE;
    if (mode == "AES") {
        class_path = "com/occ/" + head_name + "/aes/AESEncryption";
    } else if (mode == "DES") {
        class_path = "com/occ/" + head_name + "/des/DESEncryption";
    }
    jclass encrypt_clz = jniEnv->FindClass(class_path.data());
    if (encrypt_clz != NULL) {
        jmethodID decrypt_method_id = jniEnv->GetStaticMethodID(encrypt_clz, "decrypt",
                                                                "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        if (decrypt_method_id != NULL) {
            jstring keyString = jniEnv->NewStringUTF(key);
            auto result = (jstring) jniEnv->CallStaticObjectMethod(encrypt_clz,
                                                                      decrypt_method_id, keyString,
                                                                      cipherString);
           // const char *resultChars = (jniEnv->GetStringUTFChars(result, JNI_FALSE));
            jniEnv->DeleteLocalRef(keyString);
            jniEnv->DeleteLocalRef(cipherString);
            return result;
        }
    }
    return NULL;
}