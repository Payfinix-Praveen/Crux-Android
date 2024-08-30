package com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data

data class AppConfiguration(
    val defaultValue: Any,
    val description: String,
    val entries: List<Entry>,
    val key: String,
    val nestedProperties: List<NestedProperty>,
    val title: String,
    val type: String
)