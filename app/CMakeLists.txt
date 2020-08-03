cmake_minimum_required(VERSION 3.4.1)

set(TNN_ROOT ${CMAKE_SOURCE_DIR}/../../external_libs/tnn/release)
include_directories(${TNN_ROOT}/include)

#include_directories(${TNN_ROOT}/third_party/opencl/include)
#include_directories(../demo/src/main/jni/thirdparty)
include_directories(${CMAKE_SOURCE_DIR}/src/main/jni/kannarotate/include)
include_directories(${CMAKE_SOURCE_DIR}/src/main/jni/kannarotate/src)
#include_directories(${COMMON_SRC}/)
#include_directories(../demo/src/main/jni/cc/)
file(GLOB_RECURSE KANNA_SRCS ${CMAKE_SOURCE_DIR}/src/main/jni/kannarotate/src/*.c)
add_library(KANNA
        SHARED
        ${KANNA_SRCS})

set(TNN_OPENCL_ENABLE ON CACHE BOOL "" FORCE)
set(TNN_ARM_ENABLE ON CACHE BOOL "" FORCE)
#set(TNN_BUILD_SHARED OFF CACHE BOOL "" FORCE)
add_library(tnn SHARED IMPORTED)
set_target_properties(tnn PROPERTIES IMPORTED_LOCATION
        ${TNN_ROOT}/${ANDROID_ABI}/libTNN.so )

add_library(demo
        SHARED
        src/main/jni/demo_jni.cpp
        src/main/jni/UltraFace.cpp
        src/main/jni/helper_jni.cc
     )

find_library(log-lib log)

target_link_libraries(demo
        tnn
        android
        KANNA
        -ljnigraphics
#        -Wl,--whole-archive TNN -Wl,--no-whole-archive
        ${log-lib})