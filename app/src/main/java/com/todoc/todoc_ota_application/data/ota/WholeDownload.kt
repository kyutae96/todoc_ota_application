package com.todoc.todoc_ota_application.data.ota

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storage
import com.todoc.todoc_ota_application.feature.main.MainViewModel
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentSkipListMap
import com.todoc.todoc_ota_application.core.model.CollectProgress
import com.todoc.todoc_ota_application.core.model.OtaFileType
import com.todoc.todoc_ota_application.data.storage.RangeStream
import com.todoc.todoc_ota_application.startup.AuthGate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.math.floor
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object WholeDownload {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val inFlight = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.Deferred<Long>>()
    private val limiter = kotlinx.coroutines.sync.Semaphore(permits = 2)

    suspend fun downloadToFile(
        remotePath: String,
        local: File,
        onProgress: (Long, Long, Int) -> Unit = { _,_,_ -> }
    ): Long {
        // 1) 인증을 넣어야지 401에러 잡을수있는듯
        AuthGate.ensureSignedIn()

        return try {
            doDownload(remotePath, local, onProgress)
        } catch (e: StorageException) {

            if (e.errorCode == StorageException.ERROR_NOT_AUTHENTICATED) {
                AuthGate.ensureSignedIn()
                doDownload(remotePath, local, onProgress)
            } else throw e
        }
    }

    private suspend fun doDownload(
        remotePath: String,
        local: File,
        onProgress: (Long, Long, Int) -> Unit
    ): Long = suspendCancellableCoroutine { cont ->
        val ref = Firebase.storage.reference.child(remotePath)
        local.parentFile?.mkdirs()
        val tmp = File(local.parentFile, local.name + ".part")
        val task = ref.getFile(tmp)
            .addOnProgressListener { snap ->
                val d = snap.bytesTransferred
                val t = snap.totalByteCount
                val p = if (t > 0) ((d * 100) / t).toInt().coerceIn(0, 100) else 0
                onProgress(d, t, p)
            }
            .addOnSuccessListener {
                // 원자적 교체(안드 8+는 ATOMIC_MOVE 사용 권장)
                val ok = if (android.os.Build.VERSION.SDK_INT >= 26) {
                    try {
                        java.nio.file.Files.move(
                            tmp.toPath(), local.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                            java.nio.file.StandardCopyOption.ATOMIC_MOVE
                        )
                        true
                    } catch (_: Exception) { false }
                } else {
                    if (local.exists()) local.delete()
                    tmp.renameTo(local).also { success ->
                        if (!success) {
                            // 구버전 보정
                            tmp.copyTo(local, overwrite = true)
                            tmp.delete()
                        }
                    }
                    true
                }
                if (ok) cont.resume(local.length()) else cont.resumeWithException(IllegalStateException("move failed"))
            }
            .addOnFailureListener { e ->
                tmp.delete()
                cont.resumeWithException(e)
            }

        cont.invokeOnCancellation { task.cancel() }
    }
}


