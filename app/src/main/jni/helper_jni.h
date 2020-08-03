#ifndef ANDROID_HELPER_JNI_H
#define ANDROID_HELPER_JNI_H

#include <string>
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

std::string fdLoadFile(const std::string& path);
char* jstring2string(JNIEnv* env, jstring jstr);
jstring string2jstring(JNIEnv* env, const char* pat);

#ifdef __cplusplus
}
#endif
#endif //ANDROID_HELPER_JNI_H
