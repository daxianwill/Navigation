package com.yjx.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yjx.navigation.model.TrackHistory
import com.yjx.navigation.util.DistanceCalculator
import com.yjx.navigation.util.TrackHistoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class HistoryActivity : AppCompatActivity() {

    private lateinit var trackHistoryManager: TrackHistoryManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var fabClose: FloatingActionButton
    
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        trackHistoryManager = TrackHistoryManager(this)
        
        initViews()
        loadTrackHistory()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabClose = findViewById(R.id.fabClose)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = HistoryAdapter(emptyList(), 
            onItemClick = { trackHistory ->
                openTrackFromHistory(trackHistory)
            },
            onItemDelete = { trackHistory ->
                deleteTrackFromHistory(trackHistory)
            }
        )
        recyclerView.adapter = historyAdapter
        
        fabClose.setOnClickListener {
            finish()
        }
    }

    private fun loadTrackHistory() {
        val historyList = trackHistoryManager.getTrackHistoryList()
        historyAdapter.updateData(historyList)
    }

    private fun openTrackFromHistory(trackHistory: TrackHistory) {
        val intent = Intent().apply {
            data = Uri.parse(trackHistory.uri)
            putExtra("track_name", trackHistory.name)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun deleteTrackFromHistory(trackHistory: TrackHistory) {
        trackHistoryManager.deleteTrackHistory(trackHistory.id)
        loadTrackHistory()
        Toast.makeText(this, "已删除: ${trackHistory.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    inner class HistoryAdapter(
        private var historyList: List<TrackHistory>,
        private val onItemClick: (TrackHistory) -> Unit,
        private val onItemDelete: (TrackHistory) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val llTrackInfo: View = itemView.findViewById(R.id.llTrackInfo)
            val tvTrackName: TextView = itemView.findViewById(R.id.tvTrackName)
            val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
            val tvElevation: TextView = itemView.findViewById(R.id.tvElevation)
            val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            val fabDelete: FloatingActionButton = itemView.findViewById(R.id.fabDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_track_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val trackHistory = historyList[position]
            
            holder.tvTrackName.text = trackHistory.name
            holder.tvDistance.text = DistanceCalculator.formatDistance(trackHistory.distance)
            val ascent = DistanceCalculator.formatElevation(trackHistory.totalAscent)
            val descent = DistanceCalculator.formatElevation(trackHistory.totalDescent)
            holder.tvElevation.text = "上升: $ascent / 下降: $descent"
            
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            holder.tvDate.text = dateFormat.format(java.util.Date(trackHistory.openedAt))
            
            holder.llTrackInfo.setOnClickListener {
                onItemClick(trackHistory)
            }
            
            holder.fabDelete.setOnClickListener {
                onItemDelete(trackHistory)
            }
        }

        override fun getItemCount(): Int = historyList.size

        fun updateData(newList: List<TrackHistory>) {
            historyList = newList
            notifyDataSetChanged()
        }
    }
}