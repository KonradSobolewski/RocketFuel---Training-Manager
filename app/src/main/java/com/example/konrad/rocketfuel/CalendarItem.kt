package com.example.konrad.rocketfuel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class CalendarItem(
        val title : String = "Error",
        val day : String = "Error",
        val month : String = "Error",
        val year : String = "Error",
        val desc : String = "Error",
        val eventID : String = ""
): Parcelable
