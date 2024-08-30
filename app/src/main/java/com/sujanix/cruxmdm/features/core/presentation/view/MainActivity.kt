package com.sujanix.cruxmdm.features.core.presentation.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.ActivityMainBinding
import com.sujanix.cruxmdm.databinding.DeviceIdBottomsheetBinding
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application
import com.sujanix.cruxmdm.features.app_catalog.presentation.viewmodel.AppCatalogViewmodel
import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData
import com.sujanix.cruxmdm.features.auth.presentation.view.AuthActivity
import com.sujanix.cruxmdm.features.core.data.data_source.local.entity.GeoJSON
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.core.data.model.fencing.Coordinates
import com.sujanix.cruxmdm.features.core.data.model.fencing.Fencing
import com.sujanix.cruxmdm.features.core.data.model.fencing.GeoJson
import com.sujanix.cruxmdm.features.core.data.model.request.initial_request.DeviceEnrollmentData
import com.sujanix.cruxmdm.features.core.presentation.viewmodel.CruxViewModel
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Constant.GEO_FENCE
import com.sujanix.cruxmdm.features.core.utlis.Constant.PREFERENCE_NAME
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.SystemUtils
import com.sujanix.cruxmdm.features.core.utlis.canInstallPackages
import com.sujanix.cruxmdm.features.core.utlis.location.CoordinatesDeserializer
import com.sujanix.cruxmdm.features.core.utlis.location.GeofenceBuilder
import com.sujanix.cruxmdm.features.core.utlis.location.LocationUtils
import com.sujanix.cruxmdm.features.core.utlis.showDialog
import com.sujanix.cruxmdm.features.core.utlis.visible
import com.sujanix.cruxmdm.receiver.CruxReceiver
import com.sujanix.cruxmdm.service.LocationService
import com.sujanix.cruxmdm.worker.LocationManagementWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Collections


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var userData: DeviceUserData? = null
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CruxViewModel by viewModels()
    private val appCatalogViewModel: AppCatalogViewmodel by viewModels()
    private lateinit var navController: NavController
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(Coordinates::class.java, CoordinatesDeserializer())
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            viewModel.getUserData().collect { user ->
                if(user == null){
                    startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                    finish()
                }
            }
        }


        preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        editor = preferences.edit()

//        viewModel.deleteAllFenceData()
        val isInitialSettingsFetched = preferences.getBoolean(Constant.INITIAL_SETTINGS_FETCHED, false)
        if(!isInitialSettingsFetched)
            getUserDataAndFetchInitialSettings()

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

        setSupportActionBar(binding.topAppBar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupWithNavController(binding.bottomNavigation, navController)

        if (!allPermissionsGranted()) {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.appStoreFragment, R.id.contentFragment -> {
                    binding.bottomNavigation.visible(true)
                }

                else -> binding.bottomNavigation.visible(false)
            }
        }
        editor.apply {
            putBoolean(Constant.LOCATION_BREADCRUMBS, true)
            apply()
        }

//        ScheduledAppUpdateWorker.schedule(this)
        LocationManagementWorker.schedule(this)
        observeInitialLocationSetting()
//        observeLocationData()

        registerReceiver(CruxReceiver(), IntentFilter(Constant.BROADCAST_SETTING_ACTION))
    }

    private fun getUserDataAndFetchInitialSettings() {
        lifecycleScope.launch {
            viewModel.getUserData().collect { user ->
                Log.d(TAG, "getUserDataAndFetchInitialSettings: $user")
                if (user != null) {
                    userData = user
                    getInitialLocationSettings()
                    viewModel.getBreadcrumbDataByType()
                    viewModel.getLocationHistoryDataByType()
                    appCatalogViewModel.getApplicationList(
                        OrganizationData(
                            enterprise_id = userData!!.enterprise_id,
                        )
                    )
                }
            }
        }
        editor.apply{
            putBoolean(Constant.INITIAL_SETTINGS_FETCHED, true)
            apply()
        }
    }

    private suspend fun getInitialLocationSettings() {
        if(userData != null) {
            viewModel.getInitialLocationSetting(
                //TODO: change the hard coded values to dynamic
                DeviceEnrollmentData(
                    device_id = viewModel.getDeviceId().first() ?: "30fdcc15e35bd7bd",
                    enrollment_type = "Fully Managed",
                    enterprise_id = userData!!.enterprise_id,
                    org_id = userData!!.org_id,
                    user_id = userData!!.device_user_id
                )
            )
        }
    }

    private fun observeInitialLocationSetting() {
        lifecycleScope.launch {
            viewModel.initialLocationSetting.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        if (resource.value.status) {

                            Log.d(TAG, "InitialLocationSetting: ${resource.value.data}")

                            if (resource.value.data != null) {

                                if (resource.value.data.breadcrumbs != null) {
                                    viewModel.setBreadcrumbSetting(resource.value.data.breadcrumbs)
                                }
                                if (resource.value.data.fencing != null) {
                                    val fenceList = resource.value.data.fencing
                                    manageFences(fenceList)
                                }
                                if (resource.value.data.uemSetting != null) {
                                    viewModel.setUemSetting(resource.value.data.uemSetting)
                                }
                            }
                        } else {
                            Log.d(TAG, "observeInitialLocationSetting: ${resource.value.message}")
                        }
                    }
                    is Resource.Failure -> {
                        Log.d(TAG, "observeInitialLocationSetting: ${resource.errorBody}")
                    }
                }
            }
        }
    }

    private fun manageFences(fenceList: List<Fencing>) {
        for (fenceData in fenceList) {
            val boundaryIdList = fenceData.boundary_id
            if (boundaryIdList.isNotEmpty()){
                for (boundaryId in boundaryIdList) {
                    lifecycleScope.launch {
                        val response = async{ viewModel.getBoundaryCoordinates(boundaryId) }
                        getFenceData(boundaryId, fenceData, response.await())
                    }
                }
            } else {
                viewModel.insertFenceData(
                    GeoJSON(
                        fenceId = fenceData.fenceID.toString(),
                        fenceType = fenceData.fenceType,
                        boundaryId = "N/A",
                        coordinates = Coordinates.Polygon(fenceData.geoJson.coordinates),
                        coordinateType = fenceData.geoJson.coordinatesType
                    )
                )
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ${allPermissionsGranted()}")
        if (!allPermissionsGranted()) {
            requestPermissions (
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            if(LocationUtils.checkLocationPermissions(this@MainActivity, baseContext)) {

                if(!preferences.getBoolean(GEO_FENCE, false)) {
                    GeofenceBuilder (
                        this,
                        12.9590582,
                        77.6471273
                    )
                }
                if(!SystemUtils.isDeviceAdminActive(applicationContext)) {
                    showDialog(
                        context = this,
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
//                showEnterDeviceIdBottomSheet()
                if (deviceId == null){
                    showEnterDeviceIdBottomSheet()
                } else {
                    Log.d("FATAL", "checkForDeviceId: $deviceId")
                }
            }
        }
    }

    fun showEnterDeviceIdBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.DialogAnimation)
        val dialogBinding = DeviceIdBottomsheetBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val window = dialog.window
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.attributes.windowAnimations = R.style.DialogAnimation
        window.setGravity(Gravity.CENTER)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.apply {
            btnSave.setOnClickListener {
                viewModel.setDeviceId("3ef9b81b555bdbed")
//                viewModel.setDeviceId(etDeviceId.text.toString())
                dialog.dismiss()
            }
        }
        if(!this.isFinishing)
            dialog.show()
    }

    private fun startLocationService() {
        Intent(this, LocationService::class.java).apply {
            ContextCompat.startForegroundService(this@MainActivity, this)
        }
    }

    fun startLocationServiceWithLiveTracking() {
        stopLocationService()
        Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START_LIVE_TRACKING
            startService(this)
        }
    }

    fun stopLocationService() {
        stopService(Intent(this, LocationService::class.java))
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
            }, cancelable = false
        )
    }

    private fun getFenceData(boundaryId: String, fenceData: Fencing, response: GeoJson){
        lifecycleScope.launch {
            val coordinatesType = response.coordinatesType
            viewModel.insertFenceData(
                GeoJSON(
                    fenceId = fenceData.fenceID.toString(),
                    fenceType = fenceData.fenceType,
                    boundaryId = boundaryId,
                    coordinates = Coordinates.Polygon(response.coordinates),
                    coordinateType = coordinatesType
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        stopService(Intent(this, LocationService::class.java))
        _binding = null
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
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