package wtf.qase.customgoogleplay

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Messenger
import android.util.Log
import com.android.vending.expansion.zipfile.APKExpansionSupport
import com.google.android.vending.expansion.downloader.DownloadProgressInfo
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller
import com.google.android.vending.expansion.downloader.Helpers
import com.google.android.vending.expansion.downloader.IDownloaderClient
import com.google.android.vending.expansion.downloader.IDownloaderService
import com.google.android.vending.expansion.downloader.IStub
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ExpansionFilesHelper(private val ctx: MainActivity) : IDownloaderClient {

    companion object {
        val TAG: String = ExpansionFilesHelper::class.java.simpleName
    }

    private var downloaderClientStub: IStub? = null
    private var remoteService: IDownloaderService? = null

    init {
        downloaderClientStub = DownloaderClientMarshaller.CreateStub(this, ExpansionDownloaderService::class.java)
        if (expansionFilesDelivered(ctx)) {
            Log.d(TAG, "expansion files delivered")
            extract()
        } else {
            Log.d(TAG, "expansion files NOT delivered")
            try {
                val launchIntent = ctx.intent
                val intentToLaunchThisActivityFromNotification = Intent(ctx, MainActivity::class.java)
                intentToLaunchThisActivityFromNotification.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                intentToLaunchThisActivityFromNotification.action = launchIntent.action

                if (launchIntent.categories != null) {
                    for (category in launchIntent.categories) {
                        intentToLaunchThisActivityFromNotification.addCategory(category)
                    }
                }

                val pendingIntent = PendingIntent.getActivity(
                    ctx,
                    0,
                    intentToLaunchThisActivityFromNotification,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(
                    ctx, pendingIntent, ExpansionDownloaderService::class.java
                )

                if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
                    // TODO initializeDownloadUI()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "cannot find own package: $e")
                e.printStackTrace()
            }
        }
    }

    override fun onServiceConnected(m: Messenger?) {
        remoteService = DownloaderServiceMarshaller.CreateProxy(m)
        remoteService?.onClientUpdated(downloaderClientStub?.messenger)
    }

    override fun onDownloadStateChanged(newState: Int) {
        Log.d(TAG, "-- onDownloadStateChanged($newState) --")
        var showDashboard = true
        var showCellMessage = false
        val paused: Boolean
        val indeterminate: Boolean

        when (newState) {
            IDownloaderClient.STATE_IDLE -> {
                paused = false
                indeterminate = true
            }

            IDownloaderClient.STATE_CONNECTING,
            IDownloaderClient.STATE_FETCHING_URL -> {
                Log.d(TAG, "starting")
                showDashboard = true
                paused = false
                indeterminate = true
            }

            IDownloaderClient.STATE_DOWNLOADING -> {
                Log.d(TAG, "downloading")
                paused = false
                showDashboard = true
                indeterminate = false
            }

            IDownloaderClient.STATE_FAILED_CANCELED,
            IDownloaderClient.STATE_FAILED,
            IDownloaderClient.STATE_FAILED_FETCHING_URL,
            IDownloaderClient.STATE_FAILED_UNLICENSED -> {
                Log.d(TAG, "failed")
                paused = true
                showDashboard = false
                indeterminate = false
            }

            IDownloaderClient.STATE_PAUSED_NEED_CELLULAR_PERMISSION,
            IDownloaderClient.STATE_PAUSED_WIFI_DISABLED_NEED_CELLULAR_PERMISSION -> {
                showDashboard = false
                paused = true
                indeterminate = false
                showCellMessage = true
            }

            IDownloaderClient.STATE_PAUSED_BY_REQUEST -> {
                paused = true
                indeterminate = false
            }
            IDownloaderClient.STATE_PAUSED_ROAMING,
            IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE -> {
                paused = true
                indeterminate = false
            }

            IDownloaderClient.STATE_COMPLETED -> {
                showDashboard = false
                paused = false
                indeterminate = false
                extract()
                return
            }

            else -> {
                paused = true
                indeterminate = true
                showDashboard = true
            }
        }
    }

    override fun onDownloadProgress(progress: DownloadProgressInfo?) {
        Log.d(TAG, "-- onDownloadProgress($progress) --")
    }

    fun start(ctx: Context) {
        Log.d(TAG, "-- start($ctx) --")
        downloaderClientStub?.connect(ctx)
    }

    fun stop(ctx: Context) {
        Log.d(TAG, "-- stop($ctx) --")
        downloaderClientStub?.disconnect(ctx)
    }

    private fun expansionFilesDelivered(ctx: Context): Boolean {
        for (xf in Config.xAPKS) {
            val fileName = Helpers.getExpansionAPKFileName(ctx, xf.isMain, xf.fileVersion)
            if (!Helpers.doesFileExist(ctx, fileName, xf.fileSize, false)) {
                return false
            }
        }
        return true
    }

    @Throws(IOException::class)
    private fun extract() {
        Log.d(TAG, "-- extract() --")

        // TODO Replace constants
        val expansionFile = APKExpansionSupport.getAPKExpansionZipFile(
            ctx, 16, 0
        )

        // TODO Do in IO thread
        Config.xFILES.forEach { xFile ->
            val dir = File(
                File(
                    Environment.getExternalStorageDirectory(),
                    ctx.packageName
                ),
                xFile.value.dir
            )
            val file = File(dir, xFile.key)
            Log.i(TAG, "extracting file $file")
            if (file.canRead() && file.length() == xFile.value.length) {
                Log.d(TAG, "already extracted")
                return@forEach
            } else {
                Log.d(TAG, "invalid file $file (${file.canRead()}, ${file.length()})")
                file.delete()
            }

            saveToFile(
                expansionFile.getInputStream(xFile.key),
                file
            )
        }
    }

    @Throws(IOException::class)
    private fun saveToFile(inputStream: InputStream, file: File) {
        Log.d(TAG, "-- saveToFile(..., $file) --")
        inputStream.use { input ->
            Log.d(TAG, "creating dirs for ${file.parentFile.absolutePath}: ${file.parentFile.mkdirs()}")

            val outputStream = FileOutputStream(file)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024)
                var read = input.read(buffer)
                while (read != -1) {
                    output.write(buffer, 0, read)
                    read = input.read(buffer)
                }
                output.flush()
            }
        }
    }
}
