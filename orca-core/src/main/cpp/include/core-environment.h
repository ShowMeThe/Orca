//
// Created by ken on 2021/1/29.
//

#ifndef ORCA_CORE_ENVIRONMENT_H
#define ORCA_CORE_ENVIRONMENT_H

#include <jni.h>
#include "core-client.h"


class environment{

private:
    JNIEnv *jniEnv;
    jobject _context;
    bool _skip;
    bool _legal = false;
    jobject getPackageInfo();
    jstring getPackageName();
    jstring get(const char *className);

public:

    environment(JNIEnv *jniEnv, jobject context,bool skip);

    bool checkSignature();

    jobject checkApplicationContext(jobject context);

    jobject getContext();

};




#endif //ORCA_CORE_ENVIRONMENT_H
