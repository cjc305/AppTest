package com.apptest.core.domain.inbox

import java.time.Instant

data class InboxNotification(
    val id: String,
    val type: InboxNotificationType,
    val title: String,
    val body: String,
    val timestamp: Instant,
    val isRead: Boolean,
    val deepLink: String?,
)

enum class InboxNotificationType {
    NewMatch, HeartbeatReminder, ReputationChange, Completion, System
}
