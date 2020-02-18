/*****************************************************************************
 * Project:     Approov Kotlin Framework
 * Copyright(c) 2019 by CriticalBlue Ltd.
 *****************************************************************************/

package com.criticalblue.approov.exceptions

/**
 * IOException class for transient errors that we can recover from.
 */
internal class ApproovIOTransientException(message: String) : ApproovIOException(message)

