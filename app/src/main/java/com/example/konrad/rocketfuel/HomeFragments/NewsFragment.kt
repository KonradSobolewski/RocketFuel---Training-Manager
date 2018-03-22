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
class NewsFragment : Fragment() {

    companion object {
        private var Instance: NewsFragment = NewsFragment()
        fun getInstance(): Fragment {
            return Instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater?.inflate(R.layout.fragment_news, container, false)
    }

}
