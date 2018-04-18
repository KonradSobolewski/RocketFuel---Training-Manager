package com.example.konrad.rocketfuel

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_event_calender.*
import java.util.*

class EventCalenderActivity : AppCompatActivity() {

    private val mDataListener : DatePickerDialog.OnDateSetListener by lazy {
        initDataListener()
    }

    private var mDay: String? = null
    private var mMonth: String? = null
    private var mYear: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_calender)

        pickDateCalendar.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dialog = DatePickerDialog(
                    this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDataListener,
                    year,month,day)
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        submitEventCalendar.setOnClickListener {
            if (dateTitleCalendar.text != null && dateDescCalendar.text != null &&
                    mDay != null && mMonth != null) {
                val item = CalendarItem(
                        dateTitleCalendar.text.toString(),
                        mDay.toString(), mMonth.toString(), mYear.toString(),
                        dateDescCalendar.text.toString()
                )
                startActivity(Intent(this,CalendarActivity::class.java)
                        .putExtra("calendarItem",item))
                finish()
            } else {
                Toast.makeText(
                        this,
                        getString(R.string.complete_all_fields_prompt),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initDataListener() = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        mDay = day.toString()
        if(mDay!!.toInt()<10){
            mDay = "0$mDay"
        }
        mMonth = (month+1).toString()
        if(mMonth!!.toInt()<10){
            mMonth = "0$mMonth"
        }
        mYear = year.toString()
        dateCalendar.text = "$mDay/$mMonth/$mYear"
    }
}
