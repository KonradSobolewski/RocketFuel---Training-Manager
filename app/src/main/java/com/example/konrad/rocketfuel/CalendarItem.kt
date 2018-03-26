package com.example.konrad.rocketfuel

import java.io.Serializable

/**
 * Created by Konrad on 25.03.2018.
 */
class CalendarItem(
        val title : String = "Error",
        val day : String = "Error",
        val month : String = "Error",
        val desc : String = "Error",
        val eventID : String = ""
): Serializable
