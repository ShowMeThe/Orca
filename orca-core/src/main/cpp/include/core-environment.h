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
    jobject context;
    jobject getPackageInfo();

    jstring getPackageName();


public:

    environment(JNIEnv *jniEnv, jobject context);

    bool checkSignature();

    jobject getApplicationContext(jobject context);


    jobject getContext();

};




#endif //ORCA_CORE_ENVIRONMENT_H
