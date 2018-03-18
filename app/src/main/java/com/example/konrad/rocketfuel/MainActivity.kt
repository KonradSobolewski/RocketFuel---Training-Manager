package com.example.konrad.rocketfuel

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Handler().postDelayed({
            val mainIntent = Intent(this,HomeActivity::class.java)
            startActivity(mainIntent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()

        },500)

    }
}
