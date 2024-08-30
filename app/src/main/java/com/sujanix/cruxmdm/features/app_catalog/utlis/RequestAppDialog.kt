package com.sujanix.cruxmdm.features.app_catalog.utlis

import android.app.Dialog
import android.content.Context
import com.sujanix.cruxmdm.R

class RequestAppDialog(
    private val context: Context
): Dialog(context) {
    init {
        setContentView(R.layout.dialog_request_app)
    }
}