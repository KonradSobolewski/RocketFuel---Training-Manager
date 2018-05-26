package com.example.konrad.rocketfuel

import android.app.Activity
import android.app.Instrumentation
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.konrad.rocketfuel.Utilities.SpotsDialogHandler
import com.example.konrad.rocketfuel.Utilities.ToastMessageHandler
import com.example.konrad.rocketfuel.ViewModels.RegisterViewModel
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_register.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class RegisterActivity : AppCompatActivity() {
    private val animationDrawable: AnimationDrawable by lazy {
        registerLayout?.background as AnimationDrawable
    }

    private lateinit var viewModel: RegisterViewModel
    private val spotsDialogHandler = SpotsDialogHandler(this)
    private val toastMessageHandler = ToastMessageHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        viewModel = ViewModelProviders.of(this, ViewModelFactory(this, "Register"))
                .get(RegisterViewModel::class.java)
        viewModel.error().observe(this, Observer(toastMessageHandler::showToastMessage))
        viewModel.spotsGetter().observe(this, Observer(spotsDialogHandler::showSpots))
        viewModel.activityResultGetter().observe(this, Observer {
            it?.let { setResult(it, Intent()) }
            finish()
        })

        registerBtn.setOnClickListener({
            viewModel.register(registerUserNameTxt.text.toString().trim(),
                    registerUserSurnameTxt.text.toString().trim(),
                    registerEmailTxt.text.toString().trim(),
                    registerPasswordTxt.text.toString(),
                    registerConfirmPasswordTxt.text.toString())
        })

        animationDrawable.setEnterFadeDuration(4500)
        animationDrawable.setExitFadeDuration(4500)
        animationDrawable.start()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
