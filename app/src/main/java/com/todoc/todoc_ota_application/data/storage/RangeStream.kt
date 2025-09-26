package com.todoc.todoc_ota_application.data.storage


import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object RangeStream {
    private suspend fun signedUrl(path: String): String {
        val ref = Firebase.storage.reference.child(path)
        return ref.downloadUrl.await().toString()  // 서명 토큰 포함 URL
    }
    /** 파일 총 사이즈: GET Range(0-0)로 Content-Range에서 파싱 (ex: "bytes 0-0/123456") */
    suspend fun totalBytes(path: String): Long = withContext(Dispatchers.IO) {
        val url = signedUrl(path)
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 30000
            setRequestProperty("Range", "bytes=0-0")
        }
        try {
            if (conn.responseCode != HttpURLConnection.HTTP_PARTIAL) {
                throw IllegalStateException("Unexpected HTTP ${conn.responseCode} on totalBytes")
            }
            val cr = conn.getHeaderField("Content-Range") ?: error("No Content-Range")
            // "bytes 0-0/123456"
            val total = cr.substringAfter('/').toLong()
            total
        } finally {
            conn.disconnect()
        }
    }
//    suspend fun totalBytes(path: String): Long = withContext(Dispatchers.IO) {
//        val ref = Firebase.storage.reference.child(path)
//        ref.metadata.await().sizeBytes
//    }

    /** start..endInclusive 범위를 정확히 내려받는다(서버 Range GET, HTTP 206). */

    suspend fun readRange(path: String, start: Long, endInclusive: Long): ByteArray =
        withContext(Dispatchers.IO) {
            val url = signedUrl(path)
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 30000
                setRequestProperty("Range", "bytes=$start-$endInclusive")
            }
            try {
                if (conn.responseCode != HttpURLConnection.HTTP_PARTIAL) {
                    throw IllegalStateException("Unexpected HTTP ${conn.responseCode} on readRange")
                }
                conn.inputStream.use { ins ->
                    val buf = ByteArray(16 * 1024)
                    val bos = ByteArrayOutputStream()
                    while (true) {
                        val n = ins.read(buf)
                        if (n <= 0) break
                        bos.write(buf, 0, n)
                    }
                    bos.toByteArray()
                }
            } finally {
                conn.disconnect()
            }
        }
}
