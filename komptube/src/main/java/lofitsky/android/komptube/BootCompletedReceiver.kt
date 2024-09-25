package lofitsky.android.komptube

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import lofitsky.android.komptube.KomptubeHttpService.Companion.KOMPTUBE_BG_SERVICE_ACTION_START

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action?.equals(Intent.ACTION_BOOT_COMPLETED) == true) {
            Intent(context, KomptubeHttpService::class.java).apply {
                action = KOMPTUBE_BG_SERVICE_ACTION_START
            }.also {
                context.startForegroundService(it)
            }
        }
    }
}
