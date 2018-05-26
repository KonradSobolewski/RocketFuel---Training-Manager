package com.example.konrad.rocketfuel.Adapters

import android.app.Activity
import android.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.konrad.rocketfuel.Models.CalendarItem
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
        viewHolder.run {
            setTitle(model.title)
            setDay(model.day)
            setMonth(model.month)
            setDescription(model.desc)

            itemView.setOnLongClickListener {
                AlertDialog.Builder(activity).run {
                    setTitle("Deleting event")
                    setMessage("Are you sure you want to delete event?")
                    setPositiveButton("Do it!", { _, _ ->
                        notifyItemRangeChanged(position, 1, model)
                    })
                    setNegativeButton("Cancel", { dialog, _ -> dialog.dismiss() })
                    show()
                }
                true
            }
        }
    }
}