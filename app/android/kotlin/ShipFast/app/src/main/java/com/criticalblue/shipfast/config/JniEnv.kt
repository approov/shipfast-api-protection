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
     * Get the API base url from the compile time JNI env variable `SHIPFAST_API_BASE_URL` in the
     *  native C code, as per defined in the file `main/cpp/.jni.env.h` and accessible to Kotlin
     *  code via the file `main/cpp/jni-env.cpp`.
     *
     * @return The Api base url from the JNI env.
     */
    external fun getApiBaseUrl(): String

    /**
     * Get the driver latitude from the compile time JNI env variable `DRIVER_LATITUDE` in the
     *  native C code, as per defined in the file `main/cpp/.jni.env.h` and accessible to Kotlin
     *  code via the file `main/cpp/jni-env.cpp`.
     *
     * @return The driver latitude from the JNI env.
     */
    private external fun driverLatitude(): String

    /**
     * The value retrieved from native C code for the `DRIVER_LATITUDE` needs to be converted to a Kotlin double.
     *
     * @return The driver latitude as a double.
     */
    fun getDriverLatitude(): Double {
        return driverLatitude().toDouble()
    }

    /**
     * The value retrieved from native C code for the `DRIVER_LONGITUDE` needs to be converted to a Kotlin double.
     *
     * @return The driver longitude from the JNI env.
     */
    private external fun driverLongitude(): String

    /**
     * Get the driver longitude from the compile time JNI env variable `DRIVER_LONGITUDE` in the
     *  native C code, as per defined in the file `main/cpp/.jni.env.h` and accessible to Kotlin
     *  code via the file `main/cpp/jni-env.cpp`.
     *
     * @return The driver longitude as a double.
     */
    fun getDriverLongitude(): Double {
        return driverLongitude().toDouble()
    }

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
            loadLibrary("jni-env");
        }
    }
}
