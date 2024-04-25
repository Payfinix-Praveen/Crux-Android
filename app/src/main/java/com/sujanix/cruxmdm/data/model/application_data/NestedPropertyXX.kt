package com.sujanix.cruxmdm.data.model.application_data

data class NestedPropertyXX(
    val defaultValue: String,
    val description: String,
    val entries: List<Entry>,
    val key: String,
    val nestedProperties: List<NestedPropertyXXX>,
    val title: String,
    val type: String
)