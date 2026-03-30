package com.yjx.navigation.util

import android.content.Context
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.model.LatLng

class LocationManager(private val context: Context) {

    private var locationClient: AMapLocationClient? = null
    private var locationListener: ((LatLng?) -> Unit)? = null

    init {
        initLocation()
    }

    private fun initLocation() {
        locationClient = AMapLocationClient(context)
        
        val option = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = false
            interval = 2000
            isNeedAddress = true
        }
        
        locationClient?.setLocationOption(option)
    }

    fun startLocation(listener: (LatLng?) -> Unit) {
        this.locationListener = listener
        
        locationClient?.setLocationListener { location ->
            if (location != null && location.errorCode == 0) {
                val latLng = LatLng(location.latitude, location.longitude)
                listener(latLng)
            } else {
                listener(null)
            }
        }
        
        locationClient?.startLocation()
    }

    fun stopLocation() {
        locationClient?.stopLocation()
    }

    fun getLastKnownLocation(): LatLng? {
        val location = locationClient?.lastKnownLocation
        return if (location != null && location.errorCode == 0) {
            LatLng(location.latitude, location.longitude)
        } else {
            null
        }
    }

    fun destroy() {
        stopLocation()
        locationClient?.onDestroy()
        locationClient = null
        locationListener = null
    }
}