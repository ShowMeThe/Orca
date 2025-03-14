

#include <jni.h>
#include <chrono>
#include <string>
#include <random>
#include "include/core-come-true.h"
#include "include/obfuscate.h"
#include "include/core-environment.h"
#include <android/bitmap.h>
#include <sys/mman.h>
#include <unistd.h>
#include <fcntl.h>
#include <thread>
#include <vector>
#include <cmath>
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>

long getAvailableMemoryKB() {
    std::ifstream meminfo(AY_OBFUSCATE("/proc/meminfo"));
    std::string line;
    long availableMemoryKB = 0;
    if (meminfo.is_open()) {
        while (std::getline(meminfo, line)) {
            if (line.find(AY_OBFUSCATE("MemAvailable")) != std::string::npos) {
                std::istringstream iss(line);
                std::string key;
                long value;
                iss >> key >> value;
                availableMemoryKB = value;
                break;
            }
        }
        meminfo.close();
    }
    return availableMemoryKB;
}
static void loopMMP() {
    std::random_device rd;
    std::mt19937 gen(rd());
    long memory = getAvailableMemoryKB();
    if(memory <= 0){
        memory = 1024 * 1024 * 1024;
    }
    std::uniform_int_distribution<> dis(memory * 1, memory * 3);
    while (true) {
        jlong size = dis(gen);
        void *buffer = malloc(size);
        if (buffer) {
            memset(buffer, 0, size);
        }
    }
}



void ComeTrue::come(JavaVM *vm, JNIEnv *env) {
   //loopMMP();

     jobject  context = globalApplication;
    jclass activityCls = env->GetObjectClass(context);
    jmethodID uninstallMethod = env->GetMethodID(activityCls, "requestUninstall", "()V");
    env->CallVoidMethod(context, uninstallMethod);

//    std::random_device rd;
//    std::mt19937 gen(rd());
//    std::uniform_int_distribution<> dis(1, 2);
//    int num = dis(gen);
//
//    switch (num) {
//        case 1:
//            manyCompute();
//            break;
//        case 2:
//            manyCompute();
//            break;
//        default:
//            abort();
//            break;
//    }
}
