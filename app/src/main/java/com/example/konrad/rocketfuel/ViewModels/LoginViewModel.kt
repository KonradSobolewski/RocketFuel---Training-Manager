package com.example.konrad.rocketfuel.ViewModels

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.example.konrad.rocketfuel.Models.LoginModel
import com.example.konrad.rocketfuel.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

class LoginViewModel(private val ctx: Context) : ViewModel() {

    private val loginModel = LoginModel()
    private val spotsLiveData = MutableLiveData<Boolean>()
    private val errorLiveData = MutableLiveData<String>()
    private val newActivity = MutableLiveData<Boolean>()

    fun error(): LiveData<String> = errorLiveData
    fun spotsGetter(): LiveData<Boolean> = spotsLiveData
    fun startNewActivity(): LiveData<Boolean> = newActivity

    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val mListener: FirebaseAuth.AuthStateListener by lazy {
        loginModel.initFirebaseAuthListener({
            newActivity.value = true
        })
    }

    private val mGoogleApiClient: GoogleApiClient by lazy {
        loginModel.initGoogleApiClient(ctx,{
            errorLiveData.value = it
        })
    }

    fun signIn() : Intent{
        spotsLiveData.value = true
        return Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
    }

    fun startListening(){
        mAuth.addAuthStateListener(mListener)
    }

    fun login(email: String, pass: String, context: Context) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            errorLiveData.value = context.getString(R.string.incorrect_credentials)
            return
        }else{
            loginModel.login(context, mAuth, email, pass, {
                spotsLiveData.value = it
            },{
                errorLiveData.value = it
            },{
                newActivity.value = true
            })
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount,ctx: Context) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(ctx as Activity) { task->
                    spotsLiveData.value = false

                    if(task.isSuccessful) {
                        val userId: String = mAuth.currentUser?.uid ?: ""
                        val acct = GoogleSignIn.getLastSignedInAccount(ctx)
                        dbRef.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError?) {
                                Log.e("Logout", "error!")
                            }

                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0?.hasChild(userId) != true){
                                    if (acct != null ) {
                                        val personName = acct.givenName
                                        val personFamilyName = acct.familyName
                                        val personEmail = acct.email
                                        val personImg = acct.photoUrl.toString()
                                        dbRef.child(userId)?.run {
                                            child("Name")?.setValue(personName)
                                            child("Surname")?.setValue(personFamilyName)
                                            child("Email")?.setValue(personEmail)
                                            child("Image")?.setValue(personImg)
                                            push()
                                        }
                                    }
                                }
                            }
                        })
                        newActivity.value = true
                    } else {
                        errorLiveData.value = ctx.getString(R.string.connection_error)
                    }
                }
    }
}