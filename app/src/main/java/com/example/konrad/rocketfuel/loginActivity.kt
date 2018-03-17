package com.example.konrad.rocketfuel

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.TextUtils
import android.util.Log
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

class loginActivity : AppCompatActivity() {

    private var animationDrawable: AnimationDrawable? = null

    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    private val RC_SIGN_IN = 1
    private var mGoogleApiClient: GoogleApiClient? = null

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Set persmissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                .enableAutoManage(this) {
                    Toast.makeText(this, "Google connection failed", Toast.LENGTH_SHORT).show()
                }.addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()


        registerText?.setOnClickListener({
            register(emailText?.text.toString().trim(), passwordText?.text.toString().trim())
        })

        loginBtn?.setOnClickListener({
            login(emailText?.text.toString().trim(), passwordText?.text.toString().trim())
        })

        googleBtn?.setOnClickListener({
            signIn()
        })

        animationDrawable = relativeLayout?.getBackground() as AnimationDrawable

        animationDrawable?.setEnterFadeDuration(4500)
        animationDrawable?.setExitFadeDuration(4500)
        animationDrawable?.start()

        progressDialog = ProgressDialog(this)
    }

    private fun login(email: String, pass: String) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Błędne dane", Toast.LENGTH_SHORT).show()
            return
        } else {
            progressDialog?.setMessage("Logowanie prosze czekac..")
            progressDialog?.show()
            mAuth!!.signInWithEmailAndPassword(email, pass)
                    ?.addOnCompleteListener(this) { task ->
                        progressDialog?.dismiss()
                        if (task.isSuccessful) {
                            startActivity(Intent(this@loginActivity, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@loginActivity, "Błędne dane logowania", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun register(email: String, pass: String) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Błędne dane", Toast.LENGTH_SHORT).show()
            return
        } else {
            progressDialog?.setMessage("Rejestracja prosze czekac...")
            progressDialog?.show()
            mAuth?.createUserWithEmailAndPassword(email, pass)
                    ?.addOnCompleteListener(this) { task ->
                        progressDialog?.dismiss()
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Możesz sie zalogwać!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Błędne dane", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun signIn() {
        progressDialog?.setMessage("Logowanie prosze czekać...")
        progressDialog?.show()
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
                progressDialog?.dismiss()
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task->
                    progressDialog?.dismiss()
                    if(task.isSuccessful) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }else{
                        Toast.makeText(this, "Błęd połączenia", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    //Zmienia czcionke
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
