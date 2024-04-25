package com.sujanix.cruxmdm.data.model.application_data

data class ApplicationDataApi(
    val app_catelog_id: String,
//    val app_configuration: List<AppConfiguration>,
    val app_logo: String?,
    val app_logo_base64: String,
    val application_policy: ApplicationPolicy,
    val bundle_identifier: String,
    val created_at: String,
    val enterprise_id: String,
    val is_blocklisted: Boolean,
    val name: String,
    val platform: String,
    val type: String,
    val updated_at: String,
    val version: String
)