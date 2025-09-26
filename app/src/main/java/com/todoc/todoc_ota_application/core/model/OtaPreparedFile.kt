package com.todoc.todoc_ota_application.core.model

import java.io.File

data class OtaPreparedFile(
    val type: OtaFileType,
    val remotePath: String,
    val localFile: File,
    val length: Long,
    val endIndexNum: Int,
    val endIndexByte: Int,
    val headerIndex0: ByteArray // 0xC3 Index=0 헤더
)

data class OtaPreparedPlan(
    val base: String,           // "ota/<version>"
    val slotNum: Int,           // 1 or 2
    val chunkSize: Int,         // BLE 페이로드 (MTU 네고 없으면 20)
    val files: List<OtaPreparedFile>
)