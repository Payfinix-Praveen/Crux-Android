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
import com.sujanix.cruxmdm.feature.common.utlis.Constant.APP_CATEGORY_ALL_APPS
import com.sujanix.cruxmdm.feature.common.utlis.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllAppFragment: Fragment(R.layout.fragment_all_app) {

    private var _binding: FragmentAllAppBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppCatalogViewmodel by viewModels()

    private val appAdapter: AppCatalogAdapter by lazy {
        AppCatalogAdapter(
            requireContext(),
            APP_CATEGORY_ALL_APPS,
        )
    }
    private var appList: List<Application>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAllAppBinding.bind(view)

        viewModel.getApplicationList(OrganizationData(enterprise_id = "enterprises/LC01d3znq7"))
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
            viewModel.appList.collect { response ->
                when (response) {
                    is Resource.Success -> {
                        //add mapper
                        Log.d("applist", "getApplicationList: ${response.value}")
                        appList = response.value.map { applicationDataApi ->
                            applicationDataApi?.toApplication()!!
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
}