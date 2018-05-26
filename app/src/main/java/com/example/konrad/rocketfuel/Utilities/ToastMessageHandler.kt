package com.example.konrad.rocketfuel.Utilities

import android.content.Context
import android.widget.Toast

class ToastMessageHandler(private val context: Context) {
    fun showToastMessage(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
