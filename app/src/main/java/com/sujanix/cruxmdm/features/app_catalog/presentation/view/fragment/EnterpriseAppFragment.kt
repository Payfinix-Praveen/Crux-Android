package com.sujanix.cruxmdm.features.app_catalog.presentation.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application.Companion.toApplication
import com.sujanix.cruxmdm.databinding.FragmentAllAppBinding
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.ApplicationEntity
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.ApplicationEntity.Companion.toApplication
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application.Companion.toApplicationEntity
import com.sujanix.cruxmdm.features.app_catalog.data.repository.AppCatalogRepository
import com.sujanix.cruxmdm.features.app_catalog.presentation.adapter.AppCatalogAdapter
import com.sujanix.cruxmdm.features.app_catalog.presentation.viewmodel.AppCatalogViewmodel
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.app_catalog.utlis.InstallUtils
import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.isPackageInstalled
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EnterpriseAppFragment: Fragment(R.layout.fragment_all_app) {

    private val TAG: String = "EnterpriseAppFragment"
    private var _binding: FragmentAllAppBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppCatalogViewmodel by activityViewModels()
    @Inject
    lateinit var repository: AppCatalogRepository

    private val appAdapter: AppCatalogAdapter by lazy {
        AppCatalogAdapter(
            requireContext(),
            Constant.APP_CATEGORY_ALL_APPS,
            repository
        )
    }
    private var appList: List<ApplicationEntity>? = null
    private var deviceUserData: DeviceUserData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAllAppBinding.bind(view)

        lifecycleScope.launch {
            viewModel.getDeviceUserData().collect{ userData ->
                viewModel.getDeviceId().collect { deviceId ->
                    Log.d(TAG, "onCreateView: $userData")
                    if (userData != null) {

                        appAdapter.setEnterpriseId(userData.enterprise_id)
                        if (deviceId != null) {

                            viewModel.getSelfHostedApplicationListServer(
                                userData.enterprise_id,
                                deviceId
                            )
                        } else {
                            Toast.makeText(requireContext(), "Device ID not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            appList = viewModel.getSelfHostedApplicationListLocal()
        }

        observeApplicationListResponse()

        binding.apply {
            rvAppCatalog.apply {
                adapter = appAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {

            val enterpriseAppList = viewModel.getAllApps()
                .map { it.toApplication() }
                .filter { it.type == Constant.SELF_HOSTED_APP}

            enterpriseAppList.forEach {
                if (isPackageInstalled(requireContext(), it.pkg)) {
                    viewModel.setAppInstalled(it.pkg!!, true)

                    if(it.versionCode != null) {
                        val updateAvailable = InstallUtils.checkApkNeedsUpdates(requireContext(), it)
                        viewModel.setApplicationUpdateAvailable(it.pkg, updateAvailable)
                    }
                }
            }
            appAdapter.submitList(enterpriseAppList)
        }
    }

    private fun observeApplicationListResponse() {
        lifecycleScope.launch {
            viewModel.selfHostedAppList.collect { response ->
                when (response) {
                    is Resource.Success -> {
                        //add mapper
                        if(response.value.STATUS){
                            val selfHostedAppList = response.value.data.map { it.toApplication() }

                            val newAppList = selfHostedAppList.filter { app ->
                                !InstallUtils.isInList(
                                    appList!!,
                                    app.toApplicationEntity()
                                )
                            }
                            Log.d(TAG, "observeApplicationListResponse: $newAppList")
                            downloadNewApps(newAppList)
                        } else {
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

    private suspend fun downloadNewApps(appList: List<Application>?) {

        appList?.forEach { app ->
//            InstallUtils.enqueueDownload(requireContext(), app){ filePath ->
//                if(filePath.endsWith(".apk", true)) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        viewModel.apkDownloaded(
//                            filePath,
//                            app.pkg!!,
//                            true
//                        )
//                    }
//                }
//            }
            val isAppPresent = appList.any { it.pkg == app.pkg }
            if(isAppPresent) viewModel.updateApp(app, app.pkg!!)
            else viewModel.insertApp(app)

            InstallUtils.downloadAndInstallApk(
                requireContext(),
                Application(
                    type = app.type,
                    name = app.name,
                    pkg = app.pkg,
                    url = app.url,
                )
            ) { filePath ->
                Log.d(TAG, "downloadNewApps: APK downloaded")
                if(filePath.endsWith(".apk", true)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.apkDownloaded(
                            filePath,
                            app.pkg!!,
                            true
                        )
                    }
                }
            }
        }
    }
}