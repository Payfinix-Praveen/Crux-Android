package com.sujanix.cruxmdm.features.content_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file.SharedJSONFileDto
import com.sujanix.cruxmdm.features.content_management.data.repository.ContentManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentManagementViewmodel @Inject constructor(
    private val repository: ContentManagementRepository
): ViewModel() {

    private val _sharedJsonFile = MutableSharedFlow<Resource<SharedJSONFileDto>>()
    val sharedJsonFile = _sharedJsonFile.asSharedFlow()

    fun getSharedJsonFile(organizationData: OrganizationData) {
        viewModelScope.launch {
            _sharedJsonFile.emit(Resource.Loading)
            _sharedJsonFile.emit(repository.getSharedFileJson(organizationData))
        }
    }

    fun getDeviceId() = repository.getDeviceId()

    fun getUserData() = repository.getUserData()

    suspend fun getAllDownloadedFiles() = repository.getAllFile()
}