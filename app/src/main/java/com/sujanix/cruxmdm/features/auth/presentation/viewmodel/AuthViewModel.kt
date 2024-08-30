package com.sujanix.cruxmdm.features.auth.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.sujanix.cruxmdm.features.auth.data.model.request.LoginData
import com.sujanix.cruxmdm.features.auth.data.model.request.RegisterData
import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData
import com.sujanix.cruxmdm.features.auth.data.model.response.EnterpriseData
import com.sujanix.cruxmdm.features.auth.data.model.response.LoginResponse
import com.sujanix.cruxmdm.features.auth.data.model.response.RegisterResponse
import com.sujanix.cruxmdm.features.auth.data.repository.AuthRepository
import com.sujanix.cruxmdm.features.core.utlis.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
): ViewModel() {

    private val _googleAccountDetails: MutableLiveData<GoogleSignInAccount?> = MutableLiveData()
    val googleAccountDetails: LiveData<GoogleSignInAccount?>
        get() = _googleAccountDetails

    private val _registerResponse: MutableLiveData<Resource<RegisterResponse>> = MutableLiveData()
    val registerResponse: LiveData<Resource<RegisterResponse>>
        get() = _registerResponse

    private val _loginResponse: MutableLiveData<Resource<LoginResponse>> = MutableLiveData()
    val loginResponse: LiveData<Resource<LoginResponse>>
        get() = _loginResponse


    private val _enterpriseListResponse: MutableLiveData<Resource<EnterpriseData>> = MutableLiveData()
    val enterpriseListResponse: LiveData<Resource<EnterpriseData>>
        get() = _enterpriseListResponse

    fun registerUser(registerData: RegisterData) = viewModelScope.launch {
        _registerResponse.value = Resource.Loading
        _registerResponse.value = repository.registerUser(registerData)
    }

    fun loginUser(
        loginData: LoginData
    ) = viewModelScope.launch {
        _loginResponse.value = Resource.Loading
        _loginResponse.value = repository.loginUser(loginData)
    }

    fun saveUserData(deviceUserData: DeviceUserData) {
        viewModelScope.launch {
            repository.saveDeviceUserData(deviceUserData)
        }
    }

    fun getEnterpriseList(){
        viewModelScope.launch {
            _enterpriseListResponse.value = Resource.Loading
            _enterpriseListResponse.value = repository.getEnterpriseList()
        }
    }

    fun setGoogleAccount(acct: GoogleSignInAccount?) {
        _googleAccountDetails.value = acct
    }

    fun getUserData() = repository.getDeviceUserData()
}