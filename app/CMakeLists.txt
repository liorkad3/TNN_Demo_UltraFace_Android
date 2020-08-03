cmake_minimum_required(VERSION 3.4.1)

set(TNN_ROOT ${CMAKE_SOURCE_DIR}/../../external_libs/tnn/release)
include_directories(${TNN_ROOT}/include)

#include_directories(${TNN_ROOT}/third_party/opencl/include)
#include_directories(../demo/src/main/jni/thirdparty)
#include_directories(../demo/src/main/jni/thirdparty/kannarotate-android-lib/include)
#include_directories(../demo/src/main/jni/thirdparty/kannarotate-android-lib/src)
#include_directories(${COMMON_SRC}/)
#include_directories(../demo/src/main/jni/cc/)

#set(TNN_OPENCL_ENABLE ON CACHE BOOL "" FORCE)
#set(TNN_ARM_ENABLE ON CACHE BOOL "" FORCE)
#set(TNN_BUILD_SHARED OFF CACHE BOOL "" FORCE)
add_library(tnn SHARED IMPORTED)
set_target_properties(tnn PROPERTIES IMPORTED_LOCATION
        ${TNN_ROOT}/${ANDROID_ABI}/libTNN.so )

add_library(demo
    SHARED
     src/main/jni/demo_jni.cpp
     )

find_library(log-lib log)

target_link_libraries(demo
        tnn
        android
        -ljnigraphics
#        -Wl,--whole-archive TNN -Wl,--no-whole-archive
        ${log-lib})