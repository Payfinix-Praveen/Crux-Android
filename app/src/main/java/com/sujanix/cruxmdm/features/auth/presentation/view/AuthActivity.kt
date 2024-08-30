package com.sujanix.cruxmdm.features.auth.presentation.view

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.sujanix.cruxmdm.databinding.ActivityAuthBinding
import com.sujanix.cruxmdm.features.auth.presentation.viewmodel.AuthViewModel
import com.sujanix.cruxmdm.features.core.presentation.viewmodel.CruxViewModel
import com.sujanix.cruxmdm.features.enrollment.presentation.view.EnrollActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private val TAG: String = "AuthActivity"
    private var _binding: ActivityAuthBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null
    private var navHostFragment: NavHostFragment? = null
    private val viewModel: AuthViewModel by viewModels()
    private val cruxViewModel: CruxViewModel by viewModels()

    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    private val gsc: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(this, gso)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(allPermissionsGranted()){
            ActivityCompat.requestPermissions(this,
                EnrollActivity.REQUIRED_PERMISSIONS,
                EnrollActivity.REQUEST_CODE_PERMISSIONS
            )
        }

        lifecycleScope.launch {
            cruxViewModel.getUserData().collect { user ->
                if (user != null) {
                    Log.d(TAG, "onCreate: $user")
//                    val intent = Intent(this@AuthActivity, EnrollActivity::class.java)
//                    startActivity(intent)
//                    finish()
                }
            }
        }
    }

    fun allPermissionsGranted() = EnrollActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) or locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        gsc.signOut()
        _binding = null
    }
}