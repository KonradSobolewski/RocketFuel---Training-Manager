package com.example.konrad.rocketfuel.Utilities

import android.content.Context
import com.example.konrad.rocketfuel.R
import dmax.dialog.SpotsDialog

class SpotsDialogHandler(private val context: Context) {
    private val spotsDialog: SpotsDialog by lazy {
        SpotsDialog(context, R.style.DialogStyleReg)
    }

    fun showSpots(state: Boolean?) {
        if(state == true)
            spotsDialog.show()
        else
            spotsDialog.dismiss()
    }

}
