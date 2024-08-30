package com.sujanix.cruxmdm.features.auth.presentation.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.installations.FirebaseInstallationsException
import com.google.firebase.messaging.FirebaseMessaging
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.FragmentLoginBinding
import com.sujanix.cruxmdm.features.auth.data.model.request.LoginData
import com.sujanix.cruxmdm.features.auth.presentation.view.AuthActivity
import com.sujanix.cruxmdm.features.auth.presentation.viewmodel.AuthViewModel
import com.sujanix.cruxmdm.features.core.presentation.view.MainActivity
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.showLoadingDialog
import com.sujanix.cruxmdm.features.enrollment.presentation.view.EnrollActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login) {

    private lateinit var dialog: Dialog
    private var acct: GoogleSignInAccount? = null
    private val TAG: String = "LoginFragment"
    private lateinit var _binding: FragmentLoginBinding
    private val binding get() = _binding
    private val viewModel: AuthViewModel by activityViewModels()

    private var latitude = ""
    private var longitude = ""

    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    private val gsc: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(requireActivity(), gso)
    }
    private var callbackManager: CallbackManager? = null
    private var accessToken: AccessToken? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLoginBinding.bind(view)
        getLocationData()

        gsc.signOut()
        binding.apply {

            etMrloginId.setOnClickListener {
                stMrloginId.isErrorEnabled = false
                etMrloginId.error = null
            }

            tvRegister.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            btnLogin.setOnClickListener {
                when {
                    etMrloginId.text.toString().isEmpty() -> etMrloginId.error = "Username is required"
//                    etPassword.text.toString().isEmpty() -> etMrloginId.error = "Password is required"
                    else -> generateFirebaseTokenAndLogin()
                }
            }
            btnGoogle.setOnClickListener {
                signInWithGoogle()
            }
        }
        observeLoginStatus()
    }

    private fun signInWithGoogle() {
        val signInIntent = gsc.signInIntent
        startActivityForResult(signInIntent, Constant.GOOGLE_SIGN_IN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constant.GOOGLE_SIGN_IN_CODE){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                task.getResult(ApiException::class.java)
                loggedInWithGoogle()
            }catch (e: ApiException){
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                Log.d("google", e.toString())
            }
        }
    }

    private fun loggedInWithGoogle() {
        acct = GoogleSignIn.getLastSignedInAccount(requireContext())
        generateFirebaseTokenAndLogin()
    }

    @SuppressLint("HardwareIds")
    private fun loginUser(token: String) {
        val userId = binding.etMrloginId.text.toString().trim()
        if(userId.isEmpty()) {
            viewModel.loginUser(
                LoginData(
                    acct?.email!!,
                    token
                )
            )
        } else {
            viewModel.loginUser(
                LoginData(
                    userId,
                    token
                )
            )
        }
    }

    private fun generateFirebaseTokenAndLogin() {
        dialog = showLoadingDialog(requireContext())
        FirebaseMessaging.getInstance().token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(
                        MainActivity.TAG,
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    when(task.exception){
                        is IOException -> Log.d(MainActivity.TAG, "onCreateIOException: ${task.exception}")//view.snackbar(getString(R.string.no_internet))
                        is FirebaseInstallationsException -> Log.d(MainActivity.TAG, "onCreateFirebaseInstallationsException: ${task.exception}")//view.snackbar(getString(R.string.no_internet))
                        else -> Log.d(MainActivity.TAG, "onCreateElse: ${task.exception}")//view.snackbar(getString(R.string.something_went_wrong))
                    }
                    dialog.dismiss()
                    return@OnCompleteListener
                }
                Log.d(TAG, "generateFirebaseTokenAndLogin: ${task.result}")
                val token = task.result.toString()
                loginUser(token)
            })

    }

    private fun observeLoginStatus() {
        viewModel.loginResponse.observe(viewLifecycleOwner) { resource ->
            when(resource){
                is Resource.Success -> {
                    if(resource.value.status) {

                        val sharedPreferences = activity?.getSharedPreferences(
                            Constant.PREFERENCE_NAME,
                            Context.MODE_PRIVATE
                        )
                        if (resource.value.device_user != null) {
                            viewModel.saveUserData(
                                resource.value.device_user
                            )
                        }
                        val editor = sharedPreferences?.edit()
                        editor?.putString(Constant.ACCESS_TOKEN, resource.value.access_token)
                        editor?.apply()
//                        startActivity(Intent(requireActivity(), EnrollActivity::class.java))
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                    } else {
                        binding.apply {
                            if(etMrloginId.text.toString().isNotEmpty()) {
                                etMrloginId.error = resource.value.message
                                stMrloginId.isErrorEnabled = true
                                stMrloginId.error = resource.value.message
                            } else {
                                if(acct != null) {
                                    viewModel.setGoogleAccount(acct)
                                    findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                                }
                            }
                        }
                    }
                }
                is Resource.Failure -> {Log.d("FATAL", "Resource.Failure: ")}
                Resource.Loading -> {Log.d("FATAL", "Resource.Loading: ")}
            }
            dialog.dismiss()
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

    override fun onDestroyView() {
        super.onDestroyView()
        acct = null
    }
}