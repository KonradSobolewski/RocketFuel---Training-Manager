package com.example.konrad.rocketfuel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.example.konrad.rocketfuel.ViewModels.LoginViewModel
import com.example.konrad.rocketfuel.ViewModels.RegisterViewModel


class ViewModelFactory(val context: Context, val type: String) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (type) {
            "Login" -> LoginViewModel(context) as T
            "Register" -> RegisterViewModel(context) as T
            else -> throw IllegalArgumentException("Pass correct factory type")
        }
    }
}
