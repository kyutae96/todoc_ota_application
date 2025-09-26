package com.todoc.todoc_ota_application.core.model

enum class AckResult(val code: Int) {
    Success(1),
    InvalidOrder(2),
    Reject(3),
    NoFile(4)
}