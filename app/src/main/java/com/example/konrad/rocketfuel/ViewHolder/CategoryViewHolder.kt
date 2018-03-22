package com.example.konrad.rocketfuel.ViewHolder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.category_row.view.*

class CategoryViewHolder(private var mview: View) : RecyclerView.ViewHolder(mview){

    fun setTitle(title : String){
        val post_title: TextView = mview.post_title
        post_title.text = title
    }

    fun setDescritopn(description : String){
        val post_desc: TextView = mview.post_desc
        post_desc.text = description
    }

    fun setImage(context: Context, img : String){
        val post_image: ImageView = mview.post_img
        Picasso.with(context).load(img).into(post_image)
    }
}