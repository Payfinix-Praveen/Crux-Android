package com.sujanix.cruxmdm.feature.common.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.feature.app_catalog.presentation.viewmodel.AppCatalogViewmodel
import com.sujanix.cruxmdm.feature.auth.data.model.request.RegisterData
import com.sujanix.cruxmdm.feature.common.data.repository.CruxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CruxViewModel @Inject constructor(
    private val repository: CruxRepository
): ViewModel() {

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

    fun getDeviceId() = repository.getDeviceId()

    fun setDeviceId(deviceId: String){
        viewModelScope.launch {
            repository.setDeviceId(deviceId)
        }
    }
}