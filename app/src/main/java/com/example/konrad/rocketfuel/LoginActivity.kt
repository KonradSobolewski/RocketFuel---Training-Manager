package com.example.konrad.rocketfuel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference


class LoginActivity : AppCompatActivity() {

    private var animationDrawable: AnimationDrawable? = null

    private var dbRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private val RC_SIGN_IN = 1
    private var mGoogleApiClient: GoogleApiClient? = null

    private var progressBar: ProgressBar? = null

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

        progressBar = this.loginProgressBar
        registerText?.setOnClickListener({
            startActivity(Intent(this, RegisterActivity::class.java))
        })

        loginBtn?.setOnClickListener({
            login(emailText?.text.toString().trim(), passwordText?.text.toString().trim())
        })

        googleBtn?.setOnClickListener({
            signIn()
        })

        animationDrawable = relativeLayout?.background as AnimationDrawable

        animationDrawable?.setEnterFadeDuration(4500)
        animationDrawable?.setExitFadeDuration(4500)
        animationDrawable?.start()

        progressBar = this.loginProgressBar
    }

    private fun login(email: String, pass: String) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Incorrect credentials", Toast.LENGTH_SHORT).show()
            return
        } else {
            progressBar!!.visibility = View.VISIBLE
            mAuth!!.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        progressBar!!.visibility = View.GONE
                        if (task.isSuccessful) {
                            progressBar!!.visibility = View.GONE
                            startActivity(
                                    Intent(
                                    this@LoginActivity, HomeActivity::class.java
                                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        } else {
                            progressBar!!.visibility = View.GONE
                            Toast.makeText(this@LoginActivity,
                                    "Incorrect credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun signIn() {
        progressBar!!.visibility = View.VISIBLE

        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                progressBar!!.visibility = View.GONE

                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task->
                    progressBar!!.visibility = View.GONE

                    if(task.isSuccessful) {
                        val userId: String? = mAuth!!.currentUser!!.uid
                        val acct = GoogleSignIn.getLastSignedInAccount(this)
                        dbRef!!.addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {
                                Log.e("Logout", "error!")
                            }

                            override fun onDataChange(p0: DataSnapshot?) {
                                if(!p0!!.hasChild(userId)){
                                    if (acct != null ) {
                                        val databaseRef2: DatabaseReference = dbRef!!.child(userId)
                                        val personName = acct.givenName
                                        val personFamilyName = acct.familyName
                                        val personEmail = acct.email
                                        databaseRef2.child("Name").setValue(personName)
                                        databaseRef2.child("Surname").setValue(personFamilyName)
                                        databaseRef2.child("Email").setValue(personEmail)
                                        databaseRef2.push()
                                    }
                                }
                            }
                        })

                        startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        finish()
                    }else{
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
}
