package com.sujanix.cruxmdm.presentation.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.presentation.adapter.ViewPagerAdapter
import com.sujanix.cruxmdm.databinding.FragmentAppStoreBinding
import com.sujanix.cruxmdm.databinding.FragmentContentBinding

class ContentFragment: Fragment(R.layout.fragment_content) {

    private var _binding: FragmentContentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentContentBinding.bind(view)

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}