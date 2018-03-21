package com.example.konrad.rocketfuel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.konrad.rocketfuel.ViewHolder.ExerciseViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_exercise_details.*
import kotlinx.android.synthetic.main.content_exercise_details.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class ExerciseDetailsActivity : AppCompatActivity() {

    private var mDatabaseReference: DatabaseReference? = null

    private var title : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_details)
        title = intent.extras.getString("title")

        mDatabaseReference = FirebaseDatabase.getInstance().reference.child("Exercises").child(title)
        mDatabaseReference!!.keepSynced(true)

        exercisesRecycleView.setHasFixedSize(true)
        exercisesRecycleView.layoutManager = LinearLayoutManager(this)
        logRecycleView()

        fab.setOnClickListener {
            startActivity(Intent(this,UploadExerciseToCategory::class.java)
                    .putExtra("title",title))
        }
    }

    private fun logRecycleView(){

        val options = FirebaseRecyclerOptions.Builder<ExerciseItem>()
                .setQuery(mDatabaseReference,ExerciseItem::class.java)
                .build()

        val mAdapter = object : FirebaseRecyclerAdapter<ExerciseItem, ExerciseViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ExerciseViewHolder{
                val mView : View? = LayoutInflater.from(parent?.context)
                        .inflate(R.layout.exercise_row,parent,false)
                return ExerciseViewHolder(mView!!)
            }
            override fun onBindViewHolder(holder: ExerciseViewHolder?, position: Int,
                                          model: ExerciseItem?) {
                holder?.setTitle(model?.title!!)
                holder?.setDesc(model?.description!!)
                holder?.setPrompt(model?.prompts!!)
                holder?.setImg(this@ExerciseDetailsActivity, model?.image!!)
            }
        }
        mAdapter.startListening()
        exercisesRecycleView.adapter = mAdapter
    }

    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}