package net.abdulahad.action_desk.engine.schedule

import net.abdulahad.action_desk.lib.json.Json
import net.abdulahad.action_desk.model.ActionSchedule
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ActionScheduleCalculator {
	
	private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
	
	fun nextRun(schedule: ActionSchedule, from: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
		if (!schedule.enabled) return null
		
		return when (schedule.type.lowercase()) {
			ActionSchedule.TYPE_REPEAT -> nextRepeatRun(schedule, from)
			ActionSchedule.TYPE_CALENDAR -> nextCalendarRun(schedule, from)
			else -> null
		}
	}
	
	private fun nextRepeatRun(schedule: ActionSchedule, from: LocalDateTime): LocalDateTime? {
		val minutes = schedule.repeatEveryMinutes ?: return null
		if (minutes <= 0) return null
		
		return from.withNano(0).plusMinutes(minutes.toLong())
	}
	
	private fun nextCalendarRun(schedule: ActionSchedule, from: LocalDateTime): LocalDateTime? {
		return when (schedule.dayRuleType?.lowercase()) {
			ActionSchedule.DAY_RULE_DAILY -> nextDailyRun(schedule, from)
			ActionSchedule.DAY_RULE_WEEKLY -> nextWeeklyRun(schedule, from)
			else -> null
		}
	}
	
	private fun nextDailyRun(schedule: ActionSchedule, from: LocalDateTime): LocalDateTime? {
		val times = parseTimes(schedule.timesJson)
		if (times.isEmpty()) return null
		
		val today = from.toLocalDate()
		val fromClean = from.withNano(0)
		
		for (time in times) {
			val candidate = LocalDateTime.of(today, time)
			if (candidate.isAfter(fromClean)) {
				return candidate
			}
		}
		
		return LocalDateTime.of(today.plusDays(1), times.first())
	}
	
	private fun nextWeeklyRun(schedule: ActionSchedule, from: LocalDateTime): LocalDateTime? {
		val days = parseDaysOfWeek(schedule.daysOfWeekJson)
		val sharedTimes = parseTimes(schedule.timesJson)
		val customTimesByDay = parseDayTimes(schedule.dayTimesJson)
		
		if (days.isEmpty()) return null
		if (sharedTimes.isEmpty() && days.none { customTimesByDay[it].orEmpty().isNotEmpty() }) return null
		
		val today = from.toLocalDate()
		val fromClean = from.withNano(0)
		
		for (offset in 0..13) {
			val date = today.plusDays(offset.toLong())
			val dayOfWeek = date.dayOfWeek
			
			if (!days.contains(dayOfWeek)) {
				continue
			}
			
			val dayTimes = timesForDay(dayOfWeek, sharedTimes, customTimesByDay)
			
			for (time in dayTimes) {
				val candidate = LocalDateTime.of(date, time)
				
				if (candidate.isAfter(fromClean)) {
					return candidate
				}
			}
		}
		
		return null
	}
	
	private fun timesForDay(
		dayOfWeek: DayOfWeek,
		sharedTimes: List<LocalTime>,
		customTimesByDay: Map<DayOfWeek, List<LocalTime>>
	): List<LocalTime> {
		return (sharedTimes + customTimesByDay[dayOfWeek].orEmpty())
			.distinct()
			.sorted()
	}
	
	private fun parseTimes(timesJson: String?): List<LocalTime> {
		if (timesJson.isNullOrBlank()) return emptyList()
		
		return runCatching {
			Json.parse(timesJson)
				.stringList()
				.mapNotNull { parseTime(it) }
				.distinct()
				.sorted()
		}.getOrDefault(emptyList())
	}
	
	private fun parseDayTimes(dayTimesJson: String?): Map<DayOfWeek, List<LocalTime>> {
		if (dayTimesJson.isNullOrBlank()) return emptyMap()
		
		return runCatching {
			Json.parse(dayTimesJson)
				.map()
				.mapNotNull { (day, value) ->
					val dayOfWeek = parseDayOfWeek(day) ?: return@mapNotNull null
					val times = value
						.stringList()
						.mapNotNull { parseTime(it) }
						.distinct()
						.sorted()
					
					if (times.isEmpty()) null else dayOfWeek to times
				}
				.toMap()
		}.getOrDefault(emptyMap())
	}
	
	private fun parseTime(value: String?): LocalTime? {
		if (value.isNullOrBlank()) return null
		
		val text = value.trim()
		
		return runCatching { LocalTime.parse(text) }.getOrNull()
			?: runCatching { LocalTime.parse(text, timeFormatter) }.getOrNull()
	}
	
	private fun parseDaysOfWeek(daysJson: String?): Set<DayOfWeek> {
		if (daysJson.isNullOrBlank()) return emptySet()
		
		return runCatching {
			Json.parse(daysJson)
				.stringList()
				.mapNotNull { parseDayOfWeek(it) }
				.toSet()
		}.getOrDefault(emptySet())
	}
	
	private fun parseDayOfWeek(value: String?): DayOfWeek? {
		if (value.isNullOrBlank()) return null
		
		return when (value.trim().uppercase()) {
			"MON", "MONDAY" -> DayOfWeek.MONDAY
			"TUE", "TUESDAY" -> DayOfWeek.TUESDAY
			"WED", "WEDNESDAY" -> DayOfWeek.WEDNESDAY
			"THU", "THURSDAY" -> DayOfWeek.THURSDAY
			"FRI", "FRIDAY" -> DayOfWeek.FRIDAY
			"SAT", "SATURDAY" -> DayOfWeek.SATURDAY
			"SUN", "SUNDAY" -> DayOfWeek.SUNDAY
			else -> null
		}
	}
	
}
