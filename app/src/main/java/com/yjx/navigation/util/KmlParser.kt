package com.yjx.navigation.util

import android.util.Log
import android.util.Xml
import com.yjx.navigation.model.KmlData
import com.yjx.navigation.model.TrackPoint
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class KmlParser {

    private val TAG = "KmlParser"

    fun parse(inputStream: InputStream): KmlData? {
        Log.d(TAG, "Starting KML parsing")
        
        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, "UTF-8")
            
            var eventType = parser.eventType
            var name = ""
            var description = ""
            val coordinates = mutableListOf<TrackPoint>()
            var inCoordinates = false
            var inPlacemark = false
            var inLineString = false
            var inTrack = false
            var currentCoordinatesText = StringBuilder()
            val trackCoords = mutableListOf<String>()
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name
                        Log.d(TAG, "START_TAG: $tagName")
                        when {
                            tagName.contains("name", ignoreCase = true) -> {
                                if (!inPlacemark) {
                                    val text = try {
                                        parser.nextText()
                                    } catch (e: Exception) {
                                        ""
                                    }
                                    if (text.isNotBlank()) {
                                        name = text
                                        Log.d(TAG, "Found name: $name")
                                    }
                                }
                            }
                            tagName.contains("description", ignoreCase = true) -> {
                                val text = try {
                                    parser.nextText()
                                } catch (e: Exception) {
                                    ""
                                }
                                if (text.isNotBlank()) {
                                    description = text
                                }
                            }
                            tagName.contains("Placemark", ignoreCase = true) -> {
                                inPlacemark = true
                            }
                            tagName.contains("LineString", ignoreCase = true) -> {
                                inLineString = true
                                Log.d(TAG, "Found LineString element")
                            }
                            tagName.contains("Track", ignoreCase = true) -> {
                                inTrack = true
                                trackCoords.clear()
                                Log.d(TAG, "Found Track element")
                            }
                            tagName.contains("coordinates", ignoreCase = true) -> {
                                if (inLineString) {
                                    inCoordinates = true
                                    currentCoordinatesText = StringBuilder()
                                    Log.d(TAG, "Found coordinates element in LineString")
                                }
                            }
                            tagName.equals("coord", ignoreCase = true) -> {
                                if (inTrack) {
                                    val text = try {
                                        parser.nextText()
                                    } catch (e: Exception) {
                                        null
                                    }
                                    if (text != null && text.isNotBlank()) {
                                        trackCoords.add(text.trim())
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inCoordinates) {
                            val text = parser.text
                            if (text != null) {
                                currentCoordinatesText.append(text)
                                Log.d(TAG, "Appending coordinates text, current length: ${currentCoordinatesText.length}")
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name
                        when {
                            tagName.contains("coordinates", ignoreCase = true) -> {
                                if (inLineString && currentCoordinatesText.isNotEmpty()) {
                                    val text = currentCoordinatesText.toString()
                                    Log.d(TAG, "Parsing coordinates, length: ${text.length}")
                                    val parsedPoints = parseCoordinates(text)
                                    Log.d(TAG, "Parsed ${parsedPoints.size} points")
                                    coordinates.addAll(parsedPoints)
                                }
                                inCoordinates = false
                                currentCoordinatesText = StringBuilder()
                            }
                            tagName.contains("LineString", ignoreCase = true) -> {
                                inLineString = false
                            }
                            tagName.contains("Track", ignoreCase = true) -> {
                                if (trackCoords.isNotEmpty()) {
                                    Log.d(TAG, "Parsing Track coords: ${trackCoords.size} points")
                                    val parsedPoints = parseTrackCoords(trackCoords)
                                    Log.d(TAG, "Parsed ${parsedPoints.size} points from Track")
                                    coordinates.addAll(parsedPoints)
                                }
                                inTrack = false
                                trackCoords.clear()
                            }
                            tagName.contains("Placemark", ignoreCase = true) -> {
                                inPlacemark = false
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            
            Log.d(TAG, "Parsing complete: name=$name, coordinates=${coordinates.size}")
            
            if (coordinates.isNotEmpty()) {
                KmlData(
                    name = name.ifBlank { "未命名轨迹" },
                    coordinates = coordinates,
                    description = description
                )
            } else {
                Log.e(TAG, "No coordinates found in KML file")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse KML file", e)
            null
        }
    }
    
    private fun parseTrackCoords(coords: List<String>): List<TrackPoint> {
        val points = mutableListOf<TrackPoint>()
        
        for (coord in coords) {
            val parts = coord.trim().split(Regex("\\s+"))
            if (parts.size >= 2) {
                try {
                    val longitude = parts[0].trim().toDouble()
                    val latitude = parts[1].trim().toDouble()
                    val elevation = if (parts.size >= 3) {
                        try {
                            parts[2].trim().toDouble()
                        } catch (e: NumberFormatException) {
                            0.0
                        }
                    } else {
                        0.0
                    }
                    points.add(TrackPoint(latitude, longitude, elevation))
                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Failed to parse track coord: $coord")
                }
            }
        }
        
        return points
    }
    
    private fun parseCoordinates(text: String): List<TrackPoint> {
        val points = mutableListOf<TrackPoint>()
        
        val cleanedText = text.trim()
        val tokens = cleanedText.split(Regex("\\s+"))
        
        for (token in tokens) {
            val line = token.trim()
            if (line.isBlank()) continue
            
            val parts = line.split(",")
            if (parts.size >= 2) {
                try {
                    val longitude = parts[0].trim().toDouble()
                    val latitude = parts[1].trim().toDouble()
                    val elevation = if (parts.size >= 3) {
                        try {
                            parts[2].trim().toDouble()
                        } catch (e: NumberFormatException) {
                            0.0
                        }
                    } else {
                        0.0
                    }
                    points.add(TrackPoint(latitude, longitude, elevation))
                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Failed to parse coordinate: $line")
                }
            }
        }
        
        return points
    }
}