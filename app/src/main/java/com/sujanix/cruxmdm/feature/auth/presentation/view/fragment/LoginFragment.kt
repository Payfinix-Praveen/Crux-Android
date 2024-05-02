package com.sujanix.cruxmdm.feature.auth.presentation.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.LocationServices
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.feature.auth.data.model.request.LoginData
import com.sujanix.cruxmdm.databinding.FragmentLoginBinding
import com.sujanix.cruxmdm.feature.auth.presentation.view.AuthActivity
import com.sujanix.cruxmdm.feature.auth.presentation.viewmodel.AuthViewmodel
import com.sujanix.cruxmdm.feature.enrollment.presentation.view.EnrollActivity
import com.sujanix.cruxmdm.feature.common.utlis.Resource
import com.sujanix.cruxmdm.feature.device_detail.utlis.getDeviceDetails
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login) {

    private lateinit var _binding: FragmentLoginBinding
    private val binding get() = _binding
    private val viewModel: AuthViewmodel by viewModels()

    private var latitude = ""
    private var longitude = ""

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLoginBinding.bind(view)
        getLocationData()

        binding.apply {
            btnLogin.setOnClickListener {
                when {
                    etMrloginId.text.toString().isEmpty() -> etMrloginId.error = "Username is required"
                    etPassword.text.toString().isEmpty() -> etMrloginId.error = "Password is required"
                    else -> loginUser()
                }
            }
        }

        observeLoginStatus()
    }

    @SuppressLint("HardwareIds")
    private fun loginUser() {
        val userId = binding.etMrloginId.text.toString()
        val password = binding.etPassword.text.toString()
        val androidId = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)

        viewModel.loginUser(
            LoginData(
                userId,
                password,
                latitude,
                longitude,
                getDeviceDetails(requireContext(), requireActivity()).toString(),
                androidId
            )
        )
    }

    private fun observeLoginStatus() {
        viewModel.loginResponse.observe(viewLifecycleOwner) { response ->
            when(response){
                is Resource.Success -> {
                    Log.d("FATAL", "Success: Login s")

                    startActivity(Intent(requireActivity(), EnrollActivity::class.java))
                }
                is Resource.Failure -> {Log.d("FATAL", "Resource.Failure: ")}
                is Resource.Loading -> {Log.d("FATAL", "Resource.Loading: ")}
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getLocationData(){
        if ((activity as AuthActivity).isLocationEnabled()){
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    EnrollActivity.REQUEST_CODE_PERMISSIONS
                )
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                }
            }
        } else {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }
}