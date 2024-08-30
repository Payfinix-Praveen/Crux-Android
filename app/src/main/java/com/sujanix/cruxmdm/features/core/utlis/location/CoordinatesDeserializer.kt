package com.sujanix.cruxmdm.features.core.utlis.location

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.sujanix.cruxmdm.features.core.data.model.fencing.Coordinates
import com.sujanix.cruxmdm.features.core.data.model.socket.location.Coordinate
import java.lang.reflect.Type

class CoordinatesDeserializer: JsonDeserializer<Coordinates> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Coordinates {
        return when {
            json!!.isJsonArray && json.asJsonArray[0].isJsonArray && json.asJsonArray[0].asJsonArray[0].isJsonArray -> {
                val doubleNestedType = object : TypeToken<List<List<List<Coordinate>>>>() {}.type
                val coordinates: List<List<List<Coordinate>>> = context!!.deserialize(json, doubleNestedType)
                Coordinates.MultiPolygon(coordinates)
            }
            json.isJsonArray && json.asJsonArray[0].isJsonArray -> {
                val nestedType = object : TypeToken<List<List<Coordinate>>>() {}.type
                val coordinates: List<List<Coordinate>> = context!!.deserialize(json, nestedType)
                Coordinates.Polygon(coordinates)
            }
            else -> throw JsonParseException("Invalid coordinates structure")
        }
    }
}