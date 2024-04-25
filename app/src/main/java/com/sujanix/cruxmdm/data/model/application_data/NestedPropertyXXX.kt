package com.sujanix.cruxmdm.data.model.application_data

data class NestedPropertyXXX(
    val defaultValue: String,
    val description: String,
    val entries: List<Entry>,
    val key: String,
    val nestedProperties: List<NestedPropertyXXXX>,
    val title: String,
    val type: String
)