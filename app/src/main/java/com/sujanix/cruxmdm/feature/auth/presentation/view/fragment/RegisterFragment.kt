package com.sujanix.cruxmdm.feature.auth.presentation.view.fragment

import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.FragmentRegisterBinding
import com.sujanix.cruxmdm.feature.auth.presentation.viewmodel.AuthViewmodel
import com.sujanix.cruxmdm.feature.common.utlis.hideKeyboard
import com.sujanix.cruxmdm.feature.common.utlis.setEnable
import com.sujanix.cruxmdm.feature.common.utlis.visible

class RegisterFragment: Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding
    private val viewModel: AuthViewmodel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRegisterBinding.bind(view)
//        getLocationData()
//        getDeviceDetails()

        binding?.apply {

            btnSendOtp.setOnClickListener {
                registerUser()
            }
        }
    }

    private fun registerUser() {
        binding?.apply {
            val name = etMrId.text.toString().trim()
            val email = etMrName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val androidId = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)

            when {
                name.isEmpty() -> etMrId.error = "Enter name"
                email.isEmpty() -> etMrName.error = "Enter Email"
                phone.isEmpty() -> etPhone.error = "Enter Phone No."
                else -> {
                    linearLayout.setEnable(false)
                    btnSendOtp.text = ""
                    progressBar3.visible(true)
                    view?.hideKeyboard()
                }
            }
            //api call
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}