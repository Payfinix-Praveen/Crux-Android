package com.sujanix.cruxmdm.feature.device_detail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.sujanix.cruxmdm.feature.device_detail.data.repository.DeviceDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CruxViewModel @Inject constructor(
    private val repository: DeviceDetailRepository
): ViewModel() {

}