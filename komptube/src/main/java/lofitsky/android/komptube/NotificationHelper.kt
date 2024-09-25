package lofitsky.android.komptube

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

class NotificationHelper {
    companion object {
        fun build(context: Context, title: String, text: String, contentIntent: PendingIntent?): Notification {
            val notificationManager = NotificationManagerCompat.from(context)

            val notificationChannel = NotificationChannelCompat.Builder("Komptube_channel", NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setDescription("Komptube_channel")
                    .setName("Komptube_channel")
                .build()
            notificationManager.createNotificationChannel(notificationChannel)

            return NotificationCompat.Builder(context, notificationChannel.id)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                .build()
        }

        fun notify(context: Context, title: String, text: String, contentIntent: PendingIntent?): Unit {
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Не включены уведомления!", Toast.LENGTH_LONG).show()
                return
            }

            val notificationManager = NotificationManagerCompat.from(context)
            val notificationChannel = NotificationChannelCompat.Builder("Komptube_channel", NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setDescription("Komptube_channel")
                    .setName("Komptube_channel")
                .build()
            notificationManager.createNotificationChannel(notificationChannel)
            val notification = NotificationCompat.Builder(context, notificationChannel.id)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                .build()

            notificationManager.notify(Random.nextInt(), notification)
        }
    }
}
