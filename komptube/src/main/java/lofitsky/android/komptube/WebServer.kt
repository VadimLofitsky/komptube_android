package lofitsky.android.komptube

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import lofitsky.komptube.common.Mode
import java.util.Date

class WebServer(private val callback: (UrlInfo) -> Unit) : NanoHTTPD(KOMPTUBE_PHONE_PORT) {
    companion object {
        internal const val KOMPTUBE_PHONE_PORT = 8765

        data class UrlInfo(val url: String, val title: String? = null)
    }

    override fun serve(session: IHTTPSession?): Response {
        Log.i(LOG_TAG, "${this::class.simpleName}: new request query: ${session?.queryParameterString}")
        Log.i(LOG_TAG, "${this::class.simpleName}: parms: ${session?.parms}")

        if(session?.method != Method.GET) {
            return newFixedLengthResponse(
                Response.Status.METHOD_NOT_ALLOWED,
                MIME_PLAINTEXT,
                "Method ${session?.method} not allowed."
            )
        }

        if(Mode.valueOfOrNull(session.parms["mode"]?.takeIf { it.isNotBlank() }) == Mode.HEALTH_CHECK) {
            Log.i(LOG_TAG, "${this::class.simpleName}: health-check is successful")
            return newFixedLengthResponse("alive at ${Date()}")
        }

        session.parms["url"]
            ?.takeIf { it.isNotBlank() }
            ?.let { url -> UrlInfo(url, session.parms["title"]?.takeIf { it.isNotBlank() }) }
            ?.also(callback)
            ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Parameter 'url' not defined.")

        return newFixedLengthResponse("ok")
    }
}
