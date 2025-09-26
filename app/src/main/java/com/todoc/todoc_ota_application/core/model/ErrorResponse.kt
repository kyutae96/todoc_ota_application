package com.todoc.todoc_ota_application.core.model

data class ErrorResponse(
    val header: Byte,
    val rspCode: Byte,
    val message: String
)
