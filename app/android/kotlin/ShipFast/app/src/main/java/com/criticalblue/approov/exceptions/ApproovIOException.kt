/*****************************************************************************
 * Project:     Approov Kotlin Framework
 * Copyright(c) 2019 by CriticalBlue Ltd.
 *****************************************************************************/

package com.criticalblue.approov.exceptions

import java.io.IOException

/**
 * The base class to be extended by all Approov IOException classes.
 */
internal open class ApproovIOException(message: String) : IOException(message)