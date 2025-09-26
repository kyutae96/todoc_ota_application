package com.todoc.todoc_ota_application.core.model

data class InfoResponse(
    val result: Int,    // 1=Accept, 2=Invalid, 3=Reject
    val versionMajor: Int,
    val versionMinor: Int,
    val currentBootSlotNum : Byte,  // FF=255, 1, 2  최근 슬롯
    val preBootSlotNum: Byte   // FF=255, 1, 2  이전 슬롯
)