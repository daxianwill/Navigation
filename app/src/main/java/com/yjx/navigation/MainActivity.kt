package com.yjx.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.MapView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yjx.navigation.model.KmlData
import com.yjx.navigation.model.TrackHistory
import com.yjx.navigation.util.DistanceCalculator
import com.yjx.navigation.util.FilePicker
import com.yjx.navigation.util.KmlParser
import com.yjx.navigation.util.LocationManager
import com.yjx.navigation.util.MapManager
import com.yjx.navigation.util.PermissionManager
import com.yjx.navigation.util.TrackHistoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var mapManager: MapManager
    private lateinit var locationManager: LocationManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var kmlParser: KmlParser
    private lateinit var filePicker: FilePicker
    private lateinit var trackHistoryManager: TrackHistoryManager
    
    private lateinit var infoCard: View
    private lateinit var tvTrackName: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvAscent: TextView
    private lateinit var tvDescent: TextView
    private lateinit var tvPointCount: TextView
    
    private var currentKmlData: KmlData? = null
    private var currentTrackUri: Uri? = null
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val REQUEST_CODE_HISTORY = 1004
        private const val TAG = "MainActivity"
        private const val KEY_TRACK_URI = "track_uri"
        private const val KEY_TRACK_NAME = "track_name"
        private const val PREFS_NAME = "navigation_prefs"
        private const val PREF_LAST_TRACK_URI = "last_track_uri"
        private const val PREF_LAST_TRACK_NAME = "last_track_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        
        mapManager = MapManager(this)
        locationManager = LocationManager(this)
        permissionManager = PermissionManager(this)
        kmlParser = KmlParser()
        filePicker = FilePicker(this)
        trackHistoryManager = TrackHistoryManager(this)
        
        initViews()
        mapManager.initializeMap(mapView)
        requestPermissions()
        
        if (savedInstanceState != null) {
            val savedUri = savedInstanceState.getString(KEY_TRACK_URI)
            val savedName = savedInstanceState.getString(KEY_TRACK_NAME)
            if (!savedUri.isNullOrEmpty()) {
                Log.d(TAG, "Restoring track from saved state: $savedUri")
                try {
                    val uri = Uri.parse(savedUri)
                    loadKmlFile(uri, savedName ?: "未命名轨迹", saveToPrefs = false)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore track", e)
                }
            }
        } else {
            loadLastTrack()
        }
    }

    private fun loadLastTrack() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val lastUri = prefs.getString(PREF_LAST_TRACK_URI, null)
        val lastName = prefs.getString(PREF_LAST_TRACK_NAME, null)
        
        if (!lastUri.isNullOrEmpty()) {
            Log.d(TAG, "Loading last track: $lastUri")
            try {
                val uri = Uri.parse(lastUri)
                loadKmlFile(uri, lastName ?: "未命名轨迹", saveToPrefs = false)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load last track", e)
            }
        }
    }

    private fun saveCurrentTrack(uri: Uri?, name: String?) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        
        if (uri != null && name != null) {
            editor.putString(PREF_LAST_TRACK_URI, uri.toString())
            editor.putString(PREF_LAST_TRACK_NAME, name)
        } else {
            editor.remove(PREF_LAST_TRACK_URI)
            editor.remove(PREF_LAST_TRACK_NAME)
        }
        
        editor.apply()
        Log.d(TAG, "Saved current track: $uri, name: $name")
    }

    private fun initViews() {
        infoCard = findViewById(R.id.infoCard)
        tvTrackName = findViewById(R.id.tvTrackName)
        tvDistance = findViewById(R.id.tvDistance)
        tvAscent = findViewById(R.id.tvAscent)
        tvDescent = findViewById(R.id.tvDescent)
        tvPointCount = findViewById(R.id.tvPointCount)
        
        val fabAddTrack = findViewById<FloatingActionButton>(R.id.fabAddTrack)
        val fabLocation = findViewById<FloatingActionButton>(R.id.fabLocation)
        val fabClear = findViewById<FloatingActionButton>(R.id.fabClear)
        val fabHistory = findViewById<FloatingActionButton>(R.id.fabHistory)
        
        fabAddTrack.setOnClickListener {
            Toast.makeText(this, "点击了添加轨迹", Toast.LENGTH_SHORT).show()
            importKmlFile()
        }
        
        fabLocation.setOnClickListener {
            showCurrentLocation()
        }
        
        fabClear.setOnClickListener {
            clearTrack()
        }
        
        fabHistory.setOnClickListener {
            openHistoryActivity()
        }
    }

    private fun requestPermissions() {
        permissionManager.requestLocationPermissions { granted ->
            if (granted) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "需要位置权限才能显示当前位置", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLocationUpdates() {
        locationManager.startLocation { location ->
            location?.let {
                mapManager.updateCurrentLocation(it)
            }
        }
    }

    private fun showCurrentLocation() {
        permissionManager.requestLocationPermissions { granted ->
            if (granted) {
                locationManager.getLastKnownLocation()?.let { location ->
                    mapManager.moveToLocation(location, 15f)
                } ?: run {
                    Toast.makeText(this, "正在获取位置...", Toast.LENGTH_SHORT).show()
                    startLocationUpdates()
                }
            } else {
                Toast.makeText(this, "需要位置权限才能显示当前位置", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun importKmlFile() {
        permissionManager.requestStoragePermissionsWithDialog { granted ->
            if (granted) {
                val intent = filePicker.createPickFileIntent()
                startActivityForResult(intent, FilePicker.REQUEST_CODE_PICK_FILE)
            } else {
                Toast.makeText(this, "需要存储权限才能导入KML文件", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openHistoryActivity() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_HISTORY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == FilePicker.REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                currentTrackUri = uri
                takeUriPermission(uri)
                loadKmlFile(uri)
            }
        } else if (requestCode == REQUEST_CODE_HISTORY && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                currentTrackUri = uri
                loadKmlFile(uri)
            }
        }
    }

    private fun takeUriPermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            Log.d(TAG, "Taken persistable URI permission for: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to take persistable URI permission", e)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "未命名轨迹"
        try {
            val projection = arrayOf(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)
            val cursor = contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)
                    fileName = it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file name", e)
        }
        return fileName
    }

    private fun loadKmlFile(uri: Uri, fileName: String? = null, saveToPrefs: Boolean = true) {
        mainScope.launch {
            try {
                Log.d(TAG, "Loading KML file from URI: $uri")
                val kmlData = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        kmlParser.parse(inputStream)
                    }
                }
                
                kmlData?.let { data ->
                    currentKmlData = data
                    val name = fileName ?: getFileNameFromUri(uri)
                    displayTrack(data)
                    showTrackInfo(data, name)
                    saveTrackToHistory(data, uri, name)
                    currentTrackUri = uri
                    if (saveToPrefs) {
                        saveCurrentTrack(uri, name)
                    }
                    Toast.makeText(this@MainActivity, "轨迹加载成功: $name", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this@MainActivity, "解析KML文件失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load KML file", e)
                Toast.makeText(this@MainActivity, "加载KML文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTrackToHistory(kmlData: KmlData, uri: Uri, fileName: String) {
        val distance = DistanceCalculator.calculateDistance(kmlData.coordinates)
        val elevationStats = DistanceCalculator.calculateElevationStats(kmlData.coordinates)
        
        val trackHistory = TrackHistory(
            id = trackHistoryManager.generateTrackId(),
            name = fileName,
            uri = uri.toString(),
            distance = distance,
            totalAscent = elevationStats.totalAscent,
            totalDescent = elevationStats.totalDescent,
            pointCount = kmlData.coordinates.size,
            openedAt = System.currentTimeMillis()
        )
        
        trackHistoryManager.saveTrackHistory(trackHistory)
    }

    private fun displayTrack(kmlData: KmlData) {
        mapManager.drawTrack(kmlData.coordinates)
    }

    private fun showTrackInfo(kmlData: KmlData, fileName: String) {
        val distance = DistanceCalculator.calculateDistance(kmlData.coordinates)
        val formattedDistance = DistanceCalculator.formatDistance(distance)
        val elevationStats = DistanceCalculator.calculateElevationStats(kmlData.coordinates)
        val formattedAscent = DistanceCalculator.formatElevation(elevationStats.totalAscent)
        val formattedDescent = DistanceCalculator.formatElevation(elevationStats.totalDescent)
        val pointCount = kmlData.coordinates.size
        
        tvTrackName.text = fileName
        tvDistance.text = "距离: $formattedDistance"
        tvAscent.text = "上升: $formattedAscent"
        tvDescent.text = "下降: $formattedDescent"
        tvPointCount.text = "点数: $pointCount"
        
        infoCard.visibility = View.VISIBLE
    }

    private fun hideTrackInfo() {
        infoCard.visibility = View.GONE
        currentKmlData = null
        currentTrackUri = null
    }

    private fun clearTrack() {
        mapManager.clearTrack()
        hideTrackInfo()
        saveCurrentTrack(null, null)
        Toast.makeText(this, "轨迹已清除", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handleRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        currentTrackUri?.let {
            outState.putString(KEY_TRACK_URI, it.toString())
        }
        currentKmlData?.let {
            outState.putString(KEY_TRACK_NAME, tvTrackName.text.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            mapView.onResume()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            mapView.onPause()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPause", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mapView.onDestroy()
            mapManager.destroy()
            locationManager.stopLocation()
            mainScope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }
}