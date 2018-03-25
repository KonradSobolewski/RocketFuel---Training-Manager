package com.example.konrad.rocketfuel.Adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.konrad.rocketfuel.CalenderItem
import com.example.konrad.rocketfuel.R
import com.example.konrad.rocketfuel.ViewHolder.CalenderViewHolder


class CalendarAdapter(
        val activity : Activity ,
        val calenderList : ArrayList<CalenderItem>

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder{
        val view = LayoutInflater.from(activity).inflate(R.layout.calender_row, parent, false)
        return CalenderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return calenderList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder = holder as CalenderViewHolder
        val model : CalenderItem = calenderList[position]
        viewHolder.setTitle(model.title)
        viewHolder.setDay(model.day)
        viewHolder.setMonth(model.month)
        viewHolder.setDescription(model.desc)
    }
}