package com.criticalblue.approov.dto

/**
 * The data class for an Approov Token.
 */
data class ApproovToken(
    val token: String,
    val hasNewConfig: Boolean
)