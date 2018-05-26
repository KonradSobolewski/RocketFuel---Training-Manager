package com.example.konrad.rocketfuel

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.example.konrad.rocketfuel.Adapters.MyFragmentAdapter
import com.example.konrad.rocketfuel.R.id.scan
import com.example.konrad.rocketfuel.ViewModels.HomeViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import com.squareup.picasso.Picasso

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewModel : HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        viewModel.userModel().observe(this, Observer {
            navView.setNavigationItemSelectedListener(this)
            navView.getHeaderView(0).navbarHeaderID.text = it?.userName
            navView.getHeaderView(0).navbarEmailID.text = it?.userEmail
            if (it?.userImage != "")
                Picasso.with(applicationContext)
                        .load(it?.userImage)
                        .into(navView.getHeaderView(0).imagePersonIcon)
        })
        viewModel.setUserData()

        //Add adapter to pageView
        homeViewPager.adapter = MyFragmentAdapter(supportFragmentManager, this)
        homeTab.setupWithViewPager(homeViewPager)
        val root: View = homeTab.getChildAt(0)
        if (root is LinearLayout) {
            val drawable = GradientDrawable().apply {
                setColor(ContextCompat.getColor(this@HomeActivity, R.color.colorWhite))
                setSize(3, 2)
            }
            root.run {
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                dividerPadding = 10
                dividerDrawable = drawable
            }
        }

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            finish()
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        scan -> {
            startActivity(Intent(this, QRcode::class.java
                ))
            true
        }
        else -> false
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_exercise -> {
                startActivity(Intent(this, UploadExercise::class.java))
            }
            R.id.nav_training -> {

            }
            R.id.nav_calender -> {
                startActivity(Intent(this, CalendarActivity::class.java))
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_settings -> {

            }
            R.id.nav_logout-> {
                signOut()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun signOut(): Boolean {
        viewModel.signOut()
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
        return true
    }

    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
