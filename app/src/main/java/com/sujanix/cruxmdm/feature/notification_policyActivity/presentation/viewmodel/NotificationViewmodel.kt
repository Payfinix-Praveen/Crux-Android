package com.sujanix.cruxmdm.feature.notification_policyActivity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.sujanix.cruxmdm.feature.notification_policyActivity.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationViewmodel @Inject constructor(
    private val repository: NotificationRepository
): ViewModel() {

}