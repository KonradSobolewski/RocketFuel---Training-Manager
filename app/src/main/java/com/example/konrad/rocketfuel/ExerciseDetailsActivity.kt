package com.example.konrad.rocketfuel

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_exercise_details.*

class ExerciseDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_details)

        fab.setOnClickListener { view ->
            startActivity(Intent(this,UploadExercise::class.java))
        }
    }

}