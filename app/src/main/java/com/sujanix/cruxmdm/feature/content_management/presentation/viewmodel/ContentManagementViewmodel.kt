package com.sujanix.cruxmdm.feature.content_management.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.sujanix.cruxmdm.feature.content_management.data.repository.ContentManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContentManagementViewmodel @Inject constructor(
    private val repository: ContentManagementRepository
): ViewModel() {

}