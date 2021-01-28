//
// Created by Ken on 2021/1/28.
//

#include <string>
#include "include/core-client.h"
#include "core_util.h"

using namespace std;

class environment {

    JNIEnv *jniEnv;
    jobject context;

    environment(JNIEnv *jniEnv, jobject context) {
        this->jniEnv = jniEnv;
        this->context = getApplicationContext(context);
    }


    bool checkSignature() {
        string origin(SIGNATURE)
        if (origin.empty()) {
            return true;
        }
        jobject package_info = getPackageInfo();
        jclass package_info_clz = jniEnv->GetObjectClass(package_info);
        jfieldID signatures_field_id = jniEnv->GetFieldID(package_info_clz, "signatures",
                                                          "[Landroid/content/pm/Signature;");
        auto signatures = (jobjectArray) jniEnv->GetObjectField(package_info,
                                                                        signatures_field_id);
        jclass signature_clz = jniEnv->FindClass("android/content/pm/Signature");
        jmethodID get_hashcode_method_id = jniEnv->GetMethodID(signature_clz, "hashCode", "()I");
        int size = jniEnv->GetArrayLength(signatures);
        bool result = false;
        for (int i = 0; i < size; i++) {
            jobject signature = jniEnv->GetObjectArrayElement(signatures, i);
            int signature_hashcode = jniEnv->CallIntMethod(signature, get_hashcode_method_id);
            jniEnv->DeleteLocalRef(signature);
            if (to_string(signature_hashcode).empty()) {
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


    jobject getPackageInfo() {
        jclass context_clz = jniEnv->GetObjectClass(context);
        jmethodID get_package_manager_method_id = jniEnv->GetMethodID(context_clz,
                                                                      "getPackageManager",
                                                                      "()Landroid/content/pm/PackageManager;");
        jobject package_manager = jniEnv->CallObjectMethod(context, get_package_manager_method_id);
        jclass package_manager_clz = jniEnv->GetObjectClass(package_manager);
        jmethodID get_package_info_method_id = jniEnv->GetMethodID(package_manager_clz,
                                                                   "getPackageInfo",
                                                                   "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
        jobject package_info = jniEnv->CallObjectMethod(package_manager, get_package_info_method_id,
                                                        getPackageName(), 64);
        jniEnv->DeleteLocalRef(context_clz);
        jniEnv->DeleteLocalRef(package_manager);
        jniEnv->DeleteLocalRef(package_manager_clz);
        return package_info;
    }

    jstring getPackageName() {
        jclass context_clz = jniEnv->GetObjectClass(context);
        jmethodID get_package_name_method_id = jniEnv->GetMethodID(context_clz,
                                                                   "getPackageName",
                                                                   "()Ljava/lang/String;");
        jstring packageName = (jstring) jniEnv->CallObjectMethod(context,
                                                                 get_package_name_method_id);
        jniEnv->DeleteLocalRef(context_clz);
        return packageName;
    }


    jobject getContext() {
        return context;
    }


    jobject getApplicationContext(jobject context) {
        jobject application = NULL;
        jclass application_clz = jniEnv->FindClass("android/app/ActivityThread");
        if (application_clz != NULL) {
            jmethodID current_application_method_id = jniEnv->GetStaticMethodID(application_clz,
                                                                                "currentApplication",
                                                                                "()Landroid/app/Application;");
            if (current_application_method_id != NULL) {
                application = jniEnv->CallStaticObjectMethod(application_clz,
                                                             current_application_method_id);
            }
            jniEnv->DeleteLocalRef(application_clz);
        }
        if (application == NULL) {
            application = context;
        }
        return application;
    }


};
