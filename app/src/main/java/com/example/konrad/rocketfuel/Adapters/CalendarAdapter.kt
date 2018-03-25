package com.example.konrad.rocketfuel.Adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.konrad.rocketfuel.CalendarItem
import com.example.konrad.rocketfuel.R
import com.example.konrad.rocketfuel.ViewHolder.CalenderViewHolder


class CalendarAdapter(
        private val activity : Activity,
        private val calendarList : ArrayList<CalendarItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.calender_row, parent, false)
        return CalenderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return calendarList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder = holder as CalenderViewHolder
        val model : CalendarItem = calendarList[position]
        viewHolder.setTitle(model.title)
        viewHolder.setDay(model.day)
        viewHolder.setMonth(model.month)
        viewHolder.setDescription(model.desc)
    }
}