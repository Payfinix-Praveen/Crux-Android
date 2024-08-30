package com.sujanix.cruxmdm.features.core.data.model.fencing

import com.google.gson.annotations.SerializedName

data class Fencing (
    val boundary_id: List<String>,
    @SerializedName("boundary_state")val boundaryState: String,
    @SerializedName("boundary_type")val boundaryType: String,
    @SerializedName("fence_id")val fenceID: Long,
    @SerializedName("fence_name")val fenceName: String,
    @SerializedName("fence_type")val fenceType: String,
    val geoJson: GeoJson,
    @SerializedName("place_name")val placeName: String,
    val alerts: List<String>
)