package com.criticalblue.approov.exceptions

/**
 * Exception class for errors we cannot recover from.
 */
internal class ApproovSdkFatalException(message: String) : ApproovSdkException(message)
