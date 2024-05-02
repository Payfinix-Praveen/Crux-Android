package com.sujanix.cruxmdm.feature.enrollment.presentation.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.zxing.integration.android.IntentIntegrator
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.feature.common.presentation.adapter.SliderAdapter
import com.sujanix.cruxmdm.feature.common.data.model.SliderData
import com.sujanix.cruxmdm.databinding.ActivityEnrollBinding
import com.sujanix.cruxmdm.databinding.PermissionDialogBinding
import com.sujanix.cruxmdm.feature.common.presentation.view.MainActivity
import com.sujanix.cruxmdm.feature.enrollment.presentation.viewmodel.EnrollmentViewmodel
import com.sujanix.cruxmdm.feature.common.utlis.SystemUtils
import com.sujanix.cruxmdm.feature.common.utlis.isDeviceOwner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class EnrollActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnrollBinding
    private val viewModel: EnrollmentViewmodel by viewModels()

    @SuppressLint("StringFormatInvalid")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            if (intent.getBooleanExtra(
                    "com.google.android.apps.work.clouddpc.EXTRA_LAUNCHED_AS_SETUP_ACTION",
                    false)
            ) {
                //call validation api
                Toast.makeText(this, "Enroll hone wala hai...", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                viewModel.setDeviceEnrolled()
                finish()
            }
        }

        lifecycleScope.launch {

            if (viewModel.getEnrollmentDevice().last() == true) startActivity(
                Intent(
                    this@EnrollActivity,
                    MainActivity::class.java
                )
            )
        }

        binding = ActivityEnrollBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(!SystemUtils.isDeviceAdminActive(applicationContext)) {
            val message = getString(
                R.string.dialog_administrator_mode_message,
                getString(R.string.app_name)
            )
            com.sujanix.cruxmdm.feature.common.utlis.showDialog(
                activity = this,
                title = "Action Required",
                message = message,
                positiveBtnText = getString(R.string.admin_allow_settings),
                negativeBtnText = getString(R.string.admin_exit),
                positiveBtnListener = {
                    SystemUtils.setAdminMode(this, applicationContext)
                },
                negativeBtnListener = { finish() }
            )
        } else {
            Log.d("FATALENROLL", "onCreate\n serial No.: ${applicationContext.packageName}")
        }

        requestPermissions(
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )

        val sliderData = listOf(
            SliderData(R.drawable.img1, getString(R.string.lorem_ipsum)),
            SliderData(R.drawable.img2, getString(R.string.lorem_ipsum)),
            SliderData(R.drawable.img1, getString(R.string.lorem_ipsum))
        )

        binding.apply {

            vpSlider.adapter = SliderAdapter(sliderData)
            autoScrollViewPager(vpSlider)
            btnOtherOption.setOnClickListener {
                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                Log.d("FATAL", "androidID: $androidId")

                Toast.makeText(this@EnrollActivity, "Android ID: $androidId", Toast.LENGTH_SHORT)
                    .show()
            }

            btnEnroll.setOnClickListener {
                IntentIntegrator(this@EnrollActivity).apply {
                    setOrientationLocked(false)
                    initiateScan()
                }

//                openADPToEnrollDevice("com.google.android.apps.work.clouddpc")
            }
        }
    }

    private fun autoScrollViewPager(sliderPager: ViewPager) {
        val handler = Handler()

        var currentPage = 0
        val update = Runnable {
            if (currentPage == 3) {
                currentPage = 0
            }
            sliderPager.setCurrentItem(currentPage++, true)
        }

        Timer().schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, 100, 1500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        try {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if(result.contents != null) {
//                extractDataFromQr(result.contents)
                Log.d("FATALENROLL", "onActivityResult: ${result.contents}")
                val qrJson = JSONObject(result.contents)
                val extra = qrJson.getJSONObject(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION)

                Log.d("FATALENROLL", "onActivityResult: $extra")
            } else {
                Log.d("FATALENROLL", "onActivityResult: Failed to parse QR code!")
                super.onActivityResult(requestCode, resultCode, data)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.d("FATALENROLL", "onActivityResultERROR: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("FATAL", "onRequestPermissionsResult: $requestCode")
        if (requestCode == MainActivity.REQUEST_CODE_PERMISSIONS) {

            if (isDeviceOwner(this)) {
                // Even in device owner mode, if "Ask for location" is requested by the admin,
                // let's ask permissions (so do nothing here, fall through)
//                if (settingsHelper.getConfig() == null || !ServerConfig.APP_PERMISSIONS_ASK_ALL.equals(
//                        settingsHelper.getConfig().getAppPermissions()
//                    ) &&
//                    !ServerConfig.APP_PERMISSIONS_ASK_LOCATION.equals(
//                        settingsHelper.getConfig().getAppPermissions()
//                    )
//                ) {
                    // This may be called on Android 10, not sure why; just continue the flow
//                    Log.i(
//                        Const.LOG_TAG,
//                        "Called onRequestPermissionsResult: permissions=" + permissions.contentToString() +
//                                ", grantResults=" + grantResults.contentToString()
//                    )
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                    return
//                }
            }

            var locationDisabled = false
            for (n in permissions.indices) {
                if (permissions[n] == Manifest.permission.ACCESS_FINE_LOCATION ||
                    permissions[n] == Manifest.permission.ACCESS_COARSE_LOCATION) {
                    if (grantResults[n] != PackageManager.PERMISSION_GRANTED) {
                        // The user didn't allow to determine location, this is not critical, just ignore it
//                        preferences.edit()
//                            .putInt(Const.PREFERENCES_DISABLE_LOCATION, Const.PREFERENCES_ON)
//                            .commit()
                        locationDisabled = true
                    }
                }
            }
            var requestPermissions = false
            for (n in permissions.indices) {
                if (grantResults[n] != PackageManager.PERMISSION_GRANTED) {
                    if (permissions[n] == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
                        (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || locationDisabled)
                    ) {
                        // Background location is not available on Android 9 and below
                        // Also we don't need to grant background location permission if we don't grant location at all

                        continue
                    }
                    if (permissions[n] == Manifest.permission.ACCESS_FINE_LOCATION &&
                        locationDisabled
                    ) {
                        // Skip fine location permission if user intentionally disabled it
                        continue
                    }

                    // Let user know that he need to grant permissions
                    requestPermissions = true
                }
            }
            if (requestPermissions) {
                createAndShowPermissionsDialog()
            }
        }
    }

    private fun createAndShowPermissionsDialog() {
        val permissionDialogBinding = PermissionDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window = dialog.window
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(permissionDialogBinding.root)
        dialog.setCancelable(false)
        dialog.show()

        permissionDialogBinding.apply {
            btnContinue.setOnClickListener {
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                )
                dialog.dismiss()
            }
        }
    }

    fun openADPToEnrollDevice(packageName: String) {

        if (appInstalledOrNot(packageName)) {
            val LaunchIntent = packageManager
                .getLaunchIntentForPackage("com.google.android.apps.work.clouddpc")
            startActivity(LaunchIntent)

        } else {
            // App is not installed, redirect to Play Store
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = application.packageManager
        return try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // Location permissions request on Android 10 and above is rather tricky (shame on Google for their stupid logic!!!)
    // So it's implemented in a separate method


    companion object {

        private val File.size get() = if (!exists()) 0.0 else length().toDouble()
        private val File.sizeInKb get() = size / 1024
        private val File.sizeInMb get() = sizeInKb / 1024
        const val REQUEST_CODE_PERMISSIONS = 100
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
        )
    }
}