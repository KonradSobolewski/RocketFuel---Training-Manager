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

    private var mDatabase: DatabaseReference? = null

    private var mAuth: FirebaseAuth? = null
    var userName: String? = null
    var userEmail: String? = null
    var userImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        //Add adapter to pageView
        val myFragAdapter = MyFragmentAdapter(supportFragmentManager,this)
        homeViewPager.adapter = myFragAdapter
        homeTab.setupWithViewPager(homeViewPager)
        val root : View = homeTab.getChildAt(0)
        if( root is LinearLayout ){
            root.showDividers  = LinearLayout.SHOW_DIVIDER_MIDDLE
            val drawable = GradientDrawable()
            drawable.setColor(ContextCompat.getColor(this, R.color.colorWhite))
            drawable.setSize(3,2)
            root.dividerPadding = 10
            root.dividerDrawable = drawable
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val usersReference = mDatabase!!.child("Users")
        usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                println(error!!.message)
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                val currentUserSnapshot: DataSnapshot? = snapshot?.child(mAuth?.currentUser?.uid)
                userName = currentUserSnapshot?.child("Name")?.value.toString() + " " +
                        currentUserSnapshot?.child("Surname")?.value.toString()
                userEmail = currentUserSnapshot?.child("Email")?.value.toString()
                userImage = currentUserSnapshot?.child("Image")?.value.toString()
                nav_view.getHeaderView(0).navbarHeaderID.text = userName
                nav_view.getHeaderView(0).navbarEmailID.text = userEmail
                if(userImage!=null)
                    Picasso.with(applicationContext)
                            .load(userImage)
                            .into(nav_view.getHeaderView(0).imagePersonIcon)
            }
        })
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
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
                startActivity(Intent(this,UploadExercise::class.java))
            }
            R.id.nav_training -> {

            }
            R.id.nav_calender -> {

            }
            R.id.nav_gallery -> {

            }
            R.id.nav_settings -> {

            }
            R.id.nav_logout-> {
                signOut()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun signOut():Boolean{
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
