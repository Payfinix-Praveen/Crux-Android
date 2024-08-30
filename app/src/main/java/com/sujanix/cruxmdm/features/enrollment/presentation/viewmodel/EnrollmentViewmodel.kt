package com.sujanix.cruxmdm.features.enrollment.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.enrollment.data.model.request.EnrollmentData
import com.sujanix.cruxmdm.features.enrollment.data.model.response.EnterpriseGroupListDataResponse
import com.sujanix.cruxmdm.features.enrollment.data.model.response.GenerateQrCodeResponse
import com.sujanix.cruxmdm.features.enrollment.data.repository.EnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrollmentViewmodel @Inject constructor(
    private val repository: EnrollmentRepository
): ViewModel() {

    private val _enterpriseClientIdResponse: MutableLiveData<Resource<EnterpriseGroupListDataResponse>> = MutableLiveData()
    val enterpriseClientIdResponse: LiveData<Resource<EnterpriseGroupListDataResponse>>
        get() = _enterpriseClientIdResponse

    private val _generateQrCodeResponse: MutableLiveData<Resource<GenerateQrCodeResponse>> = MutableLiveData()
    val generateQrCodeResponse: LiveData<Resource<GenerateQrCodeResponse>>
        get() = _generateQrCodeResponse
    fun getEnrollmentDevice() = repository.getEnrollmentDevice()

    fun setDeviceEnrolled(){
        viewModelScope.launch {
            repository.setDeviceEnrolled()
        }
    }

    fun generateQrCode(enrollmentData: EnrollmentData) {
        viewModelScope.launch {
            _generateQrCodeResponse.value = Resource.Loading
            _generateQrCodeResponse.value = repository.generateQrCode(enrollmentData)
        }
    }

    fun getEnterpriseGroupList(enterpriseId: OrganizationData){
        viewModelScope.launch {
            _enterpriseClientIdResponse.value = Resource.Loading
            _enterpriseClientIdResponse.value = repository.getEnterpriseGroupList(enterpriseId)
        }

    }
}