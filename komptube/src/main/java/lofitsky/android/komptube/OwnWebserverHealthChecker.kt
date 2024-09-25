package lofitsky.android.komptube

import android.os.AsyncTask
import android.util.Log
import android.widget.Button
import lofitsky.komptube.common.Mode
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.FutureTask

class OwnWebserverHealthChecker(private val button: Button? = null) : AsyncTask<(Button?, Boolean?) -> Unit, Void, Boolean>() {
        fun webserverHealthCheck(callback: (Button?, Boolean) -> Unit = { _, _ ->}): Boolean
            = try {
                val job = FutureTask {
                    val request = Request.Builder()
                            .get()
//                            .url("http://192.168.1.3:${WebServer.KOMPTUBE_PHONE_PORT}/?mode=${Mode.HEALTH_CHECK}")
                            .url("http://localhost:${WebServer.KOMPTUBE_PHONE_PORT}/?mode=${Mode.HEALTH_CHECK}")
                        .build()

                    val response = OkHttpClient().newCall(request).execute()
                    Log.i(LOG_TAG, "webserverHealthCheck(): response code = ${response.code}")
                    Log.i(LOG_TAG, "webserverHealthCheck() = ${response.body?.string() ?: "<empty response body>"}")
                    (response.code == 200).also { callback(button, it) }
                }

                Thread(job)
                    .also { it.start() }
                    .join()

                job.get()
            } catch(e: Exception) {
                Log.e(LOG_TAG, "webserverHealthCheck():  $e")
                false
            }

    override fun doInBackground(vararg params: ((Button?, Boolean?) -> Unit)?): Boolean
        = webserverHealthCheck(params[0]!!)
}
