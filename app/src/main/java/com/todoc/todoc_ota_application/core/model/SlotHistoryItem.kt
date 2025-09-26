package com.todoc.todoc_ota_application.core.model

data class SlotHistoryItem(
    val at: com.google.firebase.Timestamp? = null,
    val fromSlot: Int? = null,
    val toSlot: Int? = null,
    val reason: String = "ota", // "ota","manual","revert"
    val sessionId: String? = null
) {
    @get:com.google.firebase.firestore.Exclude
    val id: String? = null
}