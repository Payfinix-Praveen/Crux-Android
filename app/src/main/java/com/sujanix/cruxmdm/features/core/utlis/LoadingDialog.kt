package com.sujanix.cruxmdm.features.core.utlis

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.sujanix.cruxmdm.R

class LoadingDialog(context: Context) : Dialog(context) {

    init {
        setCancelable(false)
        show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading_dialog_layout)

    }
}