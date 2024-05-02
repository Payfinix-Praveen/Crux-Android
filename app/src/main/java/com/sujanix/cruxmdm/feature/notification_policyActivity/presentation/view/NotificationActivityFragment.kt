package com.sujanix.cruxmdm.feature.notification_policyActivity.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.FragmentNotificationActivityBinding
import com.sujanix.cruxmdm.feature.common.presentation.adapter.ViewPagerAdapter
import com.sujanix.cruxmdm.feature.common.presentation.view.MainActivity


class NotificationActivityFragment : Fragment(R.layout.fragment_notification_activity) {

    private var _binding: FragmentNotificationActivityBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentNotificationActivityBinding.bind(view)

        val fragmentList = listOf(NotificationFragment(), RecentActivityFragment())
        val viewPagerAdapterf = ViewPagerAdapter(fragmentList, this)
        (activity as MainActivity).supportActionBar!!.show()
        binding.apply {
            viewpager.apply {
                adapter = viewPagerAdapterf
            }
            TabLayoutMediator(tabLayout, viewpager) { tab, position ->
                when(position) {
                    0 -> { tab.text = getString(R.string.notification)}
                    1 -> { tab.text = getString(R.string.recent_activity)}
//                    2 -> { tab.text = "Updates"}
                }
            }.attach()

            toolBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

    }
}