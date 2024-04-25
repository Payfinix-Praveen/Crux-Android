package com.sujanix.cruxmdm.presentation.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.FragmentRegisterBinding
import com.sujanix.cruxmdm.presentation.view.activity.AuthActivity
import com.sujanix.cruxmdm.util.hideKeyboard
import com.sujanix.cruxmdm.util.setEnable
import com.sujanix.cruxmdm.util.visible
import org.json.JSONObject

class RegisterFragment: Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding

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