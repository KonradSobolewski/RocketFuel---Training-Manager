package com.example.konrad.rocketfuel.ViewModels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.example.konrad.rocketfuel.Models.UserDataModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeViewModel : ViewModel() {
    val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val userModelLiveData = MutableLiveData<UserDataModel>()

    fun userModel() = userModelLiveData

    fun setUserData() {
        mDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                println(error?.message)
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                val userUid = mAuth.currentUser?.uid ?: ""
                if (snapshot?.hasChild(userUid) != true)
                    return
                val currentUserSnapshot: DataSnapshot = snapshot.child(userUid)
                val userName = currentUserSnapshot.child("Name").value.toString() + " " +
                        currentUserSnapshot.child("Surname").value.toString()
                val userEmail = currentUserSnapshot.child("Email").value.toString()
                val userImage = currentUserSnapshot.child("Image").value.toString()
                userModelLiveData.value = UserDataModel(userName, userEmail, userImage)
            }
        })
    }

    fun signOut() {
        mAuth.signOut()
    }
}