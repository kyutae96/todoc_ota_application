package com.todoc.todoc_ota_application.core.model

data class BleResponse(
    val header: Byte,
    val commandId: Byte,
    val data: ByteArray? = null
)
