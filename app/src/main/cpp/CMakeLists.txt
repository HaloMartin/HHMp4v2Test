# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

#自定义distribution_DIR变量，开发者可以根据自己的需要自行设置
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../libs)
#支持-std=gnu++11
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
#自定义so文件输出目录，${PROJECT_SOURCE_DIR}是工程目录，即CMakeList.txt文件所在目录
#${ANDROID_ABI}是表示CPU架构，因为我在gradle指定了只有armeabi，所以这里也只会有一个
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/output/${ANDROID_ABI})

#导入Mp4v2动态库，指定SHARED表示动态添加
add_library( Mp4v2
        SHARED
        IMPORTED )

#设置目标动态库的位置，当前目录为CMakeList.txt所在的位置，定位到libMp4v2.so需要先往上三级
set_target_properties( Mp4v2
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../../../libs/armeabi/libMp4v2.so
#        ../../../../../../libs/armeabi/libMp4v2.so
        )
#指定动态库头文件路径
include_directories(${PROJECT_SOURCE_DIR}/../../../libs/include)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

#添加脚本，编译HHMp4v2.c生成HHMp4v2动态库
add_library(HHMp4v2 SHARED
        ${PROJECT_SOURCE_DIR}/HHMp4v2.c
        )
#add_library( # Sets the name of the library.
#        native-lib
#
#        # Sets the library as a shared library.
#        SHARED
#
#        # Provides a relative path to your source file(s).
#        native-lib.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

#关联这两个库
target_link_libraries( HHMp4v2 Mp4v2 ${log-lib} )

#target_link_libraries( # Specifies the target library.
#        native-lib
#
#        # Links the target library to the log library
#        # included in the NDK.
#        ${log-lib})