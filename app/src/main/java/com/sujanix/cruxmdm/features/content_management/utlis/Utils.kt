package com.sujanix.cruxmdm.features.content_management.utlis

import android.view.View

object Utils {

    fun View.visible(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

}