package com.sujanix.cruxmdm.feature.auth.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.feature.auth.data.model.request.LoginData
import com.sujanix.cruxmdm.feature.auth.data.model.request.RegisterData
import com.sujanix.cruxmdm.feature.auth.data.model.response.RegisterResponse
import com.sujanix.cruxmdm.feature.auth.data.repository.AuthRepository
import com.sujanix.cruxmdm.feature.common.utlis.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewmodel @Inject constructor(
    private val repository: AuthRepository
): ViewModel() {

    private val _loginResponse: MutableLiveData<Resource<RegisterResponse>> = MutableLiveData()
    val loginResponse: LiveData<Resource<RegisterResponse>>
        get() = _loginResponse

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

}