package com.sujanix.cruxmdm.presentation.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.presentation.adapter.ViewPagerAdapter
import com.sujanix.cruxmdm.databinding.FragmentAppStoreBinding
import com.sujanix.cruxmdm.presentation.view.activity.MainActivity
import okhttp3.internal.notify

class AppStoreFragment: Fragment(R.layout.fragment_app_store) {

    private var _binding: FragmentAppStoreBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAppStoreBinding.bind(view)
        val fragmentList = listOf(AllAppFragment(), InstalledAppFragment(), UpdateAppFragment())
        val viewPagerAdapter = ViewPagerAdapter(fragmentList, this)
        (activity as MainActivity).supportActionBar!!.show()
        binding.apply {
            viewpager.apply {
                adapter = viewPagerAdapter
            }

            TabLayoutMediator(tabLayout, viewpager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = "All Apps"

                    }

                    1 -> {
                        tab.text = "Installed"
//                        tab.getOrCreateBadge().number = 8
                    }

                    2 -> {
                        tab.text = "Enterprise Apps"

                    }
                }
            }.attach()

            topAppBar.title = "App Catalog"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}