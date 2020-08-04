#include <jni.h>
#include <vector>
#include "UltraFace.h"
#include "helper_jni.h"
#include <android/bitmap.h>
#include <kannarotate.h>
#include <yuv420sp_to_rgb_fast_asm.h>

#define ULTRA_FACE(sig) Java_com_lk_tnndemo_UltraFace_##sig


static jclass clsFaceInfo = nullptr;
static jmethodID midconstructorFaceInfo;
static jfieldID fidx1;
static jfieldID fidy1;
static jfieldID fidx2;
static jfieldID fidy2;
static jfieldID fidscore;
static jfieldID fidlandmarks;

extern "C"
JNIEXPORT jlong JNICALL
ULTRA_FACE(init)(JNIEnv * env, jobject obj, jstring modelPath_,
        jint width, jint height, jint computeType){

    std::string modelPath (jstring2string(env, modelPath_));
    auto ultraFace = new UltraFace(modelPath, width, height, computeType);
    if (clsFaceInfo == nullptr)
    {
        clsFaceInfo = static_cast<jclass>(env->NewGlobalRef(env->FindClass("com/lk/tnndemo/UltraFace$FaceInfo")));
        midconstructorFaceInfo = env->GetMethodID(clsFaceInfo, "<init>", "()V");
        fidx1 = env->GetFieldID(clsFaceInfo, "x1" , "F");
        fidy1 = env->GetFieldID(clsFaceInfo, "y1" , "F");
        fidx2 = env->GetFieldID(clsFaceInfo, "x2" , "F");
        fidy2 = env->GetFieldID(clsFaceInfo, "y2" , "F");
        fidscore = env->GetFieldID(clsFaceInfo, "score" , "F");
        fidlandmarks = env->GetFieldID(clsFaceInfo, "landmarks" , "[F");
    }

    return (jlong)ultraFace;
}

extern "C"
JNIEXPORT jint JNICALL
ULTRA_FACE(deinit)(JNIEnv *env, jobject thiz, jlong netAddress) {
    auto * ultraFace = (UltraFace*)netAddress;
    delete ultraFace;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
ULTRA_FACE(detectFromStream)(JNIEnv *env, jobject thiz, jlong netAddress,
        jbyteArray yuv420sp, jint width, jint height, jint rotate) {

    auto *ultraFace = (UltraFace *) netAddress;
    jobjectArray faceInfoArray;
    std::vector<FaceInfo> faceInfoList;
    // Convert yuv to rgb
    LOGI("detect from stream %d x %d r %d", width, height, rotate);

    auto *yuvData = new unsigned char[height * width * 3 / 2];
    jbyte *yuvDataRef = env->GetByteArrayElements(yuv420sp, 0);

    int ret = kannarotate_yuv420sp((const unsigned char *) yuvDataRef, (int) width, (int) height,
                                   (unsigned char *) yuvData, (int) rotate);
    env->ReleaseByteArrayElements(yuv420sp, yuvDataRef, 0);
    auto *rgbaData = new unsigned char[height * width * 4];
//    unsigned char *rgbData = new unsigned char[height * width * 3];
    yuv420sp_to_rgba_fast_asm((const unsigned char *) yuvData, height, width,
                              (unsigned char *) rgbaData);
//    stbi_write_jpg(rgba_image_name, height, width, 4, rgbaData, 95);
    TNN_NS::DeviceType dt = TNN_NS::DEVICE_ARM;
    TNN_NS::DimsVector target_dims = {1, 3, height, width};
    auto rgbTNN = std::make_shared<TNN_NS::Mat>(dt, TNN_NS::N8UC4, target_dims, rgbaData);
    LOGI("input mat size: %dx%dx%d", rgbTNN->GetChannel(), rgbTNN->GetWidth(), rgbTNN->GetHeight());
    TNN_NS::Status status = ultraFace->detect(rgbTNN, width, height, faceInfoList);
    delete[] yuvData;
    delete[] rgbaData;
    if (status != TNN_NS::TNN_OK) {
        LOGE("failed to detect %d", (int) status);
        return nullptr;
    }
    LOGI("face info list size %d", (int)faceInfoList.size());
    // TODO: copy face info list
    if (!faceInfoList.empty()) {
        faceInfoArray = env->NewObjectArray(faceInfoList.size(), clsFaceInfo, nullptr);
        for (int i = 0; i < faceInfoList.size(); i++) {
            jobject objFaceInfo = env->NewObject(clsFaceInfo, midconstructorFaceInfo);
            int landmarkNum = sizeof(faceInfoList[i].landmarks)/sizeof(float);
            LOGI("face[%d] %f %f %f %f score %f landmark size %d", i, faceInfoList[i].x1, faceInfoList[i].y1, faceInfoList[i].x2, faceInfoList[i].y2, faceInfoList[i].score, landmarkNum);
            env->SetFloatField(objFaceInfo, fidx1, faceInfoList[i].x1);
            env->SetFloatField(objFaceInfo, fidy1, faceInfoList[i].y1);
            env->SetFloatField(objFaceInfo, fidx2, faceInfoList[i].x2);
            env->SetFloatField(objFaceInfo, fidy2, faceInfoList[i].y2);
            env->SetFloatField(objFaceInfo, fidscore, faceInfoList[i].score);
//            jfloatArray jarrayLandmarks = env->NewFloatArray(landmarkNum);
//            env->SetFloatArrayRegion(jarrayLandmarks, 0, landmarkNum , faceInfoList[i].landmarks);
//            env->SetObjectField(objFaceInfo, fidlandmarks, jarrayLandmarks);
            env->SetObjectArrayElement(faceInfoArray, i, objFaceInfo);
            env->DeleteLocalRef(objFaceInfo);
        }
        return faceInfoArray;
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
ULTRA_FACE(detectFromImage)(JNIEnv *env, jobject thiz, jlong netAddress,
        jobject bitmap, jint width, jint height) {
    auto* ultraFace = (UltraFace*)netAddress;
    jobjectArray faceInfoArray;;
    int ret = -1;
    AndroidBitmapInfo  sourceInfocolor;
    void*              sourcePixelscolor;

    if (AndroidBitmap_getInfo(env, bitmap, &sourceInfocolor) < 0) {
        return nullptr;
    }
    if (sourceInfocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return nullptr;
    }
    if ( AndroidBitmap_lockPixels(env, bitmap, &sourcePixelscolor) < 0) {
        return nullptr;
    }
    TNN_NS::DimsVector target_dims = {1, 3, height, width};
    TNN_NS::DeviceType dt = TNN_NS::DEVICE_ARM;
    auto input_mat = std::make_shared<TNN_NS::Mat>(dt, TNN_NS::N8UC4, target_dims, sourcePixelscolor);

    std::vector<FaceInfo> faceInfoList;
    TNN_NS::Status status = ultraFace->detect(input_mat, height, width, faceInfoList);

    AndroidBitmap_unlockPixels(env, bitmap);
    if (status != TNN_NS::TNN_OK) {
        LOGE("failed to detect %d", (int)status);
        return nullptr;
    }

    if (!faceInfoList.empty()) {
        faceInfoArray = env->NewObjectArray(faceInfoList.size(), clsFaceInfo, nullptr);
        for (int i = 0; i < faceInfoList.size(); i++) {
            jobject objFaceInfo = env->NewObject(clsFaceInfo, midconstructorFaceInfo);
            int landmarkNum = sizeof(faceInfoList[i].landmarks)/sizeof(float);
            LOGI("face[%d] %f %f %f %f score %f landmark size %d", i, faceInfoList[i].x1, faceInfoList[i].y1, faceInfoList[i].x2, faceInfoList[i].y2, faceInfoList[i].score, landmarkNum);
            env->SetFloatField(objFaceInfo, fidx1, faceInfoList[i].x1);
            env->SetFloatField(objFaceInfo, fidy1, faceInfoList[i].y1);
            env->SetFloatField(objFaceInfo, fidx2, faceInfoList[i].x2);
            env->SetFloatField(objFaceInfo, fidy2, faceInfoList[i].y2);
            env->SetFloatField(objFaceInfo, fidscore, faceInfoList[i].score);
//            jfloatArray jarrayLandmarks = env->NewFloatArray(landmarkNum);
//            env->SetFloatArrayRegion(jarrayLandmarks, 0, landmarkNum , faceInfoList[i].landmarks);
//            env->SetObjectField(objFaceInfo, fidlandmarks, jarrayLandmarks);
            env->SetObjectArrayElement(faceInfoArray, i, objFaceInfo);
            env->DeleteLocalRef(objFaceInfo);
        }
        return faceInfoArray;
    }
    return nullptr;
}