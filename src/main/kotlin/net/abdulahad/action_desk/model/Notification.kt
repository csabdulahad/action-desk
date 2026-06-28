package net.abdulahad.action_desk.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Notification(
	val icon: String,
	val msg: String,
	val isSilent: Boolean,
	val at: LocalDateTime = LocalDateTime.now()
) {
	
	fun time(): String {
		return at.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
	}
	
	fun date(): String {
		return at.format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH))
	}
	
}
