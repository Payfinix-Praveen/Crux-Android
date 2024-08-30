package com.sujanix.cruxmdm.features.app_catalog.data.model.response.selfHosted

data class Data(
    val type: String? = null,
    val app_distribution_method: String? = null,
    val app_logo: String? = null,
    val app_logo_base64: String? = null,
    val bundle_identifier: String? = null,
    val name: String? = null,
    val repository_url: String? = null,
    val signed_url: String? = null,
    val version: String? = null,
    val version_code: Int = 1
)