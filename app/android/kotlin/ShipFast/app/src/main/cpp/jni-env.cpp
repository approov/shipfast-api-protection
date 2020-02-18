//
// Created by developer on 11/10/19.
//

#include <jni.h>
#include <string>
#include ".jni.env.h"

// Every time a Gradle build runs, the bash script `gradle-build-jni-env-h.bash` will run from the
//  `preBuild.doFirst{}` hook, and will read the `.env` file in the root of this repo to retrieve
//  and save the env variables needed for the current file into the `.jni.env.h` file, that
//  later, at compile time, is used to inject the same env variables im the current file.

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_getDemoStage(
        JNIEnv *env,
        jobject /* this */) {
    std::string DEMO_STAGE = SHIPFAST_DEMO_STAGE;

    return env->NewStringUTF(DEMO_STAGE.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_getApiKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string API_KEY = SHIPFAST_API_KEY;

    return env->NewStringUTF(API_KEY.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_getApiBaseUrl(
        JNIEnv *env,
        jobject /* this */) {
    std::string API_BASE_URL = SHIPFAST_API_BASE_URL;

    return env->NewStringUTF(API_BASE_URL.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_driverLatitude(
        JNIEnv *env,
        jobject /* this */) {

    std::string LATITUDE = DRIVER_LATITUDE;

    return env->NewStringUTF(LATITUDE.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_driverLongitude(
        JNIEnv *env,
        jobject /* this */) {

    std::string LONGITUDE = DRIVER_LONGITUDE;

    return env->NewStringUTF(LONGITUDE.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_getAuth0ClientId(
        JNIEnv *env,
        jobject /* this */) {

    std::string client_id = AUTH0_CLIENT_ID;

    return env->NewStringUTF(client_id.c_str());
}
