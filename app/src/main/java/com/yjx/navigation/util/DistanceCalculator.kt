package com.yjx.navigation.util

import com.amap.api.maps.model.LatLng
import com.yjx.navigation.model.TrackPoint
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class KilometerMarker(
    val position: LatLng,
    val kilometer: Int
)

data class ElevationStats(
    val totalAscent: Double,
    val totalDescent: Double
)

object DistanceCalculator {

    private const val EARTH_RADIUS = 6371000.0

    fun calculateDistance(coordinates: List<TrackPoint>): Double {
        if (coordinates.size < 2) return 0.0
        
        var totalDistance = 0.0
        for (i in 0 until coordinates.size - 1) {
            val start = coordinates[i]
            val end = coordinates[i + 1]
            totalDistance += calculateHaversineDistance(start.toLatLng(), end.toLatLng())
        }
        
        return totalDistance
    }

    fun calculateHaversineDistance(start: LatLng, end: LatLng): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS * c
    }

    fun calculateElevationStats(coordinates: List<TrackPoint>): ElevationStats {
        var totalAscent = 0.0
        var totalDescent = 0.0
        
        for (i in 0 until coordinates.size - 1) {
            val start = coordinates[i]
            val end = coordinates[i + 1]
            val elevationChange = calculateElevationChange(start, end)
            
            if (elevationChange > 0) {
                totalAscent += elevationChange
            } else {
                totalDescent += abs(elevationChange)
            }
        }
        
        return ElevationStats(
            totalAscent = totalAscent,
            totalDescent = totalDescent
        )
    }

    private fun calculateElevationChange(start: TrackPoint, end: TrackPoint): Double {
        val startElevation = getElevationFromTrackPoint(start)
        val endElevation = getElevationFromTrackPoint(end)
        return endElevation - startElevation
    }

    private fun getElevationFromTrackPoint(trackPoint: TrackPoint): Double {
        return trackPoint.elevation
    }

    fun calculateKilometerMarkers(coordinates: List<TrackPoint>): List<KilometerMarker> {
        val markers = mutableListOf<KilometerMarker>()
        
        if (coordinates.size < 2) return markers
        
        var accumulatedDistance = 0.0
        var nextKilometer = 1
        markers.add(KilometerMarker(coordinates.first().toLatLng(), 0))
        
        for (i in 0 until coordinates.size - 1) {
            val start = coordinates[i]
            val end = coordinates[i + 1]
            val segmentDistance = calculateHaversineDistance(start.toLatLng(), end.toLatLng())
            
            if (segmentDistance <= 0) continue
            
            val startDistance = accumulatedDistance
            val endDistance = accumulatedDistance + segmentDistance
            
            while (nextKilometer * 1000 <= endDistance) {
                val targetDistance = nextKilometer * 1000.0
                val ratio = (targetDistance - startDistance) / segmentDistance
                val position = interpolatePosition(start.toLatLng(), end.toLatLng(), ratio)
                markers.add(KilometerMarker(position, nextKilometer))
                nextKilometer++
            }
            
            accumulatedDistance = endDistance
        }
        
        val totalDistance = calculateDistance(coordinates)
        if (totalDistance > 0 && markers.isNotEmpty()) {
            val lastMarker = markers.last()
            if (lastMarker.kilometer == 0 && totalDistance >= 100) {
                markers.add(KilometerMarker(coordinates.last().toLatLng(), totalDistance.toInt() / 1000))
            }
        }
        
        return markers
    }

    private fun interpolatePosition(start: LatLng, end: LatLng, ratio: Double): LatLng {
        val lat = start.latitude + (end.latitude - start.latitude) * ratio
        val lon = start.longitude + (end.longitude - start.longitude) * ratio
        return LatLng(lat, lon)
    }

    fun formatDistance(meters: Double): String {
        return when {
            meters < 1000 -> {
                String.format("%.0f 米", meters)
            }
            else -> {
                val kilometers = meters / 1000.0
                String.format("%.2f 公里", kilometers)
            }
        }
    }

    fun formatElevation(meters: Double): String {
        return String.format("%.0f 米", meters)
    }

    fun formatDistanceWithUnits(meters: Double): String {
        return when {
            meters < 1000 -> {
                String.format("%.0fm", meters)
            }
            else -> {
                val kilometers = meters / 1000.0
                String.format("%.2fkm", kilometers)
            }
        }
    }
}