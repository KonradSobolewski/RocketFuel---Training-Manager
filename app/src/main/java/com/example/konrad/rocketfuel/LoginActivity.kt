package com.example.konrad.rocketfuel

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.konrad.rocketfuel.Utilities.SpotsDialogHandler
import com.example.konrad.rocketfuel.Utilities.ToastMessageHandler
import com.example.konrad.rocketfuel.ViewModels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.android.synthetic.main.activity_login.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper


class LoginActivity : AppCompatActivity() {

    private val animationDrawable: AnimationDrawable by lazy {
        relativeLayout.background as AnimationDrawable
    }

    companion object {
        const val RC_SIGN_IN = 1
        const val REGISTER_KEY = 2
    }

    private val spotsDialogHandler = SpotsDialogHandler(this)
    private val toastMessageHandler = ToastMessageHandler(this)
    private lateinit var viewModel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProviders.of(this, ViewModelFactory(this, "Login"))
                .get(LoginViewModel::class.java)
        viewModel.error().observe(this, Observer(toastMessageHandler::showToastMessage))
        viewModel.spotsGetter().observe(this, Observer(spotsDialogHandler::showSpots))
        viewModel.startNewActivity().observe(this, Observer(this::startHomeActivity))


        registerText.setOnClickListener({
            startActivityForResult(Intent(this, RegisterActivity::class.java),REGISTER_KEY)
        })

        loginBtn.setOnClickListener({
            login(emailText?.text.toString().trim(), passwordText?.text.toString().trim())
        })

        googleBtn.setOnClickListener({
            signIn()
        })

        animationDrawable.run {
            setEnterFadeDuration(4500)
            setExitFadeDuration(4500)
            start()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startListening()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.firebaseAuthWithGoogle(account,this)
            } catch (e: ApiException) {
                spotsDialogHandler.showSpots(false)
            }
        }
        else if(requestCode == REGISTER_KEY && resultCode == Activity.RESULT_OK)
        {
            toastMessageHandler.showToastMessage(getString(R.string.register_succeeded))
        }
    }
    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun startHomeActivity(state: Boolean?){
        if(state == true) {
            startActivity(Intent(
                            this, HomeActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }
    }

    private fun login(email: String, pass: String) {
        viewModel.login(email,pass,this)
    }

    private fun signIn() {
        startActivityForResult(viewModel.signIn(), RC_SIGN_IN)
    }

}
