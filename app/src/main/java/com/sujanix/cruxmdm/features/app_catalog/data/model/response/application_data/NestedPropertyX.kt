package com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data

data class NestedPropertyX(
    val defaultValue: Int,
    val description: String,
    val entries: List<Entry>,
    val key: String,
    val nestedProperties: List<NestedPropertyXX>,
    val title: String,
    val type: String
)