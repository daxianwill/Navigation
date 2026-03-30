package com.yjx.navigation.model

import android.net.Uri

data class TrackHistory(
    val id: String,
    val name: String,
    val uri: String,
    val distance: Double,
    val totalAscent: Double,
    val totalDescent: Double,
    val pointCount: Int,
    val openedAt: Long
)