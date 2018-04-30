package wtf.qase.customgoogleplay

import com.google.android.vending.expansion.downloader.impl.DownloaderService

class ExpansionDownloaderService : DownloaderService() {

    override fun getPublicKey(): String {
        return BuildConfig.PLAYSTORE_PUBLIC_KEY
    }

    override fun getSALT(): ByteArray {
        return byteArrayOf(1, 42, -12, -1, 54, 98, -100, -12, 43, 2, -8, -4, 9, 5, -106, -107, -33, 45, -1, 84)
    }

    override fun getAlarmReceiverClassName(): String {
        return ExpansionAlarmReceiver::class.java.name
    }
}
