package com.example.konrad.rocketfuel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
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

    private var animationDrawable: AnimationDrawable? = null

    private var dbRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private var mListener: FirebaseAuth.AuthStateListener? = null

    private val RC_SIGN_IN = 1
    private var mGoogleApiClient: GoogleApiClient? = null
    private var spotsDialog: SpotsDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Set permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.INTERNET), 1
            )
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2
            )
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 3
            )
        }

        dbRef = FirebaseDatabase.getInstance().reference.child("Users")
        mAuth = FirebaseAuth.getInstance()

        mListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                val homeIntent = Intent(this, HomeActivity::class.java)
                startActivity(homeIntent)
                finish()
            }
        }

        val isRegisterDone: Boolean = intent.getBooleanExtra("registerFinishedFlag",
                false)

        if (isRegisterDone) {
            Toast.makeText(this, "Register succeeded", Toast.LENGTH_SHORT).show()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                .enableAutoManage(this) {
                    Toast.makeText(
                            this, "Google connection failed", Toast.LENGTH_SHORT
                    ).show()
                }.addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()


        registerText.setOnClickListener({
            startActivity(Intent(this, RegisterActivity::class.java))
        })

        loginBtn.setOnClickListener({
            login(emailText?.text.toString().trim(), passwordText?.text.toString().trim())
        })

        googleBtn.setOnClickListener({
            signIn()
        })

        animationDrawable = relativeLayout.background as AnimationDrawable

        animationDrawable?.setEnterFadeDuration(4500)
        animationDrawable?.setExitFadeDuration(4500)
        animationDrawable?.start()

        spotsDialog = SpotsDialog(this, R.style.DialogStyleLog)
    }

    private fun login(email: String, pass: String) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Incorrect credentials", Toast.LENGTH_SHORT).show()
            return
        }
        else {
            spotsDialog?.show()
            mAuth?.signInWithEmailAndPassword(email, pass)
                    ?.addOnCompleteListener(this) { task ->
                        spotsDialog?.dismiss()
                        if (task.isSuccessful) {
                            if (mAuth?.currentUser?.isEmailVerified != true) {
                                Toast.makeText(
                                        this@LoginActivity, "Please verify your email",
                                        Toast.LENGTH_SHORT
                                ).show()
                                return@addOnCompleteListener
                            }
                            startActivity(
                                    Intent(
                                    this@LoginActivity, HomeActivity::class.java
                                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        } else {
                            Toast.makeText(
                                    this@LoginActivity, "Incorrect credentials",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
        }
    }

    private fun signIn() {
        spotsDialog?.show()

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
                spotsDialog?.dismiss()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task->
                    spotsDialog?.dismiss()

                    if(task.isSuccessful) {
                        val userId: String? = mAuth?.currentUser?.uid
                        val acct = GoogleSignIn.getLastSignedInAccount(this)
                        dbRef?.addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {
                                Log.e("Logout", "error!")
                            }

                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0?.hasChild(userId) != true){
                                    if (acct != null ) {
                                        val databaseRef2: DatabaseReference? = dbRef?.child(userId)
                                        val personName = acct.givenName
                                        val personFamilyName = acct.familyName
                                        val personEmail = acct.email
                                        val personImg = acct.photoUrl.toString()
                                        databaseRef2?.child("Name")?.setValue(personName)
                                        databaseRef2?.child("Surname")?.setValue(personFamilyName)
                                        databaseRef2?.child("Email")?.setValue(personEmail)
                                        databaseRef2?.child("Image")?.setValue(personImg)
                                        databaseRef2?.push()
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
                        Toast.makeText(this, "Connection error", Toast.LENGTH_SHORT)
                                .show()
                    }
                }
    }

    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onStart() {
        super.onStart()

        mAuth?.addAuthStateListener(mListener!!)
    }
}
