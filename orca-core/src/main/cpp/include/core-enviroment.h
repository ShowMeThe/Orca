//
// Created by Ken on 2021/1/29.
//

#ifndef ORCA_CORE_ENVIROMENT_H
#define ORCA_CORE_ENVIROMENT_H

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




#endif //ORCA_CORE_ENVIROMENT_H
