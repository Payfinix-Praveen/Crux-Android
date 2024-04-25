package com.sujanix.cruxmdm.presentation.view.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.data.model.Application
import com.sujanix.cruxmdm.data.model.Application.Companion.toApplication
import com.sujanix.cruxmdm.data.model.Application.Companion.toApplicationEntity
import com.sujanix.cruxmdm.data.model.request.OrganizationData
import com.sujanix.cruxmdm.presentation.adapter.AppCatalogHomeAdapter
import com.sujanix.cruxmdm.databinding.FragmentHomeBinding
import com.sujanix.cruxmdm.presentation.view.activity.MainActivity
import com.sujanix.cruxmdm.presentation.viewmodel.CruxViewModel
import com.sujanix.cruxmdm.util.Constant
import com.sujanix.cruxmdm.util.InstallUtils
import com.sujanix.cruxmdm.util.Resource
import com.sujanix.cruxmdm.util.getDeviceDetails
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val appAdapter: AppCatalogHomeAdapter by lazy {
        AppCatalogHomeAdapter(requireContext())
    }
    private val viewModel: CruxViewModel by activityViewModels()
    private lateinit var preferences: SharedPreferences
    private var appList: List<Application?>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpAppCatalog()

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward= */ false)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)

        val deviceDetails = getDeviceDetails(requireContext(), requireActivity()).first
        val availableStorage = deviceDetails.getLong("AvailableStorage")
        val totalStorage = deviceDetails.getLong("TotalStorage")
//        val usedStorage = totalStorage - availableStorage

        Log.d("HomeFragment", "onViewCreated: $deviceDetails")
        preferences = (activity as MainActivity).getSharedPreferences(Constant.PREFERENCE_NAME, Context.MODE_PRIVATE)

        val radius = 10f
        val decorView = activity?.window?.decorView
        val rootView: ViewGroup = decorView?.findViewById(android.R.id.content)!!

        val windowBackground = decorView.background

        binding.apply {

//            blurView.setupWith(rootView, object : RenderScriptBlur(requireContext()) {})
//                .setBlurAutoUpdate(true)
//                .setBlurRadius(radius)
//                .setFrameClearDrawable(windowBackground)

            topAppBar.setNavigationOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
            }

            deviceDetailsTitle.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_deviceDetailFragment)
            }

            topAppBar.setOnMenuItemClickListener { menuItem ->
                Log.d("FATAL", "onCreateView: $menuItem")
                when(menuItem.itemId) {
                    R.id.notification -> { findNavController().navigate(R.id.action_homeFragment_to_notificationActivityFragment) }
                }
                true
            }

            deviceDetail.apply {
                tvDeviceName.text = deviceDetails.getString("DeviceName")
                tvNetwork.text = deviceDetails.getString("networkName")
                tvModel.text = deviceDetails.getString("MODEL")
                tvManufacturer.text = deviceDetails.getString("Manufacturer")
                tvStorage.text = "$totalStorage GB (Total)\n$availableStorage GB (Available)"
            }

            appCatalogTitle.setOnClickListener {
                Log.d("FATAL", "appCatalogTitle.setOnClickListener: clicked")
                lifecycleScope.launch {
                    viewModel.deleteAllApplications()
//                    viewModel.getApplicationList(
//                        OrganizationData(
//                            enterprise_id = "enterprises/LC01d3znq7"
//                        )
//                    )
                }
            }

            tvRecentActivityTitle.setOnClickListener {

            }
        }
        observeApplicationListResponse()
        observeSelfHostedApplicationListResponse()
    }

    private fun observeApplicationListResponse() {
        lifecycleScope.launch {
            viewModel.appList.collect { response ->
                when (response) {
                    is Resource.Success -> {
                        //add mapper
                        Log.d("applist", "getApplicationList: ${response.value}")
                        appList = response.value.reversed().take(10).map { applicationDataApi ->
                            applicationDataApi?.toApplication()
                        }
                        appAdapter.submitList(appList)
                    }

                    is Resource.Failure -> {
                        Log.d("applist", "getApplicationList: ${response.message}")
                    }

                    Resource.Loading -> {
                        Log.d("applist", "getApplicationList: Loading...")
                    }
                }
            }
        }
    }

    private fun observeSelfHostedApplicationListResponse() {
        lifecycleScope.launch {
            viewModel.appList.collect { response ->
                when (response) {
                    is Resource.Success -> {
                        //add mapper
                        Log.d("selfHostedApplist", "getApplicationList: ${response.value}")
                        response.value.filter {
                            !InstallUtils.isInList(
                                viewModel.getAllApps(),
                                it?.toApplication()?.toApplicationEntity()!!
                            )
                        }.forEach {
                            viewModel.insertApp(it?.toApplication()!!)
                        }
                    }

                    is Resource.Failure -> {
                        Log.d("selfHostedApplistDANISH", "getApplicationList: ${response.message}")
                    }

                    Resource.Loading -> {
                        Log.d("selfHostedApplistDANISHLOADING", "getApplicationList: Loading...")
                    }
                }
            }
        }
    }

    private fun setUpAppCatalog() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.rvAppCatalog.apply {
                adapter = appAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(true)
            }

            appAdapter.submitList(appList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}