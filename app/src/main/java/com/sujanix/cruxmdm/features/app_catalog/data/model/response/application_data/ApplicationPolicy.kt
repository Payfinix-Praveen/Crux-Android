package com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data

data class ApplicationPolicy(
    val accessibleTrackIds: List<String>,
    val alwaysOnVpnLockdownExemption: String,
    val autoUpdateMode: String,
    val connectedWorkAndPersonalApp: String,
    val credentialProviderPolicy: String,
    val defaultPermissionPolicy: String,
    val delegatedScopes: List<String>,
    val disabled: Boolean,
    val installConstraint: List<InstallConstraint>,
    val installPriority: Int,
    val minimumVersionCode: Int,
    val workProfileWidgets: String
)