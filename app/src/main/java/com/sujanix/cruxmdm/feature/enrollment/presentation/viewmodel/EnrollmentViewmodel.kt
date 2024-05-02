package com.sujanix.cruxmdm.feature.enrollment.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.feature.enrollment.data.repository.EnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrollmentViewmodel @Inject constructor(
    private val repository: EnrollmentRepository
): ViewModel() {

    fun getEnrollmentDevice() = repository.getEnrollmentDevice()

    fun setDeviceEnrolled(){
        viewModelScope.launch {
            repository.setDeviceEnrolled()
        }
    }
}