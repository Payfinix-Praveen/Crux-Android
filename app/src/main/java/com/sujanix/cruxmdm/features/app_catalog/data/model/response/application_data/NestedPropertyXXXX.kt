package com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data

data class NestedPropertyXXXX(
    val defaultValue: String,
    val description: String,
    val entries: List<Entry>,
    val key: String,
    val nestedProperties: List<NestedPropertyXXXXX>,
    val title: String,
    val type: String
)