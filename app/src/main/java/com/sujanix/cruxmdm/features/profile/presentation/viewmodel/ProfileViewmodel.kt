package com.sujanix.cruxmdm.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujanix.cruxmdm.features.profile.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewmodel @Inject constructor(
    private val repository: ProfileRepository
): ViewModel() {

    fun logout() {
        viewModelScope.launch {
            repository.clearUserData()
        }
    }
}