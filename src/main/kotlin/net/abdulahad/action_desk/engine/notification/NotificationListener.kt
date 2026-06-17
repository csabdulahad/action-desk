package net.abdulahad.action_desk.engine.notification

import net.abdulahad.action_desk.model.Notification

interface NotificationListener {
	fun onNewNotification(notification: Notification) {}
	fun onNotificationClear() {}
	fun onNotificationsAcknowledged() {}
}
