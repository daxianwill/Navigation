package com.yjx.navigation

import android.app.Application
import com.amap.api.location.AMapLocationClient

class NavigationApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)
    }
}