package com.sujanix.cruxmdm.feature.app_catalog.data.model

import com.sujanix.cruxmdm.feature.app_catalog.data.data_source.local.ApplicationEntity
import com.sujanix.cruxmdm.feature.app_catalog.data.model.response.application_data.ApplicationDataApi
import com.sujanix.cruxmdm.feature.app_catalog.data.model.response.selfHosted.Data

data class Application(
    val type: String? = null,
    val name: String? = null,
//    val icon: Drawable? = null,
    val pkg: String? = null,
    val url: String? = null,
    val version: String? = null,
    val versionCode: Int? = null,
    val iconUrl: String? = null,
    val isInstalled: Boolean = false,
    val isRemove: Boolean = false,
    val isDownloaded: Boolean = false
) {
    companion object {
        const val TYPE_APP = "App"
        const val TYPE_WEB = "Web"
        const val TYPE_INTENT = "intent"

        fun ApplicationDataApi.toApplication(): Application? {
            return try {
                Application(
                    type = this.type,
                    name = this.name,
                    iconUrl = this.app_logo ?: this.app_logo_base64,
                    pkg = this.bundle_identifier,
                    version = this.version,
                    isInstalled = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun Data.toApplication(): Application {
            return Application(
                type = type,
                name = name,
                iconUrl = app_logo_base64,
                pkg = bundle_identifier,
                url = signed_url ?: repository_url,
                version = version,
                versionCode = version_code
            )
        }

        fun Application.toApplicationEntity(): ApplicationEntity {
            return ApplicationEntity(
                type = type,
                name = name,
                pkg = pkg,
                url = url,
                version = version,
                versionCode = versionCode,
                iconUrl = iconUrl,
                isInstalled = isInstalled,
                isRemove = isRemove,
                isDownloaded = isDownloaded
            )
        }
    }
}
