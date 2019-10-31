package com.criticalblue.approov.exceptions

/**
 * Exception class for transient errors that we can recover from.
 */
internal class ApproovSdkTransientException(message: String) : ApproovSdkException(message)

