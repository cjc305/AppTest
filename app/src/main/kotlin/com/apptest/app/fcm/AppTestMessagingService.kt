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

/**
 * R-042 — Firebase Cloud Messaging integration.
 *
 * Handles:
 *  - [onMessageReceived]: display system notification for FCM push (used by matching and
 *    fraud-alert notifications when the app is in background / killed).
 *  - [onNewToken]: store the refreshed token so the Ktor backend can send targeted pushes.
 *    Token upload to backend is deferred to V2 (requires authenticated endpoint).
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

    // ── private ──────────────────────────────────────────────────────────────

    private fun showNotification(title: String, body: String) {
        val channelId = CHANNEL_ID
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(channelId, "AppTest 通知", NotificationManager.IMPORTANCE_DEFAULT),
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "apptest_push"
    }
}
