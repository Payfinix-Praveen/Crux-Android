package com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file

data class SharedJSONFileDto(
    val `data`: List<Data> = listOf(),
    val path: String = ""
)