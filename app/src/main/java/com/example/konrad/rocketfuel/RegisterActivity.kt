package com.example.konrad.rocketfuel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_register.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class RegisterActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val animationDrawable: AnimationDrawable by lazy {
        registerLayout?.background as AnimationDrawable
    }
    private val spotsDialog: SpotsDialog by lazy {
        SpotsDialog(this,R.style.DialogStyleReg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        registerBtn.setOnClickListener({
            register(registerUserNameTxt.text.toString().trim(),
                    registerUserSurnameTxt.text.toString().trim(),
                    registerEmailTxt.text.toString().trim(),
                    registerPasswordTxt.text.toString(),
                    registerConfirmPasswordTxt.text.toString())
        })

        animationDrawable.setEnterFadeDuration(4500)
        animationDrawable.setExitFadeDuration(4500)
        animationDrawable.start()
    }

    private fun register(userName: String, userSurname: String, email: String, pass: String,
                         passConfirm: String) {
        if (
                TextUtils.isEmpty(userName) ||
                TextUtils.isEmpty(userSurname) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(pass) ||
                TextUtils.isEmpty(passConfirm)
        ) {
            Toast.makeText(this, getString(R.string.complete_fields), Toast.LENGTH_SHORT).show()
            return
        }
        else if (pass != passConfirm) {
            Toast.makeText(this, getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show()
            return
        }
        else {
            spotsDialog.show()

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val usersReference: DatabaseReference = dbRef.child("Users")
                            val userId: String = mAuth.currentUser?.uid ?: ""
                            val userIdRef: DatabaseReference = usersReference.child(userId)
                            userIdRef.run{
                                child("Name").setValue(userName)
                                child("Surname").setValue(userSurname)
                                child("Email").setValue(email)
                                push()
                            }

                            mAuth.currentUser?.sendEmailVerification()
                            Toast.makeText(
                                    this, getString(R.string.verification_email), Toast.LENGTH_SHORT
                            ).show()
                            spotsDialog.dismiss()

                            setResult(Activity.RESULT_OK,Intent())
                            finish()
                        } else {
                            spotsDialog.dismiss()

                            Toast.makeText(
                                    this, getString(R.string.acc_creation_failed), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
