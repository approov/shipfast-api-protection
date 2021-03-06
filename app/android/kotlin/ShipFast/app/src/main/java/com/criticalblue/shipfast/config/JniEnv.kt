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

package com.criticalblue.shipfast.config

import java.lang.System.loadLibrary

/**
 * A class to retrieve environment values that where declared in the `main/cpp/.jni.env.h` file,
 *  that at compile time are hidden into the native C code of their respective methods in
 *  the file `main/cpp/jni-env.cpp`, that will be then accessed by this class during runtime.
 */
class JniEnv {

    /**
     * Get the API key from the compile time JNI env variable `SHIPFAST_API_KEY` in the native
     *  C code, as per defined in the file `main/cpp/.jni.env.h` and accessible to Kotlin code via
     *  the file `main/cpp/jni-env.cpp`.
     *
     * @return The Api key from the JNI env.
     */
    external fun getApiKey(): String

    /**
     * Get the Hmac secret from the compile time JNI env variable `SHIPFAST_API_HMAC_SECRET` in the native
     *  C code, as per defined in the file `main/cpp/.jni.env.h` and accessible to Kotlin code via
     *  the file `main/cpp/jni-env.cpp`.
     *
     * @return The Hmac secret from the JNI env.
     */
    external fun getHmacSecret(): String

    /**
     * Get the API base url from the compile time JNI env variable `SHIPFAST_API_BASE_URL` in the
     *  native C code, as per defined in the file `main/cpp/.jni.env.h` and accessible to Kotlin
     *  code via the file `main/cpp/jni-env.cpp`.
     *
     * @return The Api base url from the JNI env.
     */
    external fun getApiBaseUrl(): String

    /**
     * Get the Auth0 client id from the compile time JNI env variable `AUTH0_CLIENT_ID` in the
     *  native C code, as per defined in the file `main/cpp/.jni.env.h` and accessible to Kotlin
     *  code via the file `main/cpp/jni-env.cpp`.
     *
     * @return The Auth0 client id.
     */
    external fun getAuth0ClientId(): String


    companion object {
        init {
            // Used to load the `main/cpp/jni-env.cpp` library on application startup.
            loadLibrary("jni-env")
        }
    }
}
