package com.sujanix.cruxmdm.features.auth.data.model.response

import com.sujanix.cruxmdm.features.core.data.model.OrganizationData

data class EnterpriseData(
    val data: List<OrganizationData>,
    val msg: String,
    val status: Boolean
)