package com.sujanix.cruxmdm.feature.app_catalog.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.feature.app_catalog.data.model.Application
import com.sujanix.cruxmdm.feature.app_catalog.data.model.response.application_data.ApplicationDataApi
import com.sujanix.cruxmdm.feature.common.data.model.OrganizationData
import com.sujanix.cruxmdm.feature.app_catalog.data.model.response.selfHosted.SelfHostedApplication
import com.sujanix.cruxmdm.feature.app_catalog.data.repository.AppCatalogRepository
import com.sujanix.cruxmdm.feature.common.utlis.Resource
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

    fun getApplicationList(enterpriseId: OrganizationData){
        viewModelScope.launch {
            Log.d("FATAL", "getApplicationList: Called")
            _appList.emit(Resource.Loading)
            _appList.emit(repository.getApplicationList(enterpriseId))
        }
    }

    fun getSelfHostedApplicationList(enterpriseId: OrganizationData){
        viewModelScope.launch {
            Log.d("FATAL", "getApplicationList: Called")
            _selfHostedAppList.emit(Resource.Loading)
            _selfHostedAppList.emit(repository.getSelfHostedApplicationList(enterpriseId))
        }
    }

    fun insertApp(application: Application){
        viewModelScope.launch {
            repository.insertApplication(application)
        }
    }

    suspend fun getAllApps() = repository.getAllApplication()

    suspend fun deleteAllApplications(){
        repository.deleteAllApplication()
    }

}