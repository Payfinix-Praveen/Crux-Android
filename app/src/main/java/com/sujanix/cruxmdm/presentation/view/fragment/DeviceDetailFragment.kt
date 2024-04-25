package com.sujanix.cruxmdm.presentation.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.data.model.DeviceDetail
import com.sujanix.cruxmdm.databinding.FragmentDeviceDetailBinding
import com.sujanix.cruxmdm.presentation.adapter.DeviceDetailAdapter
import com.sujanix.cruxmdm.util.getDeviceDetails
import com.sujanix.cruxmdm.util.getNetworkInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DeviceDetailFragment : Fragment(R.layout.fragment_device_detail) {

    private var _binding: FragmentDeviceDetailBinding? = null
    private val binding get() = _binding!!
    private val ddAdapter: DeviceDetailAdapter by lazy {
        DeviceDetailAdapter(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentDeviceDetailBinding.bind(view)

        binding.apply {
            toolBar.setNavigationOnClickListener { findNavController().navigateUp() }

            rvDeviceDetail.apply {
                adapter = ddAdapter
                layoutManager = GridLayoutManager(requireContext(), 2)
                setHasFixedSize(true)
            }
        }

        val deviceDetail = getDeviceDetails(requireContext(), requireActivity()).second
        ddAdapter.submitList(deviceDetail)
        Log.d("FATAL", "onViewCreated: ${deviceDetail.size}")
        CoroutineScope(Dispatchers.Main).launch {
            getNetworkInfo(requireContext()).collect { networkInfo ->

                if (networkInfo.isConnected) {
                    deviceDetail[4] = DeviceDetail(
                        R.drawable.ic_file,
                        "Network Details",
                        "Connected with ${networkInfo.networkType}"
                    )
                } else {
                    deviceDetail[4] = DeviceDetail(
                            R.drawable.ic_file,
                            "Network Details", networkInfo.networkType
                    )
                }

                ddAdapter.submitList(deviceDetail)
                ddAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}