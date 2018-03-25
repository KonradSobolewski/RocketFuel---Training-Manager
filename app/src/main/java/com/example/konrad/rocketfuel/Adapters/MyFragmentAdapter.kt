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
        return when (position) {
            0 -> CategoryFragment.getInstance()
            1 -> RecentFragment.getInstance()
            2 -> NewsFragment.getInstance()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Category"
            1 -> "Recent"
            2 -> "News"
            else -> ""
        }
    }
}