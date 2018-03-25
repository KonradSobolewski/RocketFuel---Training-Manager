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

class EventCalenderActivity : AppCompatActivity(){

    private var   mDataListener : DatePickerDialog.OnDateSetListener? = null

    private var mDay : String?= null
    private var mYear : String?= null
    private var mMonth : String?= null

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

        mDataListener  = DatePickerDialog.OnDateSetListener { p0, year, month, day ->
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

        submitEventCalendar.setOnClickListener {
            if(dateTitleCalendar.text!=null && dateDescCalendar.text!=null &&
                    mDay!=null && mMonth!=null) {
                val item =  CalendarItem(dateTitleCalendar.text.toString(),
                        mDay.toString(), mMonth.toString(), dateDescCalendar.text.toString())
                startActivity(Intent(this,CalendarActivity::class.java)
                        .putExtra("calendarItem",item))
                finish()
            }else{
                Toast.makeText(this,"Complete all fields",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}
