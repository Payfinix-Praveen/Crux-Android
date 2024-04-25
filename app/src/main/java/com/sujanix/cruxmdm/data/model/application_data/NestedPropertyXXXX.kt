package com.sujanix.cruxmdm.data.model.application_data

data class NestedPropertyXXXX(
    val defaultValue: String,
    val description: String,
    val entries: List<Entry>,
    val key: String,
    val nestedProperties: List<NestedPropertyXXXXX>,
    val title: String,
    val type: String
)