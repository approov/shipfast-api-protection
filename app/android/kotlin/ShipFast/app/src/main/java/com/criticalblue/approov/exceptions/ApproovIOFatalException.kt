/*****************************************************************************
 * Project:     Approov Kotlin Framework
 * Copyright(c) 2019 by CriticalBlue Ltd.
 *****************************************************************************/

package com.criticalblue.approov.exceptions

/**
 * IOException class for errors we cannot recover from.
 */
internal class ApproovIOFatalException(message: String) : ApproovIOException(message)
