package com.example.konrad.rocketfuel.Models

import android.app.Activity
import android.content.Context
import com.example.konrad.rocketfuel.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class RegisterModel {
    fun register(ctx: Context,
                 mAuth: FirebaseAuth,
                 dbRef: DatabaseReference,
                 userName: String,
                 userSurname: String,
                 email: String,
                 pass: String,
                 onSpotsShow: (Boolean) -> Unit,
                 onError: (String) -> Unit,
                 onSuccess:() -> Unit) {

        onSpotsShow(true)
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(ctx as Activity) { task ->
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
                        onSpotsShow(false)
                        onSuccess()

                    } else {
                        onSpotsShow(false)
                        onError(ctx.getString(R.string.acc_creation_failed))
                    }
                }
    }
}
