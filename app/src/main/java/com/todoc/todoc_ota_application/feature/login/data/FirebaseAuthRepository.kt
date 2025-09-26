package com.todoc.todoc_ota_application.feature.login.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    // 소유자 이메일 → 로그인/회원가입 시 admin
    private val OWNER_EMAIL = "kyutae0523@to-doc.com"
    var docReg: ListenerRegistration? = null
    private fun usersRef(uid: String) = db.collection("users").document(uid)

    /** onAuthStateChanged를 Flow로 래핑 */
    fun authState(): Flow<AppUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            // 이전 문서 리스너 제거
            docReg?.remove()
            docReg = null

            if (user == null) {
                trySend(null)
                return@AuthStateListener
            }

            val docRef = db.collection("users").document(user.uid)

            docReg = docRef.addSnapshotListener { snap, err ->
                    if (err != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    if (snap != null && snap.exists()) {
                        val data = snap.data.orEmpty()
                        val roleStr = (data["role"] as? String)?.uppercase() ?: "unauthorized"
                        val role =
                            Role.entries.firstOrNull { it.name == roleStr } ?: Role.UNAUTHORIZED
                        val appUser = AppUser(
                            uid = user.uid,
                            name = (data["name"] as? String)
                                ?: (user.email?.substringBefore("@") ?: "User"),
                            email = user.email.orEmpty(),
                            avatar = (data["avatar"] as? String) ?: AppUser.defaultAvatar(user.uid),
                            organization = data["organization"] as? String,
                            role = role
                        )
                        trySend(appUser)
                    } else {
                        // Auth에는 있는데 Firestore엔 없음 → 생성
                        val isOwner = user.email == OWNER_EMAIL
                        val newRole = if (isOwner) Role.ADMIN else Role.UNAUTHORIZED
                        val appUser = AppUser(
                            uid = user.uid,
                            name = if (isOwner) "김규태" else (user.email?.substringBefore("@")
                                ?: "User"),
                            email = user.email.orEmpty(),
                            avatar = AppUser.defaultAvatar(user.uid),
                            organization = if (isOwner) "토닥" else null,
                            role = newRole
                        )
                        usersRef(user.uid).set(
                            mapOf(
                                "email" to appUser.email,
                                "name" to appUser.name,
                                "avatar" to appUser.avatar,
                                "organization" to appUser.organization,
                                "role" to appUser.role.name.lowercase()
                            )
                        ).addOnSuccessListener { trySend(appUser) }
                            .addOnFailureListener { trySend(null) }
                    }
                }

        }
        auth.addAuthStateListener(listener)
        awaitClose { docReg?.remove(); auth.removeAuthStateListener(listener) }
    }

    /** 로그인: Firestore 문서 없으면 생성 / owner면 role 보정 */
    suspend fun login(email: String, password: String) {
        val cred = auth.signInWithEmailAndPassword(email, password).await()
        val u = cred.user ?: return
        val docRef = usersRef(u.uid)
        val snap = docRef.get().await()
        if (snap.exists()) {
            val data = snap.data.orEmpty()
            val currentEmail = data["email"] as? String
            // owner인데 admin이 아니라면 role=admin으로 보정
            if (currentEmail == OWNER_EMAIL && (data["role"] as? String) != "admin") {
                docRef.update("role", "admin").await()
            }
        } else {
            val isOwner = u.email == OWNER_EMAIL
            val newUser = mapOf(
                "email" to (u.email ?: ""),
                "name" to (if (isOwner) "김규태" else (u.email?.substringBefore("@") ?: "New User")),
                "organization" to (if (isOwner) "토닥" else null),
                "avatar" to AppUser.defaultAvatar(u.uid),
                "role" to (if (isOwner) "admin" else "unauthorized")
            )
            docRef.set(newUser).await()
        }
    }

    /** 회원가입: Auth 만들고 users/{uid} 문서 생성 (owner면 admin) */
    suspend fun signup(email: String, password: String, name: String, organization: String) {
        val cred = auth.createUserWithEmailAndPassword(email, password).await()
        val u = cred.user ?: return
        val isOwner = u.email == OWNER_EMAIL
        val userData = mapOf(
            "uid" to u.uid,
            "email" to (u.email ?: ""),
            "name" to (if (isOwner) "김규태" else name),
            "organization" to (if (isOwner) "토닥" else organization),
            "avatar" to AppUser.defaultAvatar(u.uid),
            "role" to (if (isOwner) "admin" else "unauthorized")
        )
        usersRef(u.uid).set(userData).await()
    }

    suspend fun refreshUserDoc(): AppUser? {
        val u = auth.currentUser ?: return null
        val snap = usersRef(u.uid).get().await()
        if (!snap.exists()) return null
        val data = snap.data.orEmpty()
        val roleStr = (data["role"] as? String)?.uppercase() ?: "UNAUTHORIZED"
        val role = Role.values().firstOrNull { it.name == roleStr } ?: Role.UNAUTHORIZED
        return AppUser(
            uid = u.uid,
            name = (data["name"] as? String) ?: (u.email?.substringBefore("@") ?: "User"),
            email = u.email.orEmpty(),
            avatar = (data["avatar"] as? String) ?: AppUser.defaultAvatar(u.uid),
            organization = data["organization"] as? String,
            role = role
        )
    }

    suspend fun logout() {
        auth.signOut()
    }
}
