package com.yjx.navigation.model

import com.amap.api.maps.model.LatLng

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0
) {
    fun toLatLng(): LatLng = LatLng(latitude, longitude)
}