package com.sujanix.cruxmdm.features.core.data.model.fencing

import com.google.gson.annotations.SerializedName
import com.sujanix.cruxmdm.features.core.data.model.socket.location.Coordinate

data class GeoJson(
    val coordinates: List<List<Coordinate>> = listOf(),
    @SerializedName("coordinates_type")val coordinatesType: String = ""
)