package com.sujanix.cruxmdm.data.model.response.selfHosted

data class Data(
    val type: String? = null,
    val appDistributionMethod: String? = null,
    val app_logo: String? = null,
    val app_logo_base64: String? = null,
    val bundle_identifier: String? = null,
    val name: String? = null,
    val repository_url: String? = null,
    val signed_url: String? = null,
    val version: String? = null,
    val version_code: Int = 1
)