package lofitsky.android.komptube

import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity
import lofitsky.android.komptube.KomptubeHttpService.WebserverServiceBinder
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Random
import kotlin.concurrent.thread

const val LOG_TAG = "KOMPTUBE"

class MainActivity : ComponentActivity() {
    private var serviceBound = false
    private lateinit var webserverServiceIntent: Intent
    private lateinit var clipboard: ClipboardManager

    private lateinit var serverModeSwitch: Switch
    private lateinit var linkEditText: EditText
    private lateinit var encodeLinkButton: Button
    private lateinit var shareLinkButton: Button
    private lateinit var decodeLinkButton: Button
    private lateinit var clearLinkEditButton: Button
    private lateinit var webserverHealthCheckButton: Button
    private lateinit var webserverHealthCheckResult: TextView

    private val okHttpClient = OkHttpClient()

    interface WebserverServiceConnection : ServiceConnection {
        fun startWebserver(): Unit
        fun stopWebserver(): Unit
    }

    private val sConn = object : WebserverServiceConnection {
        private var webserverServiceBinder: WebserverServiceBinder? = null

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            webserverServiceBinder = service as? WebserverServiceBinder
            serviceBound = webserverServiceBinder != null
            Log.i(LOG_TAG, "onServiceConnected(): webserverServiceBinder = $webserverServiceBinder")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i(LOG_TAG, "onServiceDisconnected()")
            serviceBound = false
        }

//        fun webserverHealthCheck(): Boolean = (serviceBound &&
//                (webserverServiceBinder?.isBinderAlive ?: false) &&
//                (webserverServiceBinder?.getService()?.isServerAlive() ?: false))
//            .also { Log.i(LOG_TAG, "webserverHealthCheck() = $it") }

        fun webserverHealthCheck(ownWebserverHealthChecker: OwnWebserverHealthChecker, callback: (Button?, Boolean) -> Unit): Unit {
            ownWebserverHealthChecker.webserverHealthCheck(callback)
        }

        override fun startWebserver() {
            webserverServiceBinder?.startWebserver()
        }

        override fun stopWebserver() {
            webserverServiceBinder?.stopWebserver()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if(intent?.action?.equals(Intent.ACTION_SEND) == true) {
            Log.i(LOG_TAG, "${this::class.simpleName}.onCreate(): $intent")
            useTextFromClipboard { Log.i(LOG_TAG, "${this::class.simpleName}.onCreate(): clipboard = $it") }
        }

        setContentView(R.layout.activity_main)

        if(checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), Random().nextInt())
        }

        webserverServiceIntent = Intent(this, KomptubeHttpService::class.java)
        bindService(webserverServiceIntent, sConn, BIND_AUTO_CREATE)

//        val serverModeSwitch = findViewById<Switch>(R.id.webserver_mode)
//        val linkEditText = findViewById<EditText>(R.id.edit_link)
//        val encodeLinkButton = findViewById<Button>(R.id.encode_link)
//        val shareLinkButton = findViewById<Button>(R.id.share_link)
//        val decodeLinkButton = findViewById<Button>(R.id.decode_link)
        serverModeSwitch = findViewById(R.id.webserver_mode)
        linkEditText = findViewById(R.id.edit_link)
        encodeLinkButton = findViewById(R.id.encode_link)
        shareLinkButton = findViewById(R.id.share_link)
        decodeLinkButton = findViewById(R.id.decode_link)
        clearLinkEditButton = findViewById(R.id.clear_text)
        webserverHealthCheckButton = findViewById(R.id.webserver_health_check)
        webserverHealthCheckResult = findViewById(R.id.webserver_health_check_result)

        val ownWebserverHealthChecker = OwnWebserverHealthChecker(shareLinkButton)

        serverModeSwitch.apply {
            sConn.webserverHealthCheck(ownWebserverHealthChecker) { _, boo -> isChecked = boo }
        }

        serverModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.i(LOG_TAG, "${this::class.simpleName}.serverModeSwitch(): $isChecked")
            if(isChecked) sConn.startWebserver() else sConn.stopWebserver()
        }

        useTextFromClipboard { linkEditText.setText(it) }

        encodeLinkButton.setOnClickListener { linkEditText.setText(URLEncoder.encode(linkEditText.text.toString(), Charsets.UTF_8)) }

        shareLinkButton.setOnClickListener { btn ->
            shareLinkToKomp(linkEditText.text.toString()) { threadBtn, isOk ->
                val lastSymbol = if(isOk) "√" else " "
                (btn as Button).text = threadBtn!!.text.dropLast(1).toString() + lastSymbol
            }
        }

        decodeLinkButton.setOnClickListener { linkEditText.setText(URLDecoder.decode(linkEditText.text.toString(), Charsets.UTF_8)) }

        clearLinkEditButton.setOnClickListener { linkEditText.setText("") }

        webserverHealthCheckButton.setOnClickListener {
            sConn.webserverHealthCheck(ownWebserverHealthChecker) { _, boo -> webserverHealthCheckResult.text = (if(boo) "" else "не ") + "работает" }
        }
    }

    private fun useTextFromClipboard(callback: (CharSequence) -> Unit): Unit = clipboard.primaryClip
        ?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)?.coerceToText(this)
        ?.also(callback)
        ?.let {} ?: Unit

    private fun shareLinkToKomp(link: String, callback: (Button?, Boolean) -> Unit = { _, _ ->}): Unit {
        Log.i(LOG_TAG, "${this::class.simpleName}.shareLinkToKomp(): link = $link")

        thread {
            val sharingLinkEncoded = URLEncoder.encode(link, Charsets.UTF_8)
            val request = Request.Builder()
                    .url("http://192.168.1.2:8765/?mode=SHARE_TO_KOMP&url=$sharingLinkEncoded")
                .build()
            okHttpClient.newCall(request).execute().use { callback(null, it.code == 200) }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.i(LOG_TAG, "${this::class.simpleName}.onNewIntent(): $intent")
        intent?.getStringExtra(Intent.EXTRA_TEXT)?.also {
            linkEditText.setText(it)
            shareLinkToKomp(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!serviceBound) return
        serviceBound = false
        unbindService(sConn)
    }
}
