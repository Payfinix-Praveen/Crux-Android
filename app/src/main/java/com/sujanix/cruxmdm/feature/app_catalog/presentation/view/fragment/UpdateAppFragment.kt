package com.sujanix.cruxmdm.feature.app_catalog.presentation.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.feature.app_catalog.data.model.Application
import com.sujanix.cruxmdm.feature.app_catalog.data.model.Application.Companion.toApplication
import com.sujanix.cruxmdm.feature.common.data.model.OrganizationData
import com.sujanix.cruxmdm.databinding.FragmentAllAppBinding
import com.sujanix.cruxmdm.feature.app_catalog.presentation.adapter.AppCatalogAdapter
import com.sujanix.cruxmdm.feature.app_catalog.presentation.viewmodel.AppCatalogViewmodel
import com.sujanix.cruxmdm.feature.common.utlis.Constant
import com.sujanix.cruxmdm.feature.app_catalog.utlis.InstallUtils
import com.sujanix.cruxmdm.feature.common.utlis.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateAppFragment: Fragment(R.layout.fragment_all_app) {

    private var _binding: FragmentAllAppBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppCatalogViewmodel by viewModels()

    private val appAdapter: AppCatalogAdapter by lazy {
        AppCatalogAdapter(
            requireContext(),
            Constant.APP_CATEGORY_ALL_APPS,
        )
    }
    private var appList: List<Application>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAllAppBinding.bind(view)

        viewModel.getSelfHostedApplicationList(
            OrganizationData(
                enterprise_id = "enterprises/LC01d3znq7",
                device_id = "392e658a30aced6a")
        )
        observeApplicationListResponse()

        binding.apply {
            rvAppCatalog.apply {
                adapter = appAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
    }

    private fun observeApplicationListResponse() {
        lifecycleScope.launch {
            viewModel.selfHostedAppList.collect { response ->
                when (response) {
                    is Resource.Success -> {
                        //add mapper
                        Log.d("selfHostedApplist", "getApplicationList: ${response.value.data}")
                        if(response.value.data != null && response.value.STATUS){
                            appList = response.value.data.map { it.toApplication() }
                            appAdapter.submitList(appList)
                            downloadNewApps(appList)
                        }else{
                            //show no app distributed to this device banner
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

    private fun downloadNewApps(appList: List<Application>?) {
        InstallUtils.downloadAndInstallApk(requireContext(),
            Application(
                type = "Self Hosted Apps",
                name = "xyz",
                pkg = "com.sujanix.privatetest1"
        )
        )
        appList?.forEach { app ->
            InstallUtils.enqueueDownload(requireContext(), app)
        }
    }
}