package com.example.konrad.rocketfuel

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_login.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper


class LoginActivity : AppCompatActivity() {

    private val animationDrawable: AnimationDrawable by lazy {
        relativeLayout.background as AnimationDrawable
    }

    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val mListener: FirebaseAuth.AuthStateListener by lazy {
        initFirebaseAuthListener()
    }

    private val RC_SIGN_IN = 1
    private val mGoogleApiClient: GoogleApiClient by lazy {
        initGoogleApiClient()
    }
    private val spotsDialog: SpotsDialog by lazy {
        SpotsDialog(this, R.style.DialogStyleLog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val isRegisterDone: Boolean = intent.getBooleanExtra("registerFinishedFlag",
                false)

        if (isRegisterDone) {
            Toast.makeText(this, "Register succeeded", Toast.LENGTH_SHORT).show()
        }

        registerText.setOnClickListener({
            startActivity(Intent(this, RegisterActivity::class.java))
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

    private fun login(email: String, pass: String) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(
                    this, getString(R.string.incorrect_credentials), Toast.LENGTH_SHORT
            ).show()
            return
        }
        else {
            spotsDialog.show()
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        spotsDialog.dismiss()
                        if (task.isSuccessful) {
                            if (mAuth.currentUser?.isEmailVerified != true) {
                                Toast.makeText(
                                        this@LoginActivity, R.string.email_verify_prompt,
                                        Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                startActivity(
                                        Intent(
                                                this@LoginActivity, HomeActivity::class.java
                                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                )
                                finish()
                            }
                        } else {
                            Toast.makeText(
                                    this@LoginActivity, R.string.incorrect_credentials,
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
        }
    }

    private fun signIn() {
        spotsDialog.show()

        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                spotsDialog.dismiss()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task->
                    spotsDialog.dismiss()

                    if(task.isSuccessful) {
                        val userId: String = mAuth.currentUser?.uid ?: ""
                        val acct = GoogleSignIn.getLastSignedInAccount(this)
                        dbRef.addValueEventListener(object : ValueEventListener{
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

                        startActivity(
                                Intent(this, HomeActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        finish()
                    } else {
                        Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT)
                                .show()
                    }
                }
    }

    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mListener)
    }

    private fun initGoogleApiClient(): GoogleApiClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        return GoogleApiClient.Builder(applicationContext)
                .enableAutoManage(this) {
                    Toast.makeText(
                            this, getString(R.string.google_connection_failed), Toast.LENGTH_SHORT
                    ).show()
                }
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()
    }

    private fun initFirebaseAuthListener(): FirebaseAuth.AuthStateListener =
            FirebaseAuth.AuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    val homeIntent = Intent(this, HomeActivity::class.java)
                    startActivity(homeIntent)
                    finish()
                }
            }

}
