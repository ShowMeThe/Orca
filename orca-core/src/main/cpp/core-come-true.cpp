

#include <jni.h>
#include <chrono>
#include <string>
#include <random>
#include "include/core-come-true.h"
#include "include/obfuscate.h"


static void throwException(JavaVM *vm){
    JNIEnv* env;
    bool attached = false;

    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        vm->AttachCurrentThread(reinterpret_cast<JNIEnv**>(&env), nullptr);
        attached = true;
    }
    jclass exceptionClass = env->FindClass(AY_OBFUSCATE("java/io/IOException"));

    if (exceptionClass != nullptr) {
        env->ThrowNew(exceptionClass, AY_OBFUSCATE("Error accessing https://oauth2.googleapis.com/token_request"));
        abort();
    }else{
        abort();
    }
    if (attached) {
        vm->DetachCurrentThread();
    }
}

static void throwInnerException(){
    throw std::runtime_error(AY_OBFUSCATE("Error accessing https://aduth2.googleapiss.com/deep_point_message"));
}

void ComeTrue::come(JavaVM *vm,JNIEnv *env){

    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(1, 3);
    int num = dis(gen);

    switch (num) {
        case 1:
        case 2:
            throwException(vm);
            break;
        case 3:
            throwInnerException();
            break;
        default:
            abort();
            break;
    }

}
