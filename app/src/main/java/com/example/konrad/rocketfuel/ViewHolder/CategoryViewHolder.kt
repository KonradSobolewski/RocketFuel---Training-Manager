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
        val postTitle: TextView = mview.post_title
        postTitle.text = title
    }

    fun setDescription(description : String){
        val postDesc: TextView = mview.post_desc
        postDesc.text = description
    }

    fun setImage(context: Context, img : String){
        val postImage: ImageView = mview.post_img
        Picasso.with(context).load(img).into(postImage)
    }
}