#include "helper_jni.h"
#include <fstream>

// Helper functions
std::string fdLoadFile(const std::string& path) {
    std::ifstream file(path, std::ios::in);
    if (file.is_open()) {
        file.seekg(0, file.end);
        int size = file.tellg();
        char *content = new char[size];

        file.seekg(0, file.beg);
        file.read(content, size);
        std::string fileContent;
        fileContent.assign(content, size);
        delete [] content;
        file.close();
        return fileContent;
    } else {
        return "";
    }
}

char* jstring2string(JNIEnv* env, jstring jstr)
{
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

jstring string2jstring(JNIEnv* env, const char* pat) {
    jclass strClass = (env)->FindClass("java/lang/String");
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = (env)->NewByteArray(strlen(pat));
    (env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*) pat);
    jstring encoding = (env)->NewStringUTF("GB2312");
    auto r = (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
    env->DeleteLocalRef( strClass );
    env->DeleteLocalRef( bytes );
    env->DeleteLocalRef( encoding );
    return r;
}