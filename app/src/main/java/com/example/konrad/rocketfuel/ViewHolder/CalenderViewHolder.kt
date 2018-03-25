package com.example.konrad.rocketfuel.ViewHolder


import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.calender_row.view.*

class CalenderViewHolder(private var mview: View) : RecyclerView.ViewHolder(mview) {

    fun setTitle(title: String) {
        val postTitle = mview.titleCalender
        postTitle.text = title
    }

    fun setDay(day: String) {
        val postDay = mview.dayCalender
        postDay.text = day
    }

    fun setMonth(month: String) {
        val postMonth = mview.monthCalender
        postMonth.text = month
    }

    fun setDescription(description: String) {
        val postDesc = mview.descCalender
        postDesc.text = description
    }
}