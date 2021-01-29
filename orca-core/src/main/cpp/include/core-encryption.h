//
// Created by Ken on 2021/1/29.
//

#ifndef ORCA_CORE_ENCRYPTION_H
#define ORCA_CORE_ENCRYPTION_H

#include <jni.h>
#include <string>

class encryption {

private:
    JNIEnv *jniEnv;
    jobject context;

public:
    encryption(JNIEnv *jniEnv, jobject context);

    const char *decrypt(const char *key, const char *cipher_message);
};


#endif //ORCA_CORE_ENCRYPTION_H
