package com.example.konrad.rocketfuel

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import com.squareup.picasso.Picasso

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var userName: String = ""
    private var userEmail: String = ""
    private var userImage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

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

        navView.setNavigationItemSelectedListener(this)

        val usersReference = mDatabase.child("Users")
        usersReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                println(error?.message)
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                val userUid = mAuth.currentUser?.uid ?: ""
                if (snapshot?.hasChild(userUid) != true)
                    return
                val currentUserSnapshot: DataSnapshot = snapshot.child(userUid)
                userName = currentUserSnapshot.child("Name").value.toString() + " " +
                        currentUserSnapshot.child("Surname").value.toString()
                userEmail = currentUserSnapshot.child("Email").value.toString()
                userImage = currentUserSnapshot.child("Image").value.toString()
                navView.getHeaderView(0).navbarHeaderID.text = userName
                navView.getHeaderView(0).navbarEmailID.text = userEmail
                if (userImage != "")
                    Picasso.with(applicationContext)
                            .load(userImage)
                            .into(navView.getHeaderView(0).imagePersonIcon)
            }
        })
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return true
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
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
        return true
    }

    //change font
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
