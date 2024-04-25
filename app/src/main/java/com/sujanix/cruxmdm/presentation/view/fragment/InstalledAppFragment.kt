package com.sujanix.cruxmdm.presentation.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.data.model.Application
import com.sujanix.cruxmdm.data.model.Application.Companion.toApplication
import com.sujanix.cruxmdm.data.model.request.OrganizationData
import com.sujanix.cruxmdm.presentation.adapter.AppCatalogAdapter
import com.sujanix.cruxmdm.databinding.FragmentAllAppBinding
import com.sujanix.cruxmdm.presentation.viewmodel.CruxViewModel
import com.sujanix.cruxmdm.util.Constant.APP_CATEGORY_INSTALLED_APPS
import com.sujanix.cruxmdm.util.Resource
import com.sujanix.cruxmdm.util.isPackageInstalled
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InstalledAppFragment : Fragment(R.layout.fragment_all_app) {

    private var _binding: FragmentAllAppBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CruxViewModel by viewModels()
    private var appList: List<Application>? = null

    private val appAdapter: AppCatalogAdapter by lazy {
        AppCatalogAdapter(
            requireContext(),
            APP_CATEGORY_INSTALLED_APPS,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAllAppBinding.bind(view)

        observeApplicationListResponse()
        viewModel.getApplicationList(OrganizationData(enterprise_id = "enterprises/LC01d3znq7"))

        binding.apply {
            rvAppCatalog.apply {
                adapter = appAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

//        appAdapter.submitList((activity as MainActivity).applist)
    }

    private fun observeApplicationListResponse() {
        lifecycleScope.launch {
            viewModel.appList.collect { response ->
                when (response) {
                    is Resource.Success -> {
                        //add mapper
                        Log.d("applist", "getApplicationList: ${response.value}")
                        appList = response.value
                            .map { applicationDataApi -> applicationDataApi?.toApplication()!! }
                            .filter { isPackageInstalled(requireContext(), it.pkg) }

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