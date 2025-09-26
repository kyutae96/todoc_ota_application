package com.todoc.todoc_ota_application.core.model

import java.io.File
import java.util.Locale

enum class OtaFileType(val num: Int, val fileName: String) {
    MANIFEST(1, "MANIFEST.TXT"),
    APP000  (2, "APP000.FEZ"),
    APP001  (3, "APP001.FEZ"),
    APP002  (4, "APP002.FEZ");

    companion object {
        /** 번호로 찾기 (C3 header의 fileNum 등과 매칭) */
        fun fromNum(n: Int): OtaFileType? = entries.firstOrNull { it.num == n }

        /**
         * 파일명/아이디로 찾기.
         * - "APP000", "APP000.FEZ", "app000.fez" 모두 허용
         * - "MANIFEST" / "MANIFEST.TXT"도 허용
         */
        fun fromName(nameOrPath: String): OtaFileType? {
            val name = File(nameOrPath).name.uppercase(Locale.US)
            val bare = name.substringBeforeLast('.', name)

            // fileName(확장자 포함) or enum name, 둘 다 케이스무시로 매칭
            return entries.firstOrNull { it.fileName.equals(name, true) || it.name.equals(bare, true) }
        }
    }

    /** 화면 표시에 적당한 표시명(확장자 포함) */
    val displayName: String get() = fileName
}