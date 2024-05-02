package com.sujanix.cruxmdm.feature.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.sujanix.cruxmdm.feature.profile.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewmodel @Inject constructor(
    private val repository: ProfileRepository
): ViewModel() {

}