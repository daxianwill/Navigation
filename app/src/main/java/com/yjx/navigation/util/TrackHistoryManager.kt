package com.yjx.navigation.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yjx.navigation.model.TrackHistory

class TrackHistoryManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("track_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val historyKey = "track_history_list"

    fun saveTrackHistory(trackHistory: TrackHistory) {
        val historyList = getTrackHistoryList().toMutableList()
        
        val existingIndex = historyList.indexOfFirst { it.name == trackHistory.name }
        
        if (existingIndex != -1) {
            historyList.removeAt(existingIndex)
        }
        
        historyList.add(0, trackHistory)
        
        if (historyList.size > 50) {
            historyList.removeLast()
        }
        
        saveHistoryList(historyList)
    }

    fun getTrackHistoryList(): List<TrackHistory> {
        val json = sharedPreferences.getString(historyKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<TrackHistory>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getRecentTrackHistory(limit: Int = 10): List<TrackHistory> {
        return getTrackHistoryList().take(limit)
    }

    fun deleteTrackHistory(trackId: String) {
        val historyList = getTrackHistoryList().toMutableList()
        historyList.removeAll { it.id == trackId }
        saveHistoryList(historyList)
    }

    fun clearAllHistory() {
        sharedPreferences.edit().remove(historyKey).apply()
    }

    private fun saveHistoryList(historyList: List<TrackHistory>) {
        val json = gson.toJson(historyList)
        sharedPreferences.edit().putString(historyKey, json).apply()
    }

    fun generateTrackId(): String {
        return "track_${System.currentTimeMillis()}"
    }
}