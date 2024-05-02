package com.sujanix.cruxmdm.feature.content_management.presentation.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.sujanix.cruxmdm.R
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