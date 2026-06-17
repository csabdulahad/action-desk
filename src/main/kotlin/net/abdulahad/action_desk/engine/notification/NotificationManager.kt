package net.abdulahad.action_desk.engine.notification

import net.abdulahad.action_desk.model.Notification
import java.time.LocalDateTime
import kotlin.random.Random

object NotificationManager {
	private val items = mutableListOf<Notification>()
	private val listeners = mutableListOf<NotificationListener>()
	
	private fun getIcon(type: String): String {
		return when (type) {
			"success" -> "success"
			"error" -> "error"
			"info" -> "info"
			else -> "warn"
		}
	}
	
	private fun add(notification: Notification) {
		items.add(notification)
		listeners.forEach { it.onNewNotification(notification) }
	}
	
	fun listen(listener: NotificationListener) {
		listeners.add(listener)
	}
	
	fun removeListener(listener: NotificationListener) {
		listeners.remove(listener)
	}
	
	fun success(msg: String, isSilent: Boolean = true, at: LocalDateTime = LocalDateTime.now()) {
		add(Notification(getIcon("success"), msg, isSilent, at))
	}
	
	fun error(msg: String, isSilent: Boolean = true, at: LocalDateTime = LocalDateTime.now()) {
		add(Notification(getIcon("error"), msg, isSilent, at))
	}
	
	fun info(msg: String, isSilent: Boolean = true, at: LocalDateTime = LocalDateTime.now()) {
		add(Notification(getIcon("info"), msg, isSilent, at))
	}
	
	fun warn(msg: String, isSilent: Boolean = true, at: LocalDateTime = LocalDateTime.now()) {
		add(Notification(getIcon("warn"), msg, isSilent, at))
	}
	
	fun list(): List<Notification> {
		return items
	}
	
	fun clear() {
		items.clear()
		listeners.forEach { it.onNotificationClear() }
	}
	
	fun acknowledged() {
		listeners.forEach { it.onNotificationsAcknowledged() }
	}
	
	fun dump(count: Int = 15) {
		val messages = listOf(
			"Action executed successfully.",
			"Configuration file was updated and reloaded without requiring an application restart.",
			"Folder doesn't exist: C:/Users/ahad/AppData/Local/ActionDesk/logs/Explorer_restart",
			"Backup completed,but one or more files were skipped due to insufficient permissions.",
			"Connection to the background service was temporarily lost and has now been restored.",
			"Failed to execute command. The process exited with a non-zero status code.",
			"Update available: A newer version of Action Desk is ready to download and install.",
			"Scheduled task started at 08:30 PM and is currently running in the background.",
			"Environment variable PATH was modified. Restart the application for changes to take effect.",
			"Action was blocked because another instance of the same task is already running."
		)
		
		repeat(count) {
			val msg = messages[Random.nextInt(0, messages.size)]
			when (Random.nextInt(0, 4)) {
				0 -> success(msg, isSilent = false)
				1 -> warn(msg, isSilent = false)
				2 -> error(msg, isSilent = false)
				else -> info(msg, isSilent = false)
			}
		}
	}
}
