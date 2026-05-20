package com.apptest.feature.inbox.domain.model

import java.time.Instant

enum class InboxNotificationType { NewMatch, HeartbeatReminder, ReputationChange, Completion, System }

data class InboxNotification(
    val id: String,
    val type: InboxNotificationType,
    val title: String,
    val body: String,
    val timestamp: Instant,
    val isRead: Boolean,
    /** deep-link 用，e.g. apptest://app/{id} — null = 純資訊不可點 */
    val deepLink: String? = null,
)
