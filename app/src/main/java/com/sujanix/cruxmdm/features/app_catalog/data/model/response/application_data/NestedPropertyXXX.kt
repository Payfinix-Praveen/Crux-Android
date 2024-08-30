package com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data

data class NestedPropertyXXX(
    val defaultValue: String,
    val description: String,
    val entries: List<Entry>,
    val key: String,
    val nestedProperties: List<NestedPropertyXXXX>,
    val title: String,
    val type: String
)