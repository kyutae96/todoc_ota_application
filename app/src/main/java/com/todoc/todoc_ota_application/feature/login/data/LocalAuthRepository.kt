package com.todoc.todoc_ota_application.feature.login.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authStore by preferencesDataStore("auth_prefs")
private val KEY_USER_INITIAL = stringPreferencesKey("user_initial")
private val KEY_USER_PASSKEY = stringPreferencesKey("user_passkey")
private val KEY_PAIRING_DEVICE = stringPreferencesKey("pairing_device")

class LocalAuthRepository(private val context: Context) {
    suspend fun saveInitial(userInitialPassKey: UserInitialPassKey) {
        context.authStore.edit { it[KEY_USER_INITIAL] = userInitialPassKey.initial.trim() }
        context.authStore.edit { it[KEY_USER_PASSKEY] = userInitialPassKey.passKey.trim() }
    }

    suspend fun savePasskey(passKey:String?){
        context.authStore.edit { it[KEY_USER_PASSKEY] = passKey?.trim() ?: "1111" }
    }

    suspend fun getInitialOrNull(): String? =
        context.authStore.data.map { it[KEY_USER_INITIAL] }.first()
    suspend fun getPassKeyOrNull(): String? =
        context.authStore.data.map { it[KEY_USER_PASSKEY] }.first()
    suspend fun getPairingDevice(): String? =
        context.authStore.data.map { it[KEY_PAIRING_DEVICE] }.first()

    suspend fun isLoggedIn(): Boolean = !getInitialOrNull().isNullOrEmpty()
    suspend fun clearAll(){clearInitial();clearPasskey();clearPairingDevice()}
    suspend fun clearInitial() { context.authStore.edit { it.remove(KEY_USER_INITIAL) } }
    suspend fun clearPasskey() { context.authStore.edit { it.remove(KEY_USER_PASSKEY) } }
    suspend fun clearPairingDevice() { context.authStore.edit { it.remove(KEY_PAIRING_DEVICE) } }

    suspend fun savePairingDevice(pairingDevice: String) {
        context.authStore.edit { it[KEY_PAIRING_DEVICE] = pairingDevice.trim() }
    }


    data class UserInitialPassKey(
        val initial:String,
        val passKey: String
    )


}
