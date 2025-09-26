package com.todoc.todoc_ota_application.core.model

data class OtaCommand(
    val index: ByteArray? = null,
    val targetSlot: Int,
    val payload : ByteArray? = null,
    val commandType: OtaCommandType
)

enum class OtaCommandType {
    START_COMMAND, END_COMMAND, DATA_HEADER, DATA_WRITE
}

