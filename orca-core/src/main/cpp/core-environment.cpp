//
// Created by Ken on 2021/1/28.
//

#include <string>
#include "include/core-client.h"
#include "include/core_util.h"
#include "include/core-environment.h"
#include "include/obfuscate.h"
#include "include/core-encryption.h"
#include <iostream>
#include <cstdlib>
using namespace std;

environment::environment(JNIEnv *jniEnv, jobject context) {
    this->jniEnv = jniEnv;
    this->_context = getApplicationContext(context);
}


bool environment::checkSignature() {
    string origin;
    origin = CA;
    if(_context == nullptr){
        return false;
    }
    if (origin.empty() && DEBUG) {
        return true;
    }
    jobject package_info = getPackageInfo();
    jclass package_info_clz = jniEnv->GetObjectClass(package_info);
    jfieldID signatures_field_id = jniEnv->GetFieldID(package_info_clz, AY_OBFUSCATE("signatures"),
    AY_OBFUSCATE("[Landroid/content/pm/Signature;"));
    auto signatures = (jobjectArray) jniEnv->GetObjectField(package_info,
                                                            signatures_field_id);
    jclass signature_clz = jniEnv->FindClass(AY_OBFUSCATE("android/content/pm/Signature"));
    jmethodID get_hashcode_method_id = jniEnv->GetMethodID(signature_clz, AY_OBFUSCATE("hashCode"), "()I");
    int size = jniEnv->GetArrayLength(signatures);
    bool result = false;
    for (int i = 0; i < size; i++) {
        jobject signature = jniEnv->GetObjectArrayElement(signatures, i);
        int signature_hashcode = jniEnv->CallIntMethod(signature, get_hashcode_method_id);
        jniEnv->DeleteLocalRef(signature);
        if (to_string(signature_hashcode) == origin) {
            result = true;
            break;
        }
    }
    jniEnv->DeleteLocalRef(package_info);
    jniEnv->DeleteLocalRef(package_info_clz);
    jniEnv->DeleteLocalRef(signatures);
    jniEnv->DeleteLocalRef(signature_clz);
    return result;
}


jobject environment::getPackageInfo() {
    jclass context_clz = jniEnv->GetObjectClass(_context);
    jmethodID get_package_manager_method_id = jniEnv->GetMethodID(context_clz,
    AY_OBFUSCATE("getPackageManager"),
    AY_OBFUSCATE("()Landroid/content/pm/PackageManager;"));
    jobject package_manager = jniEnv->CallObjectMethod(_context, get_package_manager_method_id);
    jclass package_manager_clz = jniEnv->GetObjectClass(package_manager);
    jmethodID get_package_info_method_id = jniEnv->GetMethodID(package_manager_clz,
    AY_OBFUSCATE("getPackageInfo"),
    AY_OBFUSCATE("(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;"));
    jobject package_info = jniEnv->CallObjectMethod(package_manager, get_package_info_method_id,
                                                    getPackageName(), 64);
    jniEnv->DeleteLocalRef(context_clz);
    jniEnv->DeleteLocalRef(package_manager);
    jniEnv->DeleteLocalRef(package_manager_clz);
    return package_info;
}


jstring environment::getPackageName() {
    jclass context_clz = jniEnv->GetObjectClass(_context);
    jmethodID get_package_name_method_id = jniEnv->GetMethodID(context_clz,
    AY_OBFUSCATE("getPackageName"),
    AY_OBFUSCATE("()Ljava/lang/String;"));
    jstring packageName = (jstring) jniEnv->CallObjectMethod(_context,
                                                             get_package_name_method_id);
    jniEnv->DeleteLocalRef(context_clz);
    return packageName;
}


jobject environment::getContext() {
    return _context;
}

jobject environment::getApplicationContext(jobject context) {
    jobject application = nullptr;
    jobject returnApplication = nullptr;
    jclass application_clz = jniEnv->FindClass(AY_OBFUSCATE("android/app/ActivityThread"));
    if (application_clz != nullptr) {
        jmethodID current_application_method_id = jniEnv->GetStaticMethodID(application_clz,
        AY_OBFUSCATE("currentApplication"),
        AY_OBFUSCATE("()Landroid/app/Application;"));
        if (current_application_method_id != nullptr) {
            application = jniEnv->CallStaticObjectMethod(application_clz,
                                                         current_application_method_id);
            globalApplication = jniEnv->NewGlobalRef(application);
        }
        if(CD_NAME->empty()){
            returnApplication = application;
        }else{
            jclass applicationClass = jniEnv -> GetObjectClass(application);
            jclass superClass = jniEnv ->GetSuperclass(applicationClass);
            if (superClass != nullptr) {
                jmethodID getNameMethod = jniEnv->GetMethodID(jniEnv->FindClass(AY_OBFUSCATE("java/lang/Class")), AY_OBFUSCATE("getName"),
                AY_OBFUSCATE("()Ljava/lang/String;"));
                auto superClassName = (jstring) jniEnv->CallObjectMethod(superClass, getNameMethod);
                const char* superClassNameStr = jniEnv ->GetStringUTFChars(superClassName, nullptr);

                size_t size = sizeof(CD_NAME) / sizeof(CD_NAME[0]);
                for (size_t i = 0; i < size; ++i) {
                    auto value = CD_NAME[i];
                    auto result = jstring2string(jniEnv,get(value.c_str()));
                    if(result == jstring2string(jniEnv,superClassName)){
                        returnApplication = application;
                        break;
                    }
                }
            }
        }
        jniEnv -> DeleteLocalRef(application_clz);
    }
    return returnApplication;
}



jstring environment::get(const char *className){
    jstring cipherString = jniEnv->NewStringUTF(className);;
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
        jmethodID decrypt_method_id = jniEnv->GetStaticMethodID(encrypt_clz,
                                                                AY_OBFUSCATE("decrypt"),
                                                                AY_OBFUSCATE(
                                                                        "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"));
        if (decrypt_method_id != nullptr) {
            jstring keyString = jniEnv->NewStringUTF(QA);
            auto result = (jstring) jniEnv->CallStaticObjectMethod(encrypt_clz,
                                                                   decrypt_method_id, keyString,
                                                                   cipherString);
            jniEnv->DeleteLocalRef(keyString);
            jniEnv->DeleteLocalRef(cipherString);
            return result;
        }
    }
    return nullptr;
}
