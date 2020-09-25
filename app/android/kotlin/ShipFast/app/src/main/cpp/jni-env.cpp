//
// Created by developer on 11/10/19.
//

/*
Copyright (C) 2020 CriticalBlue Ltd.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

#include <jni.h>
#include <string>
#include ".jni.env.h"

// Every time a Gradle build runs, the bash script `gradle-build-jni-env-h.bash` will run from the
//  `preBuild.doFirst{}` hook, and will read the `.env` file in the root of this repo to retrieve
//  and save the env variables needed for the current file into the `.jni.env.h` file, that
//  later, at compile time, is used to inject the same env variables im the current file.

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_getApiKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string API_KEY = SHIPFAST_API_KEY;

    return env->NewStringUTF(API_KEY.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_getHmacSecret(
        JNIEnv *env,
        jobject /* this */) {
    std::string HMAC_SECRET = SHIPFAST_API_HMAC_SECRET;

    return env->NewStringUTF(HMAC_SECRET.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_getApiBaseUrl(
        JNIEnv *env,
        jobject /* this */) {
    std::string API_BASE_URL = SHIPFAST_API_BASE_URL;

    return env->NewStringUTF(API_BASE_URL.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_criticalblue_shipfast_config_JniEnv_getAuth0ClientId(
        JNIEnv *env,
        jobject /* this */) {

    std::string client_id = AUTH0_CLIENT_ID;

    return env->NewStringUTF(client_id.c_str());
}
