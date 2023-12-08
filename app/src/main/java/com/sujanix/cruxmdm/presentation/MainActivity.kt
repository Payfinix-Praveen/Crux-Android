package com.sujanix.cruxmdm.presentation

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var devicePolicyManager: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
//            if(TextUtils.isEmpty(etName.editableText) || TextUtils.isEmpty(etEmail.editableText) || TextUtils.isEmpty(etImei.editableText) ||
//                TextUtils.isEmpty(etPhone.editableText) || TextUtils.isEmpty(etSerialNo.editableText)
//            ){
//                Toast.makeText(this@MainActivity, "Fill all the fields to submit", Toast.LENGTH_SHORT).show()
//            }

            devicePolicyManager = applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val workSerialNo = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                devicePolicyManager.enrollmentSpecificId
            } else {
//                Build.getSerial()
            }
        }
    }
}