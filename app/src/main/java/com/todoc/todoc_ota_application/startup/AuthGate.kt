package com.todoc.todoc_ota_application.startup

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

object AuthGate {
    private val auth by lazy { Firebase.auth }
    private val lock = Mutex()

    suspend fun ensureSignedIn(): FirebaseUser {
        auth.currentUser?.let { return it }
        return lock.withLock {
            auth.currentUser ?: auth.signInAnonymously().await().user
            ?: error("Anonymous sign-in failed")
        }
    }
}
