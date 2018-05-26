package com.example.konrad.rocketfuel.Models

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.example.konrad.rocketfuel.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth

class LoginModel {

    fun initGoogleApiClient(ctx: Context, onError: (String) -> Unit): GoogleApiClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ctx.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        return GoogleApiClient.Builder(ctx.applicationContext)
                .enableAutoManage(ctx as AppCompatActivity) {
                    onError(ctx.getString(R.string.google_connection_failed))
                }
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()
    }

    fun initFirebaseAuthListener(onSuccess: () -> Unit): FirebaseAuth.AuthStateListener =
            FirebaseAuth.AuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    onSuccess()
                }
            }



    fun login(ctx: Context, mAuth: FirebaseAuth,email: String, pass: String,
              onSpotsShow: (Boolean) -> Unit,
              onError: (String) -> Unit,
              onSuccess:() -> Unit){
        onSpotsShow(true)
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(ctx as Activity) { task ->
                    onSpotsShow(false)
                    if (task.isSuccessful) {
                        if (mAuth.currentUser?.isEmailVerified != true) {
                            onError(ctx.getString(R.string.email_verify_prompt))
                        } else {
                            onSuccess()
                        }
                    } else {
                        onError(ctx.getString(R.string.incorrect_credentials))
                    }
                }
    }
}