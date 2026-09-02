package com.example.androidbljoy.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String,
    val forceUpdate: Boolean
)

enum class InstallResult {
    SUCCESS,
    NEED_PERMISSION,
    ERROR
}

class UpdateManager(private val context: Context) {

    // The update URL on Firebase Storage for bucket: androidblejoy.firebasestorage.app
    private val updateJsonUrl = "https://storage.googleapis.com/androidblejoy.firebasestorage.app/updates/update.json"

    fun getCurrentVersionCode(): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            1L
        }
    }

    fun getCurrentVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$updateJsonUrl?t=${System.currentTimeMillis()}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val updateInfo = Gson().fromJson(jsonText, UpdateInfo::class.java)
                val currentCode = getCurrentVersionCode()
                
                if (updateInfo.versionCode > currentCode) {
                    return@withContext updateInfo
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun downloadApk(apkUrl: String, onProgress: (Float) -> Unit): File? = withContext(Dispatchers.IO) {
        try {
            val url = URL(apkUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val contentLength = connection.contentLength
                // Use external files dir for maximum compatibility with PackageInstaller across vendors
                val baseDir = context.getExternalFilesDir(null) ?: context.cacheDir
                val updatesDir = File(baseDir, "updates")
                if (!updatesDir.exists()) {
                    updatesDir.mkdirs()
                }
                
                val apkFile = File(updatesDir, "app-update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                connection.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                                onProgress(progress)
                            }
                        }
                    }
                }
                return@withContext apkFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    fun installApk(apkFile: File): InstallResult {
        try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                return InstallResult.ERROR
            }

            // Android 8.0+ (API 26+) check for unknown sources permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    return InstallResult.NEED_PERMISSION
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // Grant URI permission explicitly to any matching package installer
            val resolveInfos = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (info in resolveInfos) {
                context.grantUriPermission(info.activityInfo.packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
            return InstallResult.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            return InstallResult.ERROR
        }
    }
}
