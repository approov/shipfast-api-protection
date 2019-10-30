package com.criticalblue.approov.exceptions

/**
 * The base class to be used for all the other Approov Exception classes.
 */
internal open class ApproovSdkException(message: String) : Throwable(message)