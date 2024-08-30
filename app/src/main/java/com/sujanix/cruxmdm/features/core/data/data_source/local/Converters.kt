package com.sujanix.cruxmdm.features.core.data.data_source.local

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sujanix.cruxmdm.features.core.data.model.fencing.Coordinates
import com.sujanix.cruxmdm.features.core.data.model.socket.location.Coordinate

class Converters {
    private val TAG: String = "Converters"
    private val gson = Gson()

    @TypeConverter
    fun fromCoordinates(coordinates: Coordinates): String {
        return when(coordinates) {
            is Coordinates.Polygon -> gson.toJson(coordinates.coordinates)
            is Coordinates.MultiPolygon -> gson.toJson(coordinates.coordinates)
            is Coordinates.DoubleNested -> gson.toJson(coordinates.coordinates)
        }
    }

    @TypeConverter
    fun toCoordinates(data: String): Coordinates {
        val polygonType = object : TypeToken<List<List<Coordinate>>>() {}.type
        val multiPolygonType = object : TypeToken<List<List<List<Coordinate>>>>() {}.type
        val doubleNestedType = object : TypeToken<List<List<List<List<Coordinate>>>>>() {}.type

        return try {
            val polygon = gson.fromJson<List<List<Coordinate>>>(data, polygonType)
            Coordinates.Polygon(polygon)
        } catch (e: Exception) {
            try {
                val multiPolygon = gson.fromJson<List<List<List<Coordinate>>>>(data, multiPolygonType)
                Coordinates.MultiPolygon(multiPolygon)
            } catch (e: Exception) {
                val doubleNested =
                    gson.fromJson<List<List<List<List<Coordinate>>>>>(data, doubleNestedType)
                Coordinates.DoubleNested(doubleNested)
            }
        }
    }
}