package com.sujanix.cruxmdm.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.data.model.Application
import com.sujanix.cruxmdm.data.model.request.OrganizationData
import com.sujanix.cruxmdm.data.model.application_data.ApplicationDataApi
import com.sujanix.cruxmdm.data.model.request.LoginData
import com.sujanix.cruxmdm.data.model.request.RegisterData
import com.sujanix.cruxmdm.data.model.response.RegisterResponse
import com.sujanix.cruxmdm.data.model.response.selfHosted.SelfHostedApplication
import com.sujanix.cruxmdm.data.repository.CruxRepository
import com.sujanix.cruxmdm.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CruxViewModel @Inject constructor(
    private val repository: CruxRepository
): ViewModel() {

    private val _loginResponse: MutableLiveData<Resource<RegisterResponse>> = MutableLiveData()
    val loginResponse: LiveData<Resource<RegisterResponse>>
        get() = _loginResponse

    private val _appList = MutableSharedFlow<Resource<List<ApplicationDataApi?>>>()
    val appList = _appList.asSharedFlow()

    private val _selfHostedAppList = MutableSharedFlow<Resource<SelfHostedApplication>>()
    val selfHostedAppList = _selfHostedAppList.asSharedFlow()

    fun registerUser(registerData: RegisterData){
        viewModelScope.launch {
            repository.registerUser(registerData)
        }
    }

    fun loginUser(
        loginData: LoginData
    ) = viewModelScope.launch {
        _loginResponse.value = Resource.Loading
        _loginResponse.value = repository.loginUser(loginData)
    }

    fun getUserData() = repository.getUserData()

    fun saveUserData(registerData: RegisterData) {
        viewModelScope.launch {
            repository.saveUserData(registerData)
        }
    }

    fun clearUserData() {
        viewModelScope.launch {
            repository.clearUserData()
        }
    }

    fun getEnrollmentDevice() = repository.getEnrollmentDevice()

    fun setDeviceEnrolled(){
        viewModelScope.launch {
            repository.setDeviceEnrolled()
        }
    }

    fun getDeviceId() = repository.getDeviceId()

    fun setDeviceId(deviceId: String){
        viewModelScope.launch {
            repository.setDeviceId(deviceId)
        }
    }

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