package com.todoc.todoc_ota_application.core.model

data class CollectProgress(
    val fileNum: Int,     // 1=MANIFEST, 2=APP000, 3=APP001, 4=APP002
    val downloaded: Int,  // 누적 받은 바이트
    val total: Int,       // 총 바이트
    val percent: Int      // 0..100
) {
    companion object {
        fun of(fileNum: Int, downloaded: Long, total: Long): CollectProgress {
            val p = if (total > 0) ((downloaded * 100) / total).toInt().coerceIn(0, 100) else 0
            return CollectProgress(fileNum, downloaded.toInt(), total.toInt(), p)
        }
    }
}

data class OtaProgress(
    val fileNum: Int,
    val collected: Int,
    val total: Int,
    val percent: Int
) {
    companion object {
        /** collected/total 로부터 percent를 계산해 생성 */
        fun ofCounts(fileNum: Int, collected: Int, total: Int): OtaProgress {
            val pct = if (total <= 0) 0 else ((collected * 100) / total).coerceIn(0, 100)
            return OtaProgress(fileNum, collected, total, pct)
        }
    }
}