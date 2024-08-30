package com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data

data class NestedPropertyXX(
    val defaultValue: String,
    val description: String,
    val entries: List<Entry>,
    val key: String,
    val nestedProperties: List<NestedPropertyXXX>,
    val title: String,
    val type: String
)