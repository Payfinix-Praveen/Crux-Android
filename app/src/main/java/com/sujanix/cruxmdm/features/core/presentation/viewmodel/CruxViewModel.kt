package com.sujanix.cruxmdm.features.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.GeoJSON
import com.sujanix.cruxmdm.features.core.data.model.fencing.Fencing
import com.sujanix.cruxmdm.features.core.data.model.fencing.GeoJson
import com.sujanix.cruxmdm.features.core.data.model.location_history.UemSetting
import com.sujanix.cruxmdm.features.core.data.model.request.LocationDataRequest
import com.sujanix.cruxmdm.features.core.data.model.request.Property
import com.sujanix.cruxmdm.features.core.data.model.request.initial_request.DeviceEnrollmentData
import com.sujanix.cruxmdm.features.core.data.model.response.LocationDataResponse
import com.sujanix.cruxmdm.features.core.data.model.response.initial_location_setting.InitialLocationSetting
import com.sujanix.cruxmdm.features.core.data.repository.CruxRepository
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CruxViewModel @Inject constructor(
    private val repository: CruxRepository
): ViewModel() {

    private val _locationHistoryData: MutableStateFlow<List<Property>> = MutableStateFlow(listOf())
    val locationHistoryData = _locationHistoryData.asStateFlow()

    private val _breadcrumbData: MutableStateFlow<List<Property>> = MutableStateFlow(listOf())
    val breadcrumbData = _breadcrumbData.asStateFlow()

    private val _locationHistoryResponse = MutableStateFlow<Resource<LocationDataResponse>>(Resource.Loading)
    val locationHistoryResponse = _locationHistoryResponse.asStateFlow()

    private val _breadcrumbResponse = MutableStateFlow<Resource<LocationDataResponse>>(Resource.Loading)
    val breadcrumbResponse = _breadcrumbResponse.asStateFlow()

    private val _initialLocationSetting = MutableSharedFlow<Resource<InitialLocationSetting>>()
    val initialLocationSetting = _initialLocationSetting.asSharedFlow()

    private val _fenceDataResponse = MutableSharedFlow<Resource<GeoJson>>()
    val fenceDataResponse = _fenceDataResponse.asSharedFlow()

    fun getUserData() = repository.getUserData()

    fun getInitialLocationSetting(enrollmentData: DeviceEnrollmentData){
        viewModelScope.launch {
            _initialLocationSetting.emit(Resource.Loading)
            _initialLocationSetting.emit(repository.getInitialLocationSetting(enrollmentData))
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

    fun setBreadcrumbSetting(enabled: String){
        viewModelScope.launch {
            repository.setBreadcrumbSettings(enabled == "ENABLED")
        }
    }

    fun getBreadcrumbSetting() = repository.getBreadcrumbSettings()

    fun setGeofenceSetting(fencing: Fencing){
        viewModelScope.launch {
            repository.setGeofenceSettings(fencing)
        }
    }

    fun getGeofenceSetting() = repository.getGeofenceSettings()

    fun setUemSetting(uemSetting: UemSetting){
        viewModelScope.launch {
            repository.setUemSettings(uemSetting)
        }
    }

    fun getUemSetting() = repository.getGeofenceSettings()

    fun getLocationHistoryDataByType(type: String = Constant.LOCATION_HISTORY) {
        viewModelScope.launch {
            _locationHistoryData.value = repository.getLocationDataByType(type)
        }
    }

    fun getBreadcrumbDataByType(type: String = Constant.LOCATION_BREADCRUMBS) {
        viewModelScope.launch {
            _breadcrumbData.value = repository.getLocationDataByType(type)
        }
    }

    fun sendLocationDataToServer(type: String = Constant.LOCATION_HISTORY, locationDataRequest: LocationDataRequest) {
        viewModelScope.launch {
            if (type == Constant.LOCATION_HISTORY) {
//                _locationHistoryResponse.value = repository.sendLocationData(locationDataRequest, type)

            } else {
//                _locationHistoryResponse.value = repository.sendLocationData(locationDataRequest, type)
            }
        }
    }

    suspend fun getBoundaryCoordinates(boundaryID: String): GeoJson {
//        viewModelScope.launch {
//            _fenceDataResponse.emit(Resource.Loading)
//            _fenceDataResponse.emit(repository.getFenceData(boundaryID))
//        }
        return repository.getBoundaryCoordinates(boundaryID)
    }

    fun insertFenceData(geoJSON: GeoJSON){
        viewModelScope.launch {
            repository.insertFenceData(geoJSON)
        }
    }

    fun deleteAllFenceData(){
        viewModelScope.launch {
            repository.deleteFenceData()
        }
    }
}