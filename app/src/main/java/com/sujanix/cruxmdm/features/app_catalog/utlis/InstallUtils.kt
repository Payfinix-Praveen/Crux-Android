package com.sujanix.cruxmdm.features.app_catalog.utlis

import android.Manifest
import android.annotation.TargetApi
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionCallback
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.ApplicationEntity
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application.Companion.toApplicationEntity
import com.sujanix.cruxmdm.features.core.presentation.view.MainActivity
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Constant.ACTION_INSTALL_COMPLETE
import com.sujanix.cruxmdm.features.core.utlis.LegacyUtils.getAdminComponentName
import com.sujanix.cruxmdm.features.core.utlis.SystemUtils.getDpm
import com.sujanix.cruxmdm.features.core.utlis.isApkDownloaded
import com.sujanix.cruxmdm.features.core.utlis.isPackageInstalled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Locale
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object InstallUtils {
    
    const val LOG = "InstallUtils"
    private const val FILE_NAME = "TrueRead Link.apk"
    private const val FILE_BASE_PATH = "file://"
    private const val MIME_TYPE = "application/vnd.android.package-archive"
    private const val PROVIDER_PATH = ".provider"
    private const val APP_INSTALL_TYPE = "\"application/vnd.android.package-archive\""

    fun enqueueDownload(context: Context, application: Application, onDownloadComplete: ((filePath: String) -> Unit)? = null) {
//        val downloadDialog = ProgressDialog(context, R.layout.progress_download)
//        downloadDialog.setTitle("Downloading APK...")
//        downloadDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            var destination = //context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
            destination += "${application.name}.apk"
//            val file = File(destination, "${application.name}.apk")
            val uri = Uri.parse("$FILE_BASE_PATH$destination")
            val file = File(destination)

            if (file.exists()) file.delete()
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = Uri.parse(application.url)
//            val downloadUri = Uri.parse("https://s3.us-east-1.wasabisys.com/crux-tests/reactTest.apk?AWSAccessKeyId=WIKDT4WY6JWELZNG21HL&Expires=1715331873&Signature=hq%2B5YPks3rQODhZ2PDyzB6Bx5HM%3D")
            val request = DownloadManager.Request(downloadUri)
            request.setMimeType(MIME_TYPE)
            request.setTitle("Crux MDM")
            request.setDescription("Downloading enterprise apps...")
            // set destination
            request.setDestinationUri(uri)
//            showInstallOption(destination, uri)

            // Enqueue a new download and same the referenceId
            val downloadId = downloadManager.enqueue(request)

            var downloading = true
            Log.d("silentInstallApplication", "onInstallError: called 1")
            // Loop until the download is complete or has failed
            while (downloading) {
                // Get the download progress
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                cursor.moveToFirst()

                val bytesDownloaded =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytesTotal =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    if(isApkFileAccessible(context, uri)) {
//                        installApk(uri, context)
//                    showInstallOption(context, destination, uri)
//                    tempInstallApk(context, file)
                        if(file.exists()) onDownloadComplete?.invoke(file.absolutePath)
                        silentInstallApplication(
                            context,
                            uri,
                            application.pkg!!,
                            object : InstallErrorHandler {
                                override fun onInstallError() {
                                    Log.d(
                                        "silentInstallApplication",
                                        "onInstallError: Error installing APK"
                                    )
                                }
                            })
                    }

                    downloading = false
                } else if (status == DownloadManager.STATUS_FAILED) {
                    // Download failed
                    Log.d("silentInstallApplication", "onInstallError: Error installing APK")
                    downloading = false

                }
                cursor.close()
            }
        }
    }

    private fun installApk(uri: Uri, context: Context) {
        val resolvedUri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            File(uri.path!!)
        )
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            setDataAndType(resolvedUri, "application/vnd.android.package-archive")
        }
        // Grant temporary permission to the content URI
        context.grantUriPermission(
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.component?.packageName,
            resolvedUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        context.startActivity(intent)
    }

    private fun tempInstallApk(context: Context, apkFile: File){
        val packageInstaller =context.packageManager.packageInstaller
        val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
        val sessionId = packageInstaller.createSession(params)

        try {
            val session = packageInstaller.openSession(sessionId)
            val outputStream = session.openWrite("package", 0, -1)

            val inputStream = FileInputStream(apkFile)
            inputStream.copyTo(outputStream)
            session.fsync(outputStream)
            outputStream.close()
            inputStream.close()

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            session.commit(pendingIntent.intentSender)

//            showToast("Installation started")
            Log.d("tempInstallApk", "tempInstallApk: Installation started")
        } catch (e: Exception) {
//            showToast("Error installing APK")
            Log.d("tempInstallApk", "tempInstallApk: Error installing APK")
            e.printStackTrace()
        }
    }
    fun showInstallOption(
        context: Context,
        destination: String,
//        uri: Uri
    ) {
        // set BroadcastReceiver to install app when .apk is downloaded

        Log.d("TAG", "showInstallOption: ${URI(destination)}")

        val file = if(destination.contains("file://")) File(destination.replace("file://", "")) else File(destination)
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val install = Intent(Intent.ACTION_VIEW)
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        install.data = contentUri
        context.startActivity(install)
//        context.unregisterReceiver(this)
//        val onComplete = object : BroadcastReceiver() {
//            override fun onReceive(
//                context: Context,
//                intent: Intent
//            ) {
//                val contentUri = FileProvider.getUriForFile(
//                    context,
//                    "${context.packageName}.fileprovider",
//                    File(destination)
//                )
//                val install = Intent(Intent.ACTION_VIEW)
//                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
//                install.data = contentUri
//                context.startActivity(install)
//                context.unregisterReceiver(this)
//                // finish()
//            }
//        }
//        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun isApkFileAccessible(context: Context, apkUri: Uri): Boolean {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageArchiveInfo(apkUri.path!!, PackageManager.GET_ACTIVITIES)

        // If packageInfo is not null, the APK file is accessible
        return packageInfo != null
    }

    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadAndInstallApk(
        context: Context,
        application: Application,
        onDownloadComplete: ((filePath: String) -> Unit)? = null
    ){
        CoroutineScope(Dispatchers.IO).launch {
            val file = downloadFile(
                context,
                application.name!!,
                application.url!!,
                object : DownloadProgress {
                    override fun onDownloadProgress(progress: Int, total: Long, current: Long) {
//                        Log.d("downloadAndInstallApk", "onDownloadProgress: $progress")
                    }
                })
            Log.d("FATAL", "downloadAndInstallApk: ${file.path}")
            if(file.exists()) onDownloadComplete?.invoke(file.absolutePath)

            val packageManager = context.packageManager
            val isAdminApp = packageManager.checkPermission(
                Manifest.permission.BIND_DEVICE_ADMIN,
                context.packageName
            )

            if (isAdminApp == PackageManager.PERMISSION_GRANTED) {
                // The app has device admin permission
                silentInstallApplication(context, Uri.fromFile(file), application.pkg!!, object : InstallErrorHandler {
                    override fun onInstallError() {
                        Log.d("silentInstallApplication", "onInstallError: Error installing APK")
                    }
                })
            } else {
                // The app does not have device admin permission
                Log.d("silentInstallApplication", "onInstallError: Crux MDM requires device admin permission")
            }

//            try {
//                installPackage(context, Uri.fromFile(file), application.pkg!!)
//            } catch (e: Exception){
//
//            }
        }
    }

    fun silentInstallApplication(
        context: Context,
        fileUri: Uri,
        packageName: String,
        errorHandler: InstallErrorHandler
    ) {
        try {
            Log.i(LOG, "Installing $packageName")
//            val inputStream = context.contentResolver.openInputStream(fileUri)
            val inputStream = context.assets.open("xyz.apk")
            val packageInstaller = context.packageManager.packageInstaller
//            packageInstaller.registerSessionCallback(SessionCallbackList(errorHandler))
            val params = SessionParams(
                SessionParams.MODE_FULL_INSTALL
            )
            params.setAppPackageName(packageName)

            // set params
            val sessionId = packageInstaller.createSession(params)
            clearMyRestrictions(context)
            val session = packageInstaller.openSession(sessionId)
            addMyRestrictions(context)
            val out = session.openWrite("COSU", 0, -1)
            val buffer = ByteArray(65536)
            var c: Int
            while ((inputStream?.read(buffer).also { c = it ?: 0 }) != -1) {
                out.write(buffer, 0, c)
            }
            session.fsync(out)
            inputStream?.close()
            out.close()

            session.commit(createIntentSender(context, sessionId, packageName))
            session.close()
            Log.i(LOG, "Installation session committed")
        } catch (e: Exception) {
            Log.w(LOG, "PackageInstaller error: " + e.message)
            e.printStackTrace()
            errorHandler.onInstallError()
        }
    }

    fun createIntentSender(context: Context?, sessionId: Int, packageName: String?): IntentSender {
//        val intent: Intent = Intent(ACTION_INSTALL_COMPLETE)
//        if (packageName != null) {
//            intent.putExtra(Constant.PACKAGE_NAME, packageName)
//        }
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            sessionId,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
        val statusReceiver: IntentSender? = null
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1337111117, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        return pendingIntent.intentSender
    }


    @Throws(IOException::class)
    fun installPackage(context: Context, fileUri: Uri, packageName: String?): Boolean {
        val `in` = context.contentResolver.openInputStream(fileUri)!!
        val packageInstaller = context.packageManager.packageInstaller
        val params = SessionParams(
            SessionParams.MODE_FULL_INSTALL
        )
        params.setAppPackageName(packageName)
        // set params
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)
        val out = session.openWrite("COSU", 0, -1)
        val buffer = ByteArray(65536)
        var c: Int
        while ((`in`.read(buffer).also { c = it }) != -1) {
            out.write(buffer, 0, c)
        }
        session.fsync(out)
        `in`.close()
        out.close()

        session.commit(createIntentSender(context, sessionId))
        return true
    }

    private fun createIntentSender(context: Context, sessionId: Int): IntentSender {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            Intent(ACTION_INSTALL_COMPLETE),
            PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent.intentSender
    }

    fun checkApkNeedsUpdates(context: Context, application: Application): Boolean {
        return try {
            val info = context.packageManager?.getPackageInfo(application.pkg!!, 0)
            val versionCode =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) info?.versionCode?.toLong()
                else info?.longVersionCode
            versionCode!! > application.versionCode!!.toLong()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun isInList(applicationsForInstall: List<ApplicationEntity>, a: ApplicationEntity): Boolean {
        for (b in applicationsForInstall) {
//            if (a.pkg.equals(b.pkg,true) && a.version.equals(b.version, true) ||

            if (a.pkg.equals(b.pkg, true) && a.versionCode == b.versionCode) {
//                Log.d("FATAL", "isInList: ${b.type == "Self Hosted Private App" && b.apkPath == null}")
                if(b.type == "Self Hosted Private App" && b.apkPath == null) return false
                return true
            }
        }
        return false
    }

    fun isApkNeedUpdate(applicationsForInstall: List<ApplicationEntity>, a: ApplicationEntity): Boolean {
        for (b in applicationsForInstall) {
//            if (a.pkg.equals(b.pkg,true) && a.version.equals(b.version, true) ||
            if (a.pkg.equals(b.pkg, true) && a.versionCode == b.versionCode) {
                return true
            }
        }
        return false
    }

    fun checkApplicationDownloadAndInstallStatus(context: Context, application: Application): ApplicationEntity {
        val applicationEntity = application.toApplicationEntity()

        val temp = applicationEntity.copy(
            isInstalled = isPackageInstalled(context, applicationEntity.pkg),
            isDownloaded = isApkDownloaded(context, applicationEntity.name)
        )
        return temp
    }

    fun generateApplicationsForInstallList(
        context: Context, applications: List<Application>,
        applicationsForInstall: MutableList<Application>,
        pendingInstallations: Map<String?, File?>
    ) {
        val packageManager = context.packageManager

        // First handle apps to be removed, then apps to be installed
        // We process only applications of type "app" (default) and skip web links and others
        for (a in applications) {
            if ((a.type == null || a.type.equals(Application.TYPE_APP)) && a.isRemove &&
                !isInList(applicationsForInstall, a)
            ) {
                Log.d(
                    LOG,
                    ("checkAndUpdateApplications(): marking app " + a.pkg).toString() + " to remove"
                )
                applicationsForInstall.add(a)
            }
        }
        for (a in applications) {
            if ((a.type == null || a.type
                    .equals(Application.TYPE_APP)) && !a.isRemove &&
                !pendingInstallations.containsKey(a.pkg) && !isInList(
                    applicationsForInstall,
                    a
                )
            ) {
                Log.d(
                    LOG,
                    ("checkAndUpdateApplications(): marking app " + a.pkg).toString() + " to install"
                )
                applicationsForInstall.add(a)
            }
        }
        val it: MutableIterator<Application> = applicationsForInstall.iterator()

        while (it.hasNext()) {
            val application: Application = it.next()
            if ((application.url == null || application.url.trim()
                    .equals("")) && !application.isRemove
            ) {
                // An app without URL is a system app which doesn't require installation
                Log.d(
                    LOG,
                    ("checkAndUpdateApplications(): app " + application.pkg).toString() + " is system, skipping"
                )
                it.remove()
                continue
            }

            try {
                val packageInfo: PackageInfo =
                    packageManager.getPackageInfo(application.pkg!!, 0)

                if (application.isRemove && !application.version.equals("0") &&
                    !areVersionsEqual(
                        packageInfo.versionName,
                        packageInfo.versionCode,
                        application.version,
                        application.versionCode
                    )
                ) {
                    // If a removal is required, but the app version doesn't match, do not remove
                    Log.d(
                        LOG,
                        (("checkAndUpdateApplications(): app " + application.pkg).toString() + " version not match: "
                                + application.version).toString() + " " + packageInfo.versionName + ", skipping"
                    )
                    it.remove()
                    continue
                }

//                if (!application.isRemove && !upgradingHmdmFreeToFull(
//                        context,
//                        application,
//                        packageInfo
//                    ) &&
//                    (application.isSkipVersion() || application.version.equals("0") ||
                    if(areVersionsEqual(
                                packageInfo.versionName,
                                packageInfo.versionCode,
                                application.version,
                                application.versionCode
                            )
                ) {
                    // If installation is required, but the app of the same version already installed, do not install
                    Log.d(
                        LOG,
                        (("checkAndUpdateApplications(): app " + application.pkg).toString() + " versions match: "
                                + application.version).toString() + " " + packageInfo.versionName + ", skipping"
                    )
                    it.remove()
                    continue
                }

                if (!application.isRemove &&
                    compareVersions(
                        packageInfo.versionName,
                        packageInfo.versionCode,
                        application.version,
                        application.versionCode
                    ) > 0
                ) {
                    // Downgrade requested!
                    // It will only succeed if a higher version is marked as "Remove"
                    // Let's check that condition to avoid failed attempts to install and downloads of the lower version each time
                    Log.d(
                        LOG,
                        ("Downgrade requested for " + application.pkg).toString() +
                                ": installed version " + packageInfo.versionName + ", required version " + application.version
                    )
                    var canDowngrade = false
                    for (a in applications) {
                        if (a.pkg.equals(application.pkg, true) && a.isRemove &&
                            areVersionsEqual(
                                packageInfo.versionName,
                                packageInfo.versionCode,
                                a.version,
                                a.versionCode
                            )
                        ) {
                            // Current version will be removed
                            canDowngrade = true
                            break
                        }
                    }
                    if (canDowngrade) {
                        Log.d(
                            LOG,
                            ("Current version of " + application.pkg).toString() + " will be removed, downgrade allowed"
                        )
                    } else {
                        Log.d(
                            LOG,
                            ("Ignoring downgrade request for " + application.pkg).toString() + ": remove current version first!"
                        )
                        it.remove()
                        continue
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // The app isn't installed, let's keep it in the "To be installed" list
                if (application.isRemove) {
                    // The app requires removal but already removed, remove from the list so do nothing with the app
                    Log.d(
                        LOG,
                        ("checkAndUpdateApplications(): app " + application.pkg).toString() + " not found, nothing to remove"
                    )
                    it.remove()
                    continue
                }
            }
        }
    }


    private fun isInList(applicationsForInstall: List<Application>, a: Application): Boolean {
        for (b in applicationsForInstall) {
            if (a.pkg.equals(b.pkg,true) &&
                a.version
                    .equals(b.version, true) && a.isRemove === b.isRemove
            ) {
                return true
            }
        }
        return false
    }

    // Free and full versions of Headwind MDM launcher have the same version name but different version codes
    // This is a dirty hack determining the full version by the URL
    // It's however better to use different versions, for example 5.16.1 for free and 5.16.2 for full
//    private fun upgradingHmdmFreeToFull(
//        context: Context,
//        application: Application,
//        packageInfo: PackageInfo
//    ): Boolean {
//        if (!application.pkg.equals(context.packageName)) {
//            return false
//        }
//        return Utils.getLauncherVariant().equals("opensource") && application.url
//            .endsWith("master.apk")
//    }

    private fun areVersionsEqual(v1: String?, c1: Int, v2: String?, c2: Int?): Boolean {
        if (c2 != null && c2 != 0) {
            // If version code is present, let's compare version codes instead of names
            return c1 == c2
        }

        if (v1 == null || v2 == null) {
            // Exceptional case, we should never be here but this shouldn't crash the app with NPE
            return v1 === v2
        }

        // Compare only digits (in Android 9 EMUI on Huawei Honor 8A, getPackageInfo doesn't get letters!)
        val v1d = v1.replace("[^\\d.]".toRegex(), "")
        val v2d = v2.replace("[^\\d.]".toRegex(), "")
        return v1d == v2d
    }

    // Returns -1 if v1 < v2, 0 if v1 == v2 and 1 if v1 > v2
    fun compareVersions(v1: String?, c1: Int, v2: String?, c2: Int?): Int {
        if (c2 != null && c2 != 0) {
            // If version code is present, let's compare version codes instead of names
            return if (c1 < c2) {
                -1
            } else if (c1 > c2) {
                1
            } else {
                0
            }
        }

        // Exceptional cases: null values
        if (v1 == null && v2 == null) {
            return 0
        }
        if (v1 == null) {
            return -1
        }
        if (v2 == null) {
            return 1
        }
        // Versions are numbers separated by a dot
        val v1d = v1.replace("[^\\d.]".toRegex(), "")
        val v2d = v2.replace("[^\\d.]".toRegex(), "")

        val v1n = v1d.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val v2n = v2d.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        // One version could contain more digits than another
        val count = if (v1n.size < v2n.size) v1n.size else v2n.size

        for (n in 0 until count) {
            try {
                val n1 = v1n[n].toInt()
                val n2 = v2n[n].toInt()
                if (n1 < n2) {
                    return -1
                } else if (n1 > n2) {
                    return 1
                }
                // If major version numbers are equals, continue to compare minor version numbers
            } catch (e: Exception) {
                return 0
            }
        }

        // Here we are if common parts are equal
        // Now we decide that if a version has more parts, it is considered as greater
        if (v1n.size < v2n.size) {
            return -1
        } else if (v1n.size > v2n.size) {
            return 1
        }
        return 0
    }

//    fun generateFilesForInstallList(
//        context: Context?, files: List<RemoteFile?>,
//        filesForInstall: MutableList<RemoteFile?>
//    ) {
//        val TIME_TOLERANCE_MS: Long = 60000
//        for (remoteFile in files) {
//            val file = File(Environment.getExternalStorageDirectory(), remoteFile.getPath())
//            if (remoteFile.isRemove) {
//                if (file.exists()) {
//                    filesForInstall.add(remoteFile)
//                }
//            } else {
//                if (!file.exists()) {
//                    filesForInstall.add(remoteFile)
//                } else {
//                    val remoteFileDb: RemoteFile = RemoteFileTable.selectByPath(
//                        DatabaseHelper.instance(context).getReadableDatabase(),
//                        remoteFile.getPath()
//                    )
//                    if (remoteFileDb != null) {
//                        if (remoteFileDb.getChecksum() == null || !remoteFileDb.getChecksum()
//                                .equalsIgnoreCase(remoteFile.getChecksum())
//                        ) {
//                            filesForInstall.add(remoteFile)
//                        }
//                    } else {
//                        // Entry not found in the database, let's check the checksum
//                        try {
//                            val checksum: String =
//                                CryptoUtils.calculateChecksum(FileInputStream(file))
//                            if (checksum.equals(remoteFile.getChecksum(), ignoreCase = true)) {
//                                // File is correct, just save the entry in the database
//                                RemoteFileTable.insert(
//                                    DatabaseHelper.instance(context).getWritableDatabase(),
//                                    remoteFile
//                                )
//                            } else {
//                                filesForInstall.add(remoteFile)
//                            }
//                        } catch (e: FileNotFoundException) {
//                            // We should never be here!
//                            filesForInstall.add(remoteFile)
//                        }
//                    }
//                }
//            }
//        }
//    }

    @Throws(Exception::class)
    fun downloadFile(context: Context, appName: String, strUrl: String, progressHandler: DownloadProgress): File {
        var tempFile = File(context.getExternalFilesDir(null), "${appName}.apk")
        if (tempFile.exists()) {
            tempFile.delete()
        }

        try {
            try {
                tempFile.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()

                tempFile = File.createTempFile(getFileName(strUrl), "temp")
            }

            val url = URL(strUrl)

            val connection: HttpURLConnection
//            if (BuildConfig.TRUST_ANY_CERTIFICATE && url.protocol.lowercase(Locale.getDefault()) == "https") {
            if (url.protocol.lowercase(Locale.getDefault()) == "https") {
                connection = url.openConnection() as HttpsURLConnection
                connection.hostnameVerifier =
                    DO_NOT_VERIFY
            } else {
                connection = url.openConnection() as HttpURLConnection
            }
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.connectTimeout = Constant.CONNECTION_TIMEOUT
            connection.readTimeout = Constant.CONNECTION_TIMEOUT
            val signature = getRequestSignature(strUrl)
            if (signature != null) {
                connection.setRequestProperty("X-Request-Signature", signature)
            }
            connection.connect()

            if (connection.responseCode != 200) {
                throw Exception("Bad server response for " + strUrl + ": " + connection.responseCode)
            }

            val lengthOfFile = connection.contentLength

            progressHandler.onDownloadProgress(0, lengthOfFile.toLong(), 0)

            val `is` = connection.inputStream
            val dis = DataInputStream(`is`)

            val buffer = ByteArray(1024)
            var length: Int
            var total: Long = 0

            val fos = FileOutputStream(tempFile)
            while ((dis.read(buffer).also { length = it }) > 0) {
                total += length.toLong()
                progressHandler.onDownloadProgress(
                    ((total * 100.0f) / lengthOfFile).toInt(),
                    lengthOfFile.toLong(),
                    total
                )
                fos.write(buffer, 0, length)
            }
            fos.flush()
            fos.close()

            dis.close()
        } catch (e: Exception) {
            tempFile.delete()
            throw e
        }

        return tempFile
    }

    fun getRequestSignature(strUrl: String): String? {
        var index = strUrl.indexOf("/files/", 0)
        if (index == -1) {
            // Seems to be an external resource, do not add signature
            return null
        }
        index += "/files/".length
        val filepath = strUrl.substring(index)

//        try {
//            return CryptoHelper.getSHA1String("changeme-C3z9vi54" + filepath)
//        } catch (e: Exception) {
//        }
        return null
    }

    private fun getFileName(strUrl: String): String {
        return strUrl.substring(strUrl.lastIndexOf("/"))
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun silentUninstallApplication(context: Context, packageName: String?) {
        val packageInstaller = context.packageManager.packageInstaller
        try {
            packageInstaller.uninstall(packageName!!, createIntentSender(context, 0, null))
        } catch (e: Exception) {
            // If we're trying to remove an unexistent app, it causes an exception so just ignore it
        }
    }

    fun requestInstallApplication(
        context: Context,
        file: File,
        errorHandler: InstallErrorHandler?
    ) {
//        if (file.name.endsWith(".xapk")) {
//            XapkUtils.install(context, XapkUtils.extract(context, file), null, errorHandler)
//            return
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                file
            )
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            // Let's set Intent.FLAG_ACTIVITY_NEW_TASK here
            // Some devices report:
            // android.util.AndroidRuntimeException
            // Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val apkUri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun requestUninstallApplication(context: Context, packageName: String) {
        val packageUri = Uri.parse("package:$packageName")
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
        // Let's set Intent.FLAG_ACTIVITY_NEW_TASK here
        // Some devices report:
        // android.util.AndroidRuntimeException
        // Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPackageInstallerStatusMessage(status: Int): String {
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> return "PENDING_USER_ACTION"
            PackageInstaller.STATUS_SUCCESS -> return "SUCCESS"
            PackageInstaller.STATUS_FAILURE -> return "FAILURE_UNKNOWN"
            PackageInstaller.STATUS_FAILURE_BLOCKED -> return "BLOCKED"
            PackageInstaller.STATUS_FAILURE_ABORTED -> return "ABORTED"
            PackageInstaller.STATUS_FAILURE_INVALID -> return "INVALID"
            PackageInstaller.STATUS_FAILURE_CONFLICT -> return "CONFLICT"
            PackageInstaller.STATUS_FAILURE_STORAGE -> return "STORAGE"
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> return "INCOMPATIBLE"
        }
        return "UNKNOWN"
    }

    // always verify the host - dont check for certificate
    val DO_NOT_VERIFY: HostnameVerifier =
        HostnameVerifier { hostname, session -> true }

    /**
     * Trust every server - dont check for any certificate
     * This should be called at the app start if TRUST_ANY_CERTIFICATE is set to true
     */
    fun initUnsafeTrustManager() {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {
            }
        })

        // Install the all-trusting trust manager
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection
                .setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteTempApk(file: File) {
        try {
            if (file.name.endsWith(".xapk")) {
                // For XAPK, we need to remove the directory with the same name
                val path = file.absolutePath
                val directory = File(path.substring(0, path.length - 5))
                if (directory.exists()) {
                    deleteRecursive(directory)
                }
            }
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
        }
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )

        fileOrDirectory.delete()
    }

    fun clearTempFiles(context: Context) {
        try {
            val filesDir = context.getExternalFilesDir(null)
            for (child in filesDir!!.listFiles()) {
                if (child.name.equals("MqttConnection", ignoreCase = true)) {
                    // These are names which should be kept here
                    continue
                }
                if (child.isDirectory) {
                    deleteRecursive(child)
                } else {
                    child.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class SessionCallbackList(
        private val errorHandler: InstallErrorHandler
    ): SessionCallback(){
        override fun onCreated(sessionId: Int) {
            TODO("Not yet implemented")
        }

        override fun onBadgingChanged(sessionId: Int) {
            TODO("Not yet implemented")
        }

        override fun onActiveChanged(sessionId: Int, active: Boolean) {
            TODO("Not yet implemented")
        }

        override fun onProgressChanged(sessionId: Int, progress: Float) {
            TODO("Not yet implemented")
        }

        override fun onFinished(sessionId: Int, success: Boolean) {
            Handler(Looper.getMainLooper()).post {
                if (success) {
                    Log.i(LOG, "Installation successful")
                    // Handle success
                } else {
                    Log.w(LOG, "Installation failed")
                    // Handle failure
                    errorHandler.onInstallError()
                }
            }
        }
    }

    fun clearMyRestrictions(context: Context) {
        getDpm(context).clearUserRestriction(
            getAdminComponentName(context),
            UserManager.DISALLOW_INSTALL_APPS
        )
        getDpm(context).clearUserRestriction(
            getAdminComponentName(context),
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES
        )
    }

    fun addMyRestrictions(context: Context) {
        getDpm(context).addUserRestriction(
            getAdminComponentName(context),
            UserManager.DISALLOW_INSTALL_APPS
        )
        getDpm(context).addUserRestriction(
            getAdminComponentName(context),
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES
        )
    }

    fun openPlayStore(context: Context, appPackageName: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=$appPackageName")
            setPackage("com.android.vending") // Specify Play Store as the target app
        }

        try {
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // Play Store is not installed, open the app page in a browser instead
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
//                Uri.parse("https://play.google.com/store/apps/details?id=com.sujanix.helloandroid")
            )
            context.startActivity(browserIntent)
        }
    }

    fun uninstallApp(context: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
        }
        context.startActivity(intent)
    }

    interface InstallErrorHandler {
        fun onInstallError()
    }
}
