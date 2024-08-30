package com.sujanix.cruxmdm.features.enrollment.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.ActivityRegisterDeviceUserBinding
import com.sujanix.cruxmdm.features.auth.data.model.request.RegisterData
import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData
import com.sujanix.cruxmdm.features.auth.presentation.viewmodel.AuthViewModel
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.hideKeyboard
import com.sujanix.cruxmdm.features.core.utlis.setEnable
import com.sujanix.cruxmdm.features.core.utlis.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterDeviceUserActivity : AppCompatActivity() {

    private val TAG = "RegisterDeviceUserActivity"

    private var _binding: ActivityRegisterDeviceUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var userData: DeviceUserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityRegisterDeviceUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launchWhenStarted {
            viewModel.getUserData().collect {
                if (it != null) {
                    userData = it
                }
            }
        }

        binding.apply {
            btnBack.setOnClickListener {
                onBackPressed()
            }
            tvAppVersion.text = getString(R.string.app_version, Constant.APP_VERSION)
            btnRegister.setOnClickListener {
                registerUser()
            }
        }

        observeRegisterUser()
    }

    private fun registerUser() {
        binding.apply {
            val name = etName.text.toString().trim()
            val email = etEmailId.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val enterpriseId = userData.enterprise_id
            val orgId = userData.org_id
            val aadhar = etAadhar.text.toString().trim()

            when {
                name.isEmpty() -> etName.error = "Enter name"
                email.isEmpty() -> etEmailId.error = "Enter Email"
                phone.isEmpty() -> etPhone.error = "Enter Phone No."
                aadhar.isEmpty() -> etAadhar.error = "Enter Aadhar No."

                else -> {
                    linearLayout.setEnable(false)
                    btnRegister.text = ""
                    progressBar3.visible(true)
                    root.hideKeyboard()
                }
            }
            viewModel.registerUser(
                RegisterData(
                    name = name,
                    email = email,
                    mobile_number = phone,
                    org_id = orgId,
                    enterprise_id = enterpriseId,
                    aadhar_number = aadhar
                )
            )
        }
    }

    private fun observeRegisterUser() {

        viewModel.registerResponse.observe(this) { resource ->

            when(resource) {
                is Resource.Loading -> {
                    Log.d(TAG, "ObserveRegisterUser: Loading")
                }
                is Resource.Success -> {
                    Log.d(TAG, "ObserveRegisterUser: Success")
                    binding.apply {
                        progressBar3.visible(false)
                    }
                    if(resource.value.Status) {

                        val sharedPreferences = getSharedPreferences(
                            Constant.PREFERENCE_NAME,
                            Context.MODE_PRIVATE
                        )
                        val editor = sharedPreferences?.edit()
                        editor?.putString(Constant.DEVICE_USER_ID, resource.value.device_user_id)
                        editor?.apply()

                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            resource.value.Message,
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.apply {
                            progressBar3.visible(false)
                            linearLayout.setEnable(true)
                            btnRegister.text = getString(R.string.register)
                        }
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(
                        this,
                        "Registration Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.apply {
                        progressBar3.visible(false)
                        linearLayout.setEnable(true)
                        btnRegister.text = getString(R.string.register)
                    }
                }
            }
        }
    }
}