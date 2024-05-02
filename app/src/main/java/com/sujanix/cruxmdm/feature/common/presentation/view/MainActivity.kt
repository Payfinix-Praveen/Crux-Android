package com.sujanix.cruxmdm.feature.common.presentation.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.feature.app_catalog.data.model.Application
import com.sujanix.cruxmdm.feature.common.data.model.OrganizationData
import com.sujanix.cruxmdm.databinding.ActivityMainBinding
import com.sujanix.cruxmdm.databinding.DeviceIdBottomsheetBinding
import com.sujanix.cruxmdm.feature.app_catalog.presentation.viewmodel.AppCatalogViewmodel
import com.sujanix.cruxmdm.feature.common.presentation.viewmodel.CruxViewModel
import com.sujanix.cruxmdm.service.LocationService
import com.sujanix.cruxmdm.feature.common.utlis.Constant.GEO_FENCE
import com.sujanix.cruxmdm.feature.common.utlis.Constant.PREFERENCE_NAME
import com.sujanix.cruxmdm.feature.common.utlis.SystemUtils
import com.sujanix.cruxmdm.feature.common.utlis.canInstallPackages
import com.sujanix.cruxmdm.feature.common.utlis.location.GeofenceBuilder
import com.sujanix.cruxmdm.feature.common.utlis.location.LocationUtils
import com.sujanix.cruxmdm.feature.common.utlis.showDialog
import com.sujanix.cruxmdm.feature.common.utlis.visible
import com.sujanix.cruxmdm.worker.ScheduledAppUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var applist: List<Application>
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CruxViewModel by viewModels()
    private val appCatalogViewModel: AppCatalogViewmodel by viewModels()
    private lateinit var navController: NavController
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.appStoreFragment,
                R.id.contentFragment,
            )
        )

        preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        setSupportActionBar(binding.topAppBar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupWithNavController(binding.bottomNavigation, navController)

        if (!allPermissionsGranted()) {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

//        if (!isDeviceOwner(applicationContext)) {
//            if (!SystemUtils.becomeDeviceOwnerByCommand(applicationContext)) {
//                SystemUtils.becomeDeviceOwnerByXmlFile(applicationContext)
//            }
//        } else Log.d("FATAL", "onCreate: app is not a device admin app")

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.appStoreFragment, R.id.contentFragment -> {
                    binding.bottomNavigation.visible(true)
                }

                else -> binding.bottomNavigation.visible(false)
            }
        }

//        applist = getInstalledAppList()

        appCatalogViewModel.getApplicationList(
            OrganizationData(
                enterprise_id = "enterprises/LC01d3znq7"
            )
        )
        ScheduledAppUpdateWorker.schedule(this)
    }

    @SuppressLint("StringFormatInvalid")
    override fun onResume() {
        super.onResume()
        if (!allPermissionsGranted()) {
            requestPermissions (
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            if(LocationUtils.checkLocationPermissions(this@MainActivity, baseContext)) {

                val fence = listOf(
                    LatLng(77.64347588986351,
                        12.959851922985962),
                    LatLng(77.64611596845509,
                        12.959674486488922),
                    LatLng(77.64561526389474,
                        12.958033192890625),
                    LatLng(77.64247448074298,
                        12.958402854554734),
                    LatLng(77.64218619629958,
                        12.960251154644183),
                    LatLng(77.64347588986351,
                        12.959851922985962)
                )
//                val customGeofence = CustomGeofence(this, fence)
//                customGeofence.startTracking()

                if(!preferences.getBoolean(GEO_FENCE, false)) {
                    GeofenceBuilder (
                        this,
                        12.9590582,
                        77.6471273
                    )
                }
                if(!SystemUtils.isDeviceAdminActive(applicationContext)) {
                    showDialog(
                        activity = this,
                        title = "Action Required",
                        message = getString(R.string.dialog_administrator_mode_message, getString(R.string.app_name)),
                        positiveBtnText = getString(R.string.admin_allow_settings),
                        negativeBtnText = getString(R.string.admin_exit),
                        positiveBtnListener = {
                            SystemUtils.setAdminMode(this, applicationContext)
                        },
                        negativeBtnListener = { finish() }
                    )
                } else {

                    if(checkForUnknownResource()){
                        checkForDeviceId()
                    }
                }
            }
            startLocationService()
        }
    }

    private fun checkForDeviceId() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getDeviceId().collect { deviceId ->
                if (deviceId == null){
                    showEnterDeviceIdBottomSheet()
                } else {
                    Log.d("FATAL", "checkForDeviceId: $deviceId")
                }
            }
        }
    }

    private fun showEnterDeviceIdBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.DialogAnimation)
        val dialogBinding = DeviceIdBottomsheetBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val window = dialog.window
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.attributes.windowAnimations = R.style.DialogAnimation
        window.setGravity(Gravity.BOTTOM)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.apply {
            btnSave.setOnClickListener {
                viewModel.setDeviceId(etDeviceId.text.toString())
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun startLocationService() {
        Intent(this, LocationService::class.java).apply {
            startService(this)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledAppList(): List<Application> {
        val appList: MutableList<Application> = ArrayList()
        val pm: PackageManager = packageManager
        val allApps = pm.getInstalledApplications(ApplicationInfo.FLAG_SYSTEM)
        Collections.sort(allApps, ApplicationInfo.DisplayNameComparator(pm))
        for (info in allApps) {
            if (pm.getLaunchIntentForPackage(info.packageName) != null) {
                appList.add(
                    Application (
                        type = Application.TYPE_APP,
                        pkg = info.packageName,
//                        icon = pm.getApplicationIcon(info.packageName),
                        name = pm.getApplicationLabel(info).toString()
                    )
                )
            }
        }
        return appList
    }

    private fun checkForUnknownResource(): Boolean {
        if(!canInstallPackages(this)){
            showUnknownResourcesDialog()
            return false
        } else return true
    }

    private fun showUnknownResourcesDialog(){

        showDialog(
            this@MainActivity,
            "Action Required",
            getString(R.string.dialog_unknown_sources_title),
            getString(R.string.dialog_unknown_sources_continue),
            getString(R.string.main_activity_cancel),
            positiveBtnListener = {
                // In Android Oreo and above, permission to install packages are set per each app
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse(
                            "package:$packageName"
                        )
                    )
                )
            },
            cancelable = false
            )
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, LocationService::class.java))
    }

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 100
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }
}