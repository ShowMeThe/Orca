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
    jobject _context;

public:
    encryption(JNIEnv *jniEnv, jobject context);

    jstring decrypt(const char *key, const char *data);
};


#endif //ORCA_CORE_ENCRYPTION_H
