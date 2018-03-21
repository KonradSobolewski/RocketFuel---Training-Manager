package com.example.konrad.rocketfuel.HomeFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.konrad.rocketfuel.CategoryItem
import com.example.konrad.rocketfuel.ExerciseDetailsActivity
import com.example.konrad.rocketfuel.R
import com.example.konrad.rocketfuel.ViewHolder.CategoryViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CategoryFragment : Fragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val Instance: CategoryFragment = CategoryFragment()
        fun getInstance(): Fragment? {
            return Instance
        }
    }

    private var recyclerView : RecyclerView? = null

    private var mDatabaseReference : DatabaseReference? =
            FirebaseDatabase.getInstance().reference.child("Category")

    private var options : FirebaseRecyclerOptions<CategoryItem>? =
            FirebaseRecyclerOptions.Builder<CategoryItem>()
                    .setQuery(mDatabaseReference,CategoryItem::class.java)
                    .build()

    private var mAdapter : FirebaseRecyclerAdapter<CategoryItem, CategoryViewHolder>? =
            object : FirebaseRecyclerAdapter<CategoryItem,CategoryViewHolder>(options) {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CategoryViewHolder {
            val mView : View? = LayoutInflater.from(parent?.context)
                    .inflate(R.layout.category_row,parent,false)
            return CategoryViewHolder(mView!!)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder?, position: Int,
                                      model: CategoryItem?) {
            holder?.setTitle(model?.title!!)
            holder?.setDescritopn(model?.description!!)
            holder?.setImage(context, model?.image!!)
            Log.d("position", Integer.toString(position))

            holder?.itemView?.setOnClickListener {

                startActivity(
                        Intent(activity, ExerciseDetailsActivity::class.java)
                                .putExtra("title", model?.title!!)
                )
            }
            holder?.itemView?.setOnLongClickListener({
                show()
            })

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view : View = inflater!!.inflate(
                R.layout.fragment_category, container, false
        )
        val mLayoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.recycleCategory)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = mLayoutManager
        mAdapter!!.startListening()
        recyclerView?.adapter = mAdapter

        return view
    }

    override fun onStart() {
        super.onStart()
        if(mAdapter != null){
            mAdapter!!.startListening()
        }
    }

    override fun onResume() {
        if(mAdapter != null){
            mAdapter!!.startListening()
        }
        super.onResume()
    }

    override fun onStop() {
        if(mAdapter != null){
            mAdapter!!.stopListening()
        }
        super.onStop()
    }

    fun show():Boolean{
        var snackbar:Snackbar= Snackbar.make(view!!,"dpupa",500)
        snackbar.show()
        return true
    }
}


