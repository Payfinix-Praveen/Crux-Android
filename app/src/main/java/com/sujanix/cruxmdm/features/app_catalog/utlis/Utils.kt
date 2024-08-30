package com.sujanix.cruxmdm.features.app_catalog.utlis

object Utils {

    fun getPackageNameFromPlayStoreUrl(url: String): String{
        val pattern = "id=(.*)".toRegex()
        val matchResult = pattern.find(url)
        return matchResult?.groupValues?.getOrNull(1) ?: ""
    }
}