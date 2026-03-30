package com.yjx.navigation.model

data class KmlData(
    val name: String,
    val coordinates: List<TrackPoint>,
    val description: String = ""
)