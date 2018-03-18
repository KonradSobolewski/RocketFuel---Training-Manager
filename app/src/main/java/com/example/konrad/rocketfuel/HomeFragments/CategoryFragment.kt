package com.example.konrad.rocketfuel.HomeFragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.konrad.rocketfuel.R


/**
 * A simple [Fragment] subclass.
 */
class CategoryFragment : Fragment() {

    companion object {
        private val Instance: CategoryFragment = CategoryFragment()
        fun getInstance(): Fragment? {
            return Instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_category, container, false)
    }

}
