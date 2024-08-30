package com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data

data class NestedProperty(
    val defaultValue: Int,
    val description: String,
    val key: String,
    val nestedProperties: List<NestedPropertyX>,
    val title: String,
    val type: String
)