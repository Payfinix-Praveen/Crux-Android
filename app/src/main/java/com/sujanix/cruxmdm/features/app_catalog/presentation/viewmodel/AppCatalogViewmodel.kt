package com.sujanix.cruxmdm.features.app_catalog.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.RequestedApp
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.toRequestedApp
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application
import com.sujanix.cruxmdm.features.app_catalog.data.model.response.application_data.ApplicationDataApi
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.app_catalog.data.model.response.selfHosted.SelfHostedApplication
import com.sujanix.cruxmdm.features.app_catalog.data.repository.AppCatalogRepository
import com.sujanix.cruxmdm.features.core.data.model.response.ApkSignedUrlResponse
import com.sujanix.cruxmdm.features.core.utlis.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppCatalogViewmodel @Inject constructor(
    private val repository: AppCatalogRepository
): ViewModel() {

    private val _appList = MutableSharedFlow<Resource<List<ApplicationDataApi?>>>()
    val appList = _appList.asSharedFlow()

    private val _selfHostedAppList = MutableSharedFlow<Resource<SelfHostedApplication>>()
    val selfHostedAppList = _selfHostedAppList.asSharedFlow()

    private val _apkSignedUrlResponse = MutableSharedFlow<Resource<ApkSignedUrlResponse>>()
    val apkSignedUrlResponse = _apkSignedUrlResponse.asSharedFlow()

    private val _requestedAppList = MutableSharedFlow<List<RequestedApp>>()
    val requestedAppList = _requestedAppList.asSharedFlow()

    fun getApplicationList(enterpriseId: OrganizationData){
        viewModelScope.launch {
            Log.d("FATAL", "getApplicationList: Called")
            _appList.emit(Resource.Loading)
            _appList.emit(repository.getApplicationList(enterpriseId))
        }
    }

    fun getSelfHostedApplicationListServer(enterpriseId: String, deviceId: String){
        viewModelScope.launch {
            Log.d("FATAL", "getApplicationList: Called")
            _selfHostedAppList.emit(Resource.Loading)
            _selfHostedAppList.emit(repository.getSelfHostedApplicationList(enterpriseId, deviceId))
        }
    }

    fun getApkSignedUrl(enterpriseId: String, packageName: String){
        viewModelScope.launch {
            Log.d("FATAL", "getApplicationList: Called")
            _selfHostedAppList.emit(Resource.Loading)
            _selfHostedAppList.emit(repository.getSelfHostedApplicationList(enterpriseId, packageName))
        }
    }

    fun insertApp(application: Application){
        viewModelScope.launch {
            repository.insertApplication(application)
        }
    }

    suspend fun getAllApps() = repository.getAllApplication()


    suspend fun getSelfHostedApplicationListLocal() = repository.getSelfHostedApplications()

    suspend fun deleteAllApplications(){
        repository.deleteAllApplication()
    }

    suspend fun apkDownloaded(filePath: String, packageName: String, downloaded: Boolean = true) {
        repository.apkDownloaded(filePath, packageName, downloaded)
    }

    fun setAppInstalled(packageName: String, isInstalled: Boolean){
        viewModelScope.launch{
            repository.setInstalled(packageName, isInstalled)
        }
    }

    fun setApplicationUpdateAvailable(packageName: String, updated: Boolean){
        viewModelScope.launch{
            repository.setApplicationUpdateAvailable(packageName, updated)
        }
    }

    fun getDeviceUserData() = repository.getUserData()

    fun getDeviceId() = repository.getDeviceId()

    fun updateApp(toApplication: Application, pkgName: String) {
        viewModelScope.launch {
            repository.updateApplication(toApplication, pkgName, true)
        }
    }

    fun insertRequestedApp(requestedApp: RequestedApp) = viewModelScope.launch {
        repository.insertRequestApp(requestedApp)
    }

    fun getRequestedAppList() = viewModelScope.launch {
        _requestedAppList.emit(repository.getRequestAppList())
    }

    fun deleteRequestedApp(packageName: String) = viewModelScope.launch {
        repository.deleteRequestedApp(packageName)
    }

    fun getRequestedAppByPackageName(packageName: String) = viewModelScope.launch {
        repository.getRequestedAppByPackageName(packageName)
    }

    fun deleteAllRequestedApps() = viewModelScope.launch {
        repository.deleteAllRequestedApps()
    }

}