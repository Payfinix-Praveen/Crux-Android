package com.sujanix.cruxmdm.feature.common.utlis

import android.annotation.TargetApi
import android.app.Activity
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.sujanix.cruxmdm.feature.common.utlis.Constant.REQUEST_CODE_ENABLE_ADMIN
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * These utils are used only in the 'system' flavor
 * when Headwind MDM is installed as a system app and
 * signed by OS keys
 */
object SystemUtils {
    /**
     * This command requires system privileges: MANAGE_DEVICE_ADMINS, MANAGE_PROFILE_AND_DEVICE_OWNERS
     * The MANAGE_PROFILE_AND_DEVICE_OWNERS is only provided to system apps!
     *
     * Also, there's the following restriction in the DevicePolicyManagerService.java:
     * The device owner can only be set before the setup phase of the primary user has completed,
     * except for adb command if no accounts or additional users are present on the device.
     *
     * So it looks like Headwind MDM can never declare itself as a device owner,
     * except when it is running from inside a setup wizard (or declares itself as a setup wizard!)
     * To become a setup wizard, Headwind MDM should be preinstalled in the system, and
     * handle the following intent: android.intent.action.DEVICE_INITIALIZATION_WIZARD
     *
     * @param context
     * @return
     */

    fun setAdminMode(activity: Activity, context: Context){

        val componentName = LegacyUtils.getAdminComponentName(context)

        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable app administration for profile control")
        activity.startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
    }

    fun getDpm(context: Context): DevicePolicyManager {
        return context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    fun isDeviceAdminActive(context: Context): Boolean{
        return try {
            val componentName = LegacyUtils.getAdminComponentName(context)
            val devicePolicyManager = getDpm(context)
            devicePolicyManager.isAdminActive(componentName)
        } catch (e: Exception){
            e.printStackTrace()
            Log.d("FATAL", "isDeviceAdminActive: ${e.message}")
            false
        }
    }

    fun becomeDeviceOwnerByCommand(context: Context): Boolean {
        val command = "dpm set-device-owner ${context.packageName}/.CruxDeviceAdminReceiver"
        val result = executeShellCommand(command, false)
//        RemoteLogger.log(context, Const.LOG_INFO, "DPM command output: $result")
        Log.d("FATAL", "becomeDeviceOwnerByCommand: $command")
        return result.startsWith("Active admin component set")
    }

    fun executeShellCommand(command: String, useShell: Boolean): String {
        val output = StringBuffer()
        val p: Process
        try {
            p = if (useShell) {
                val cmdArray =
                    arrayOf("sh", "-c", command)
                Runtime.getRuntime().exec(cmdArray)
            } else {
                Runtime.getRuntime().exec(command)
            }
            p.waitFor()
            val reader =
                BufferedReader(InputStreamReader(p.inputStream))
            var line = ""
            while (reader.readLine().also { line = it } != null) {
                output.append(line + "\n")
            }
            if (output.toString().trim { it <= ' ' }.equals("", ignoreCase = true)) {
                // No output, try to read an error!
                val errorReader =
                    BufferedReader(InputStreamReader(p.errorStream))
                while (errorReader.readLine().also { line = it } != null) {
                    output.append(line + "\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return output.toString()
    }

//    fun autoSetDeviceId(context: Context): Boolean {
//        val deviceIdUse: String = SettingsHelper.getInstance(context).getDeviceIdUse()
//        var deviceId: String? = null
//        Log.d(Const.LOG_TAG, "Device ID choice: $deviceIdUse")
//        if (BuildConfig.DEVICE_ID_CHOICE.equals("imei") || "imei" == deviceIdUse) {
//            deviceId = DeviceInfoProvider.getImei(context)
//        } else if (BuildConfig.DEVICE_ID_CHOICE.equals("serial") || "serial" == deviceIdUse) {
//            deviceId = DeviceInfoProvider.getSerialNumber()
//            if (deviceId == Build.UNKNOWN) {
//                deviceId = null
//            }
//        } else if (BuildConfig.DEVICE_ID_CHOICE.equals("mac")) {
//            deviceId = DeviceInfoProvider.getMacAddress()
//        }
//        return if (deviceId == null || deviceId.length == 0) {
//            false
//        } else SettingsHelper.getInstance(context.applicationContext).setDeviceId(deviceId)
//    }

    fun becomeDeviceOwnerByXmlFile(context: Context?): Boolean {
        val cn = LegacyUtils.getAdminComponentName(context!!)
        Log.d("FATAL", "becomeDeviceOwnerByXmlFile: ${cn.className}")
        val deviceOwnerFileName = "/data/system/device_owner_2.xml"
        val deviceOwnerFileContent = """<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<root>
<device-owner package="${cn.packageName}" name="" component="${cn.packageName}/${cn.className}" userRestrictionsMigrated="true" canAccessDeviceIds="true" />
<device-owner-context userId="0" />
</root>"""
        val devicePoliciesFileName = "/data/system/device_policies.xml"
        val devicePoliciesFileContent = """<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<policies setup-complete="true" provisioning-state="3">
<admin name="${cn.packageName}/${cn.className}">
<policies flags="17" />
<strong-auth-unlock-timeout value="0" />
<user-restrictions no_add_managed_profile="true" />
<default-enabled-user-restrictions>
<restriction value="no_add_managed_profile" />
</default-enabled-user-restrictions>
<cross-profile-calendar-packages />
</admin>
<password-validity value="true" />
<lock-task-features value="16" />
</policies>"""
        if (!writeStringToFile(deviceOwnerFileName, deviceOwnerFileContent, false)) {
            Log.e(
                "FATAL",
                "Could not create device owner file $deviceOwnerFileName"
            )
            return false
        }

        // Now when we succeeded to create the device owner file, let's update device policies file
        if (!writeStringToFile(devicePoliciesFileName, devicePoliciesFileContent, true)) {
            Log.e(
                "FATAL",
                "Could not update device policies file $devicePoliciesFileName"
            )
            return false
        }
        return true
    }

    // https://stackoverflow.com/questions/10061154/how-to-programmatically-enable-disable-accessibility-service-in-android
    fun autoSetAccessibilityPermission(context: Context, packageName: String, className: String) {
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "$packageName/$className"
        )
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, "1"
        )
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun autoSetOverlayPermission(context: Context, packageName: String) {
        val packageManager = context.packageManager
        var uid = 0
        uid = try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            applicationInfo.uid
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return
        }
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val OP_SYSTEM_ALERT_WINDOW = 24

        // src/com/android/settings/applications/DrawOverlayDetails.java
        // See method: void setCanDrawOverlay(boolean newState)
        try {
            val clazz: Class<*> = AppOpsManager::class.java
            val method = clazz.getDeclaredMethod(
                "setMode",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java,
                Int::class.javaPrimitiveType
            )
            method.invoke(
                appOpsManager,
                OP_SYSTEM_ALERT_WINDOW,
                uid,
                packageName,
                AppOpsManager.MODE_ALLOWED
            )
            Log.d("FATAL", "Overlay permission granted to $packageName")
        } catch (e: Exception) {
            Log.e("FATAL", Log.getStackTraceString(e))
        }
    }
}

