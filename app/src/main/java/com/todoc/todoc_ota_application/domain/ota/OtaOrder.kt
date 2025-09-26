package com.todoc.todoc_ota_application.domain.ota

import com.todoc.todoc_ota_application.core.model.OtaFileType

object OtaOrder {
    val order = listOf(
        OtaFileType.MANIFEST,
        OtaFileType.APP000,
        OtaFileType.APP001,
        OtaFileType.APP002
    )
    fun remotePath(base: String, t: OtaFileType): String = "$base/${t.fileName}" // base="ota/<version>"
}
