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



static std::string jstring2string(JNIEnv* env, jstring jStr) {
//    if (!jStr) return "";
//    jsize len = env->GetStringUTFLength(jStr);
//    std::string str(len, '\0');
//    env->GetStringUTFRegion(jStr, 0, len, &str[0]);
//    return str;
    if (!jStr) return "";
    const char *chars = env->GetStringUTFChars(jStr, nullptr);
    std::string str(chars);
    env->ReleaseStringUTFChars(jStr, chars);
    return str;
}


#endif //ORCA_CORE_UTIL_H
