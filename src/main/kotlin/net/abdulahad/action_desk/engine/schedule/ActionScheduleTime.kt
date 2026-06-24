package net.abdulahad.action_desk.engine.schedule

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ActionScheduleTime {
	
	private val compactFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
	private val compactSecondsFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
	
	fun parse(value: String?): LocalDateTime? {
		if (value.isNullOrBlank()) return null
		
		val text = value.trim()
		
		return runCatching { LocalDateTime.parse(text) }.getOrNull()
			?: runCatching { LocalDateTime.parse(text, compactFormatter) }.getOrNull()
			?: runCatching { LocalDateTime.parse(text, compactSecondsFormatter) }.getOrNull()
	}
	
	fun stringify(value: LocalDateTime?): String? {
		return value?.withNano(0)?.toString()
	}
	
}
