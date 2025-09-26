package com.todoc.todoc_ota_application.data.storage


import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

// data/firestore/Refs.kt
object OtaRefs {
    fun deviceRef(db: FirebaseFirestore, deviceId: String) =
        db.collection("devices").document(deviceId)

    fun sessionsCol(db: FirebaseFirestore, deviceId: String) =
        deviceRef(db, deviceId).collection("otaSessions")

    fun sessionRef(db: FirebaseFirestore, deviceId: String, sessionId: String) =
        sessionsCol(db, deviceId).document(sessionId)

    fun eventsCol(db: FirebaseFirestore, deviceId: String, sessionId: String) =
        sessionRef(db, deviceId, sessionId).collection("events")

    fun slotHistoryCol(db: FirebaseFirestore, deviceId: String) =
        deviceRef(db, deviceId).collection("slotHistory")



}
