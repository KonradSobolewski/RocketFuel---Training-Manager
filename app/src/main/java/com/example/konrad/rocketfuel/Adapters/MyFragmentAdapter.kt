package com.example.konrad.rocketfuel.Adapters

import android.content.Context
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.konrad.rocketfuel.HomeFragments.CategoryFragment
import com.example.konrad.rocketfuel.HomeFragments.NewsFragment
import com.example.konrad.rocketfuel.HomeFragments.RecentFragment

/**
 * Created by Konrad on 18.03.2018.
 */
class MyFragmentAdapter(fm: FragmentManager, context: Context) : FragmentPagerAdapter(fm) {

    private var context: Context? = context

    override fun getItem(position: Int): android.support.v4.app.Fragment? {
        when (position) {
            0 -> return CategoryFragment.getInstance()
            1 -> return RecentFragment.getInstance()
            2 -> return NewsFragment.getInstance()
            else -> return null
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        when (position) {
            0 -> return "Category"
            1 -> return "Recent"
            2 -> return "News"
        }
        return ""
    }
}