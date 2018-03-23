package com.example.konrad.rocketfuel.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.exercise_row.view.*


class ExerciseViewHolder(private var mview: View) : RecyclerView.ViewHolder(mview) {

    fun setTitle(title: String) {
        val post_title = mview.post_title_exe
        post_title.text = title
    }

    fun setTimestamp(timestamp: String) {
        val post_timestamp = mview.post_timestamp_exe
        post_timestamp.text = timestamp
    }

    fun setImg(context: Context, image: String) {
        val post_image = mview.post_img_exe
        Picasso.with(context).load(image).into(post_image)
    }
}