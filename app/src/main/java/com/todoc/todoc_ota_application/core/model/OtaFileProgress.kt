package com.todoc.todoc_ota_application.core.model

data class OtaFileProgress(
    val fileId: String,          // "APP000" 같은 아이디
    val displayName: String?,    // "app000.fez" 같은 표시명
    val percent: Int,            // 0..100
    val etaText: String,         // "02:35"
    val processedChunks: Int,
    val totalChunks: Int
)
