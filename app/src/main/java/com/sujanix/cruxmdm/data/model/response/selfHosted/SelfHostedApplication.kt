package com.sujanix.cruxmdm.data.model.response.selfHosted

import com.sujanix.cruxmdm.data.model.Application

data class SelfHostedApplication(
    val STATUS: Boolean,
    val `data`: List<Data>
)
