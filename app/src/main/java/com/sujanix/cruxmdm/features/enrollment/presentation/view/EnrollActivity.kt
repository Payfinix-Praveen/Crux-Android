package com.sujanix.cruxmdm.features.enrollment.presentation.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.ActivityEnrollBinding
import com.sujanix.cruxmdm.databinding.PermissionDialogBinding
import com.sujanix.cruxmdm.databinding.ValidateInventoryDialogBinding
import com.sujanix.cruxmdm.features.auth.data.model.response.DeviceUserData
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.core.data.model.SliderData
import com.sujanix.cruxmdm.features.core.presentation.adapter.SliderAdapter
import com.sujanix.cruxmdm.features.core.presentation.view.MainActivity
import com.sujanix.cruxmdm.features.core.presentation.viewmodel.CruxViewModel
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.SystemUtils
import com.sujanix.cruxmdm.features.core.utlis.changeColor
import com.sujanix.cruxmdm.features.core.utlis.isDeviceOwner
import com.sujanix.cruxmdm.features.enrollment.data.model.request.EnrollmentData
import com.sujanix.cruxmdm.features.enrollment.data.model.response.GenerateQrCodeResponse
import com.sujanix.cruxmdm.features.enrollment.data.model.response.GroupData
import com.sujanix.cruxmdm.features.enrollment.presentation.RegisterDeviceUserActivity
import com.sujanix.cruxmdm.features.enrollment.presentation.viewmodel.EnrollmentViewmodel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class EnrollActivity : AppCompatActivity() {

    private lateinit var dialog: Dialog
    private val TAG: String = "EnrollActivity"
    private var _binding: ActivityEnrollBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EnrollmentViewmodel by viewModels()
    private val cruxViewModel: CruxViewModel by viewModels()
    private var groupIdList = emptyList<GroupData>()
    private var validateInventory = false
    private var groupId: String = ""

    private var userData: DeviceUserData? = null

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

            if (viewModel.getEnrollmentDevice().last() == false) {
                Log.d(TAG, "onCreate: ${viewModel.getEnrollmentDevice().last()}")
//                startActivity(
//                    Intent(
//                        this@EnrollActivity,
//                        MainActivity::class.java
//                    )
//                )
//                finish()
            }
        }

        lifecycleScope.launch {

            cruxViewModel.getUserData().collect { user ->
                Log.d(TAG, "onCreate: $user")
                if (user != null) {
                    userData = user
                    viewModel.getEnterpriseGroupList(
                        OrganizationData(
                            enterprise_id = user.enterprise_id
                        )
                    )
                }
            }
        }
        _binding = ActivityEnrollBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(!SystemUtils.isDeviceAdminActive(applicationContext)) {
            val message = getString(
                R.string.dialog_administrator_mode_message,
                getString(R.string.app_name)
            )
            com.sujanix.cruxmdm.features.core.utlis.showDialog(
                context = this,
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
            btnGenerateQrCode.setOnClickListener {
                lifecycleScope.launch {
                    showValidateInventoryDialog() { validate, clientId, dateTime ->
                        if (userData != null) {
                            Log.d(TAG, "onCreate: $userData")
                            generateQrCode(
                                EnrollmentData(
                                    additionalData = userData!!.copy(
                                        groupId = groupId,
                                        validate_inventory = validate
                                    ),
                                    duration = dateTime ?: "2024-05-16T12:48:35.450Z",
                                    enterpriseName = userData!!.enterprise_id,
                                    oneTimeOnly = true,
                                    policyName = "newPolicy",
                                    user_name = userData!!.fullname,
                                    user_id = userData!!.device_user_id,
                                    timeDifferenceInSeconds = "1000"
                                )
                            )
                        }
                    }
                }
            }

            btnEnroll.setOnClickListener {
                IntentIntegrator(this@EnrollActivity).apply {
                    setOrientationLocked(false)
                    initiateScan()
                }

//                openApplication(
//                    this@EnrollActivity,
//                    "com.google.android.apps.work.clouddpc",
//                    Constant.ANDROID_DEVICE_POLICY
//                )
            }

            btnAddNewDeviceUser.setOnClickListener {
                startActivity(Intent(this@EnrollActivity, RegisterDeviceUserActivity::class.java))
            }
        }
        observeEnterpriseGroupList()
        observeGenerateQrCode()

//        Toast.makeText(this, Build.getSerial(), Toast.LENGTH_LONG).show()
    }

    private fun generateQrCode(enrollmentData: EnrollmentData) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val requestBody = Gson().toJson(enrollmentData).toString().toRequestBody("application/json".toMediaType())
                val request: Request = Request.Builder()
                    .url("https://02fhoaa58j.execute-api.us-east-1.amazonaws.com/cruxdev/enrolled/s3_qr_code")
//                    .url("http://192.168.0.187:5000/enrolled/s3_qr_code")
                    .post(requestBody)
                    .build()
                Log.d(TAG, "enrollmentData: ${Gson().toJson(enrollmentData)}")

                val client = OkHttpClient()
                val response: Response = client.newCall(request).execute()

                val responseBody = response.body!!.string()
                Log.d(TAG, "response.body: $responseBody")

                if (response.isSuccessful) {
                    val result = Gson().fromJson(responseBody, GenerateQrCodeResponse::class.java)
                    Log.d(TAG, "generateQrCode: $result")
                    dialog.dismiss()
                    Handler(Looper.getMainLooper()).post {
                        showQrCodeDialog(result.qr_url)
                    }
                } else {
                    Log.d(TAG, "generateQrCode: ${response.message}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "generateQrCodeCatch: ${e.message}")
        }
    }

    private fun observeEnterpriseGroupList(){
        viewModel.enterpriseClientIdResponse.observe(this){ resource ->
            when(resource) {
                is Resource.Loading -> {

                }
                is Resource.Success -> {
                    groupIdList = resource.value.data
                    Log.d(TAG, "observeEnterpriseGroupList: $groupIdList")
                }
                is Resource.Failure -> {

                }
            }
        }
    }

    private fun showValidateInventoryDialog(
        onClick: (validate: Boolean, clientId: String?, dateTime: String?) -> Unit
    ) {
        dialog = Dialog(this)
        var dateTime  = ""
        val dialogBinding = ValidateInventoryDialogBinding.inflate(layoutInflater)
        dialogBinding.apply {
            swValidate.setOnCheckedChangeListener { _, isChecked ->
                swValidate.changeColor(this@EnrollActivity)
                validateInventory = isChecked
            }

            groupDropDown.apply {
                val adapter = ArrayAdapter(this@EnrollActivity,
                    R.layout.list_item,
                    groupIdList.map { it.name })
                setAdapter(adapter)
                setOnItemClickListener { _, _, position, _ ->
                    groupId = groupIdList[position].group_id
                }
            }
            etQRValidity.setOnClickListener {
                tilQRValidity.error = null
                tilQRValidity.isErrorEnabled = false
                showDatePicker(this@EnrollActivity) {date ->
                    showTimePicker(this@EnrollActivity) { time ->
                        dateTime = "${date}T$time"
                        etQRValidity.setText(dateTime)
                    }
                }
            }

            btnProceed.setOnClickListener {
                Log.d(TAG, "showValidateInventoryDialog: $dateTime")
                when {
                    etQRValidity.text!!.isEmpty() -> {
                        tilQRValidity.isErrorEnabled = true
                        tilQRValidity.error = "Please select a date"
                    }
                    else -> {
                        dialog.dismiss()
                        onClick(validateInventory, groupId, dateTime)
                    }
                }
//                onClick(validateInventory, groupId, dateTime)
            }
            btnHistory.setOnClickListener {
                dialog.dismiss()
//                showHistoryDialog()
            }
        }
        val window = dialog.window
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.attributes.windowAnimations = R.style.DialogAnimation
        window.setGravity(Gravity.CENTER)
        dialog.setContentView(dialogBinding.root)
        dialog.show()

    }

    private fun showDatePicker(context: Context, onSelect: (date: String) -> Unit) {

        Log.d(TAG, "showDatePicker: called")
        val c: Calendar = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                run {
                    onSelect( year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString())
                }
            },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }

    private fun showTimePicker(context: Context, onSelect: (time: String) -> Unit) {
        val c = Calendar.getInstance()
        val mHour = c[Calendar.HOUR_OF_DAY]
        val mMinute = c[Calendar.MINUTE]
        val mSecond = c[Calendar.SECOND]


        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            this,
            { view, hourOfDay, minute -> onSelect("$hourOfDay:$minute:${mSecond}.450Z") },
            mHour,
            mMinute,
            false
        )
        timePickerDialog.show()
    }

    private fun observeGenerateQrCode() {
        viewModel.generateQrCodeResponse.observe(this) { resource ->
            when(resource) {
                is Resource.Loading -> {

                }
                is Resource.Success -> {

                    Log.d(TAG, "observeGenerateQrCode: ${resource.value}")
                    dialog.dismiss()
                    showQrCodeDialog(resource.value.qr_url)
                }
                is Resource.Failure -> {

                }
            }
        }
    }

    private fun showQrCodeDialog(qrCode: String) {
        val dialog = QrCodeDialog(
                this,
                qrCode,
                TimeUnit.SECONDS.toMillis(1000)
            )
        dialog.apply {
            title = "Scan QR Code"
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setCancelable(false)
            show()
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

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
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