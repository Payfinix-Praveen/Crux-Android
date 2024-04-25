package com.sujanix.cruxmdm.presentation.view.activity

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sujanix.cruxmdm.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private var _binding: ActivityAuthBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null
    private var navHostFragment: NavHostFragment? = null

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

//        navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
//        navController = navHostFragment?.findNavController()
//        setupActionBarWithNavController(navController!!)
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
}