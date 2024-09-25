package lofitsky.android.komptube

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import lofitsky.android.komptube.WebServer.Companion.KOMPTUBE_PHONE_PORT
import lofitsky.android.komptube.WebServer.Companion.UrlInfo
import java.net.NetworkInterface
import java.util.Random


class KomptubeHttpService : Service() {
    companion object {
        internal const val KOMPTUBE_BG_SERVICE_ACTION_START = "KOMPTUBE_BG_SERVICE_ACTION_START"
    }

    inner class WebserverServiceBinder : Binder() {
        fun getService(): KomptubeHttpService = this@KomptubeHttpService

        fun startWebserver() = this@KomptubeHttpService.startWebserver()
        fun stopWebserver() = this@KomptubeHttpService.stopWebserver()
    }

    private lateinit var webServer: WebServer

    private val ownWebserverHealthChecker = OwnWebserverHealthChecker()

    override fun onBind(intent: Intent): IBinder? = WebserverServiceBinder()

    override fun onCreate() {
        super.onCreate()

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val notification = NotificationHelper.build(this, "Komptube", "Сервис запущен", null)
        webServer = WebServer(this::openUrl)
        startForeground(Random().nextInt(), notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(LOG_TAG, "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv ${this::class.simpleName}@onStartCommand vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv")
        Log.i(LOG_TAG, "Got intent $intent")
        Log.i(LOG_TAG, "Action: ${intent?.action}")
        Log.i(LOG_TAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ ${this::class.simpleName}@onStartCommand ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")

        when(intent?.action) {
            KOMPTUBE_BG_SERVICE_ACTION_START -> {
                val notification = NotificationHelper.build(this, "Komptube", "Запущен http сервер. ${getSelfIP()}:$KOMPTUBE_PHONE_PORT", null)
                if(!isServerAlive()) startWebserver()
                startForeground(Random().nextInt(), notification)
            }
        }

        return Service.START_REDELIVER_INTENT
    }

//    private fun startWebserver() { webServer.start() }
//    private fun stopWebserver() { webServer.stop() }
//    fun isServerAlive(): Boolean = webServer.isAlive

    private fun startWebserver() { if(!isServerAlive()) webServer.start() }
    private fun stopWebserver() { webServer.stop() }
    fun isServerAlive(): Boolean = ownWebserverHealthChecker.webserverHealthCheck()

    private fun getSelfIP(): String = NetworkInterface.getNetworkInterfaces()
        ?.toList()?.firstNotNullOfOrNull { iface ->
            iface.inetAddresses?.toList()?.firstNotNullOfOrNull { addr ->
                addr?.takeIf { !addr.isLoopbackAddress }?.hostAddress?.let { ha ->
                    ha.takeIf { !it.contains(":") }
                }
            }
        }  ?: "<not detected>"

    private fun openUrl0(url: String): Unit {
        Log.i(LOG_TAG, "${this::class.simpleName}.openUrl(): send intent to open url '$url'")
        Thread {
            Intent(Intent.ACTION_VIEW)
                .apply {
                    data = Uri.parse(url)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                .also { startActivity(it) }
        }.start()
    }

    private fun openUrl(urlInfo: UrlInfo): Unit {
        Log.i(LOG_TAG, "${this::class.simpleName}.openUrl(): send intent to open url '${urlInfo.url}'")
        Thread {
            val viewIntent = Intent(Intent.ACTION_VIEW)
                .apply {
                    data = Uri.parse(urlInfo.url)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.let { PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_MUTABLE) }

            val title = urlInfo.title ?: urlInfo.url
            val text = urlInfo.title?.let { urlInfo.url } ?: ""
            NotificationHelper.notify(this, title, text, viewIntent)
        }.start()
    }
}
