//
// Created by Ken on 2021/1/28.
//

#ifndef ORCA_CORE_UTIL_H
#define ORCA_CORE_UTIL_H

#include <jni.h>
#include <string>
#include <sstream>
#include <android/log.h>

#define LOG(...)__android_log_print(ANDROID_LOG_INFO, "Occ-core", __VA_ARGS__)

using namespace std;



static std::string jstring2string(JNIEnv* env, jstring jstr) {
    char* chars = (char *) env->GetStringChars(jstr, nullptr);
    auto size = env->GetStringLength(jstr);
    std::string str(chars);
    env->ReleaseStringChars(jstr, env->GetStringChars(jstr, nullptr));
    return str;
}


#endif //ORCA_CORE_UTIL_H
