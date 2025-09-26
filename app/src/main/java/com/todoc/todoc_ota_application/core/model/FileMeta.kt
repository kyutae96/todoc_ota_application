package com.todoc.todoc_ota_application.core.model

data class FileMeta(
    val id: String,
    val displayName: String?,
    val totalChunks: Int,
    var processedChunks: Int = 0
)