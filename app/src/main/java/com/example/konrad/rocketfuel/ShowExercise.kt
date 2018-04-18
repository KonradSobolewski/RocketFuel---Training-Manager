package com.example.konrad.rocketfuel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_show_exercise.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper


class ShowExercise : AppCompatActivity() {
    private val mDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var title : String
    private lateinit var category : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_exercise)

        title = intent.extras.getString("title") ?: ""
        category = intent.extras.getString("category") ?: ""

        val exerciseReference: DatabaseReference = mDatabaseReference.child("Exercises")
                .child(category).child(title)
        exerciseReference.keepSynced(true)

        exerciseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                println(error?.message)
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot?) {
                    post_title_exe_details.text = snapshot?.child("title")?.value.toString()
                    post_desc_exe_details.text = "Description: ${snapshot?.child("description")?.value.toString()}"
                    post_prompts_exe_details.text = "Hints: ${snapshot?.child("prompts")?.value.toString()}"
                    post_timestamp_exe_details.text = snapshot?.child("timestamp")?.value.toString()
                    Picasso.with(applicationContext)
                            .load(snapshot?.child("image")?.value.toString())
                            .into(post_img_exe_details)
            }
        })
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
        super.onBackPressed()
    }

    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
