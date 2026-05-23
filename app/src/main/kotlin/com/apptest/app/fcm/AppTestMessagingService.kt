package com.apptest.app.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.apptest.app.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.atomic.AtomicInteger

/**
 * R-042 — Firebase Cloud Messaging integration.
 *
 * MED-007/008 (audit 2026-05-23):
 *  - Notification channel is created once in [ensureChannel] (idempotent; still safe to call
 *    repeatedly, but the heavy [createNotificationChannel] is skipped once [channelCreated] is
 *    set). Application.onCreate creates it proactively via [ensureChannel].
 *  - [notifId] is a process-wide AtomicInteger so each message gets a unique notification ID
 *    and a unique PendingIntent requestCode — prevents tap on older notification overriding a
 *    newer one's Intent.
 */
@AndroidEntryPoint
class AppTestMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        // TODO V2: POST token to Ktor backend /v1/devices/token (requires auth)
    }

    private fun showNotification(title: String, body: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(nm)
        val id = notifId.incrementAndGet()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this, id, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
        )
        nm.notify(id, NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title).setContentText(body)
            .setAutoCancel(true).setContentIntent(pending).build())
    }

    companion object {
        const val CHANNEL_ID = "apptest_push"
        private val notifId = AtomicInteger(0)
        private var channelCreated = false

        /** Idempotent; safe to call from Application.onCreate before any message arrives. */
        fun ensureChannel(nm: NotificationManager) {
            if (channelCreated) return
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "AppTest 通知", NotificationManager.IMPORTANCE_DEFAULT)
            )
            channelCreated = true
        }
    }
}
