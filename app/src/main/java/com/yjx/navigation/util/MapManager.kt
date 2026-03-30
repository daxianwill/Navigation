package com.yjx.navigation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.yjx.navigation.model.TrackPoint

class MapManager(private val context: Context) {

    private var aMap: AMap? = null
    private var currentLocationMarker: Marker? = null
    private var trackPolyline: Polyline? = null
    private val kilometerMarkers = mutableListOf<Marker>()

    fun initializeMap(mapView: MapView) {
        aMap = mapView.map
        aMap?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
            isScaleControlsEnabled = true
            logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_CENTER
        }
    }

    fun updateCurrentLocation(latLng: LatLng) {
        if (currentLocationMarker == null) {
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title("当前位置")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .draggable(false)
            
            currentLocationMarker = aMap?.addMarker(markerOptions)
            
            moveToLocation(latLng, 15f)
        } else {
            currentLocationMarker?.position = latLng
        }
    }

    fun drawTrack(coordinates: List<TrackPoint>) {
        if (coordinates.isEmpty()) return
        
        clearTrack()
        
        val latLngList = coordinates.map { it.toLatLng() }
        
        val polylineOptions = PolylineOptions()
            .addAll(latLngList)
            .color(android.graphics.Color.RED)
            .width(10f)
        
        trackPolyline = aMap?.addPolyline(polylineOptions)
        
        val kilometerMarkersList = DistanceCalculator.calculateKilometerMarkers(coordinates)
        addKilometerMarkers(kilometerMarkersList)
        
        val builder = LatLngBounds.builder()
        latLngList.forEach { builder.include(it) }
        
        try {
            val bounds = builder.build()
            val padding = 100
            aMap?.animateCamera(
                com.amap.api.maps.CameraUpdateFactory.newLatLngBounds(bounds, padding)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addKilometerMarkers(markers: List<KilometerMarker>) {
        clearKilometerMarkers()
        
        markers.forEach { marker ->
            val markerOptions = MarkerOptions()
                .position(marker.position)
                .icon(createKilometerMarkerIcon(marker.kilometer))
                .anchor(0.5f, 0.5f)
                .draggable(false)
            
            val markerObj = aMap?.addMarker(markerOptions)
            markerObj?.let { kilometerMarkers.add(it) }
        }
    }

    private fun createKilometerMarkerIcon(kilometer: Int): BitmapDescriptor {
        val text = if (kilometer == 0) "起点" else "${kilometer}km"
        val textSizeValue = 40f
        val padding = 20
        val cornerRadius = 20f
        
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = textSizeValue
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val backgroundPaint = Paint().apply {
            color = Color.parseColor("#FF6200EE")
            isAntiAlias = true
        }
        
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()
        
        val bitmapWidth = textWidth + padding * 2
        val bitmapHeight = textHeight + padding * 2
        
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val rectF = RectF(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint)
        
        val x = bitmapWidth / 2f
        val y = bitmapHeight / 2f - textBounds.exactCenterY()
        canvas.drawText(text, x, y, textPaint)
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun clearKilometerMarkers() {
        kilometerMarkers.forEach { it.remove() }
        kilometerMarkers.clear()
    }

    fun clearTrack() {
        trackPolyline?.remove()
        trackPolyline = null
        clearKilometerMarkers()
    }

    fun moveToLocation(latLng: LatLng, zoom: Float) {
        aMap?.animateCamera(
            com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        )
    }

    fun destroy() {
        aMap = null
        currentLocationMarker = null
        trackPolyline = null
        kilometerMarkers.clear()
    }
}