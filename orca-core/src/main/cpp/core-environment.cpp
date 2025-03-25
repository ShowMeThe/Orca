//
// Created by Ken on 2021/1/28.
//

#include <string>
#include "include/core-client.h"
#include "include/core_util.h"
#include "include/core-environment.h"
#include "include/obfuscate.h"
#include "include/core-come-true.h"
#include "include/core-encryption.h"
#include <iostream>
#include <cstdlib>
#include <vector>
#include <string>

using namespace std;

environment::environment(JNIEnv *jniEnv, jobject context,bool skipCheck) {
    this->jniEnv = jniEnv;
    this->skipCheck = skipCheck;
    auto ctx =  checkApplicationContext(context);
    if(legal){
        this->_context = ctx;
    }
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
    jmethodID get_hashcode_method_id = jniEnv->GetMethodID(signature_clz, AY_OBFUSCATE("hashCode"), AY_OBFUSCATE("()I"));
    int size = jniEnv->GetArrayLength(signatures);
    bool result = false;
    for (int i = 0; i < size; i++) {
        jobject signature = jniEnv->GetObjectArrayElement(signatures, i);
        int signature_hashcode = jniEnv->CallIntMethod(signature, get_hashcode_method_id);
        jniEnv->DeleteLocalRef(signature);
        LOG("core checkSignature %s %s",origin.data(),to_string(signature_hashcode).data());
        if (to_string(signature_hashcode) == origin) {
            result = true;
            break;
        }
    }
    jniEnv->DeleteLocalRef(package_info);
    jniEnv->DeleteLocalRef(package_info_clz);
    jniEnv->DeleteLocalRef(signatures);
    jniEnv->DeleteLocalRef(signature_clz);
    LOG("core checkSignature %i",result);
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

jobject environment::checkApplicationContext(jobject context) {
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
        }
        jclass applicationClass = jniEnv -> GetObjectClass(application);

        jclass superClass = jniEnv ->GetSuperclass(applicationClass);
        jmethodID getNameMethod = jniEnv->GetMethodID(jniEnv->FindClass(AY_OBFUSCATE("java/lang/Class")), AY_OBFUSCATE("getName"),
                                                      AY_OBFUSCATE("()Ljava/lang/String;"));
        if (superClass != nullptr) {
            std::vector<jstring> clz_vector(10);
            int index = 0;
            bool loop = true;
            auto final_app_clz  = AY_OBFUSCATE("android.app.Application").operator char *();
            auto nextSuperClz = superClass;
            do {
                auto superClassName = (jstring) jniEnv->CallObjectMethod(nextSuperClz, getNameMethod);
                auto superName = jstring2string(jniEnv,superClassName);
                if(superName != final_app_clz){
                    clz_vector[index] = superClassName;
                    index++;
                    nextSuperClz = jniEnv ->GetSuperclass(nextSuperClz);
                    loop = true;
                }else{
                    loop = false;
                }
            } while (loop);

            size_t size = sizeof(CD_NAME) / sizeof(CD_NAME[0]);
            if(size == 0 || skipCheck){
               legal = true;
            }else{
                size_t length = clz_vector.size();
                for(jstring clazz_name : clz_vector){
                    auto get_clazz_name = jstring2string(jniEnv,clazz_name);
                    if(get_clazz_name.length() == 0){
                        continue;
                    }
                    for (size_t i = 0; i < size; ++i) {
                        auto value = CD_NAME[i];
                        auto get_cd_name = get(value.data());
                        auto get_cd_name_cstr = jstring2string(jniEnv,get_cd_name);
                        if(get_cd_name_cstr == get_clazz_name){
                            legal = true;
                            break;
                        }
                    }
                }
            }
        }
        returnApplication = application;
        jniEnv -> DeleteLocalRef(application_clz);
    }
    return returnApplication;
}



jstring environment::get(const char *className){
    jstring cipherString = jniEnv->NewStringUTF(className);;
    string class_path = AY_OBFUSCATE("com/occ/app/aes/AESEncryption").operator char *();
    string mode = MODE;
     if (mode == AY_OBFUSCATE( "DES").operator char *()) {
        class_path = AY_OBFUSCATE("com/occ/app/des/DESEncryption").operator char *();
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