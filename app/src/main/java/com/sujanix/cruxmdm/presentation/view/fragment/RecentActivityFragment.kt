package com.sujanix.cruxmdm.presentation.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.FragmentNotificationBinding
import com.sujanix.cruxmdm.presentation.adapter.NotificationsAdapter

class RecentActivityFragment : Fragment(R.layout.fragment_notification) {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val activitiesAdapter by lazy {
        NotificationsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentNotificationBinding.bind(view)

        binding.rvNotification.apply {
            adapter = activitiesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

    }

}