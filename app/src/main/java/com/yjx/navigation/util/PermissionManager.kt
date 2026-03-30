package com.yjx.navigation.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: AppCompatActivity) {

    private var onPermissionResult: ((Boolean) -> Unit)? = null

    fun requestLocationPermissions(listener: (Boolean) -> Unit) {
        this.onPermissionResult = listener
        
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (hasLocationPermissions()) {
            listener(true)
        } else {
            activity.requestPermissions(permissions, REQUEST_CODE_LOCATION)
        }
    }

    fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestStoragePermissionsWithDialog(listener: (Boolean) -> Unit) {
        Log.d("PermissionManager", "requestStoragePermissionsWithDialog called")
        this.onPermissionResult = listener
        
        if (hasStoragePermissions()) {
            Log.d("PermissionManager", "Storage permissions already granted")
            listener(true)
            return
        }
        
        Log.d("PermissionManager", "Showing storage permission dialog")
        showStoragePermissionDialog {
            Log.d("PermissionManager", "Requesting storage permissions directly")
            requestStoragePermissionsDirectly()
        }
    }

    private fun showStoragePermissionDialog(onPositive: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("需要存储权限")
            .setMessage("为了能够导入和查看KML轨迹文件，应用需要访问您的存储权限。\n\n授予权限后，您可以：\n• 选择并导入KML轨迹文件\n• 在地图上查看轨迹路线\n• 保存和管理您的轨迹数据")
            .setPositiveButton("授权") { _, _ ->
                Log.d("PermissionManager", "User clicked authorize button")
                onPositive()
            }
            .setNegativeButton("取消") { dialog, _ ->
                Log.d("PermissionManager", "User clicked cancel button")
                dialog.dismiss()
                onPermissionResult?.invoke(false)
            }
            .setCancelable(false)
            .show()
    }

    private fun requestStoragePermissionsDirectly() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("PermissionManager", "Using Android 13+ permissions")
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            Log.d("PermissionManager", "Using legacy storage permissions")
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        
        Log.d("PermissionManager", "Requesting permissions: ${permissions.joinToString()}")
        activity.requestPermissions(permissions, REQUEST_CODE_STORAGE)
    }

    fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("PermissionManager", "Checking Android 13+ permissions")
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            Log.d("PermissionManager", "Checking legacy permissions")
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun handleRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("PermissionManager", "handleRequestPermissionsResult: requestCode=$requestCode, permissions=${permissions.joinToString()}, grantResults=${grantResults.joinToString()}")
        
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        Log.d("PermissionManager", "All permissions granted: $allGranted")
        
        if (requestCode == REQUEST_CODE_STORAGE && !allGranted) {
            Log.d("PermissionManager", "Storage permissions denied, showing dialog")
            showPermissionDeniedDialog()
        }
        
        onPermissionResult?.invoke(allGranted)
        onPermissionResult = null
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("权限被拒绝")
            .setMessage("存储权限被拒绝，无法导入KML轨迹文件。\n\n您可以在应用设置中手动授予权限：\n1. 打开应用信息\n2. 选择权限\n3. 启用存储权限")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    companion object {
        const val REQUEST_CODE_LOCATION = 1002
        const val REQUEST_CODE_STORAGE = 1003
    }
}