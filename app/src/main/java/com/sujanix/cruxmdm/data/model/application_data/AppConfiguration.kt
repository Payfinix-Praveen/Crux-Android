package com.sujanix.cruxmdm.data.model.application_data

data class AppConfiguration(
    val defaultValue: Any,
    val description: String,
    val entries: List<Entry>,
    val key: String,
    val nestedProperties: List<NestedProperty>,
    val title: String,
    val type: String
)