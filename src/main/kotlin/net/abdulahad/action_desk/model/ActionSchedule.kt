package net.abdulahad.action_desk.model

import java.time.LocalDateTime

/**
 * One optional schedule for one Action.
 *
 * v1 supports:
 * - type = repeat, repeatEveryMinutes > 0
 * - type = calendar, dayRuleType = daily, times = ["HH:mm"]
 *
 * v2 supports:
 * - type = calendar, dayRuleType = weekly, daysOfWeekJson = ["MON"], times = ["HH:mm"]
 *
 * The extra nullable fields are intentionally kept for future weekly/monthly rules.
 */
data class ActionSchedule(
	var id: Int = 0,
	var actionId: Int = 0,
	var enabled: Boolean = false,
	var type: String = TYPE_REPEAT,
	var repeatEveryMinutes: Int? = null,
	var dayRuleType: String? = null,
	var daysOfWeekJson: String? = null,
	var daysOfMonthJson: String? = null,
	var weekPosition: String? = null,
	var weekday: String? = null,
	var timesJson: String? = null,
	var dayTimesJson: String? = null,
	var lastRunAt: LocalDateTime? = null,
	var nextRunAt: LocalDateTime? = null,
	var createdAt: LocalDateTime = LocalDateTime.now(),
	var updatedAt: LocalDateTime = LocalDateTime.now()
) {
	
	companion object {
		const val TYPE_REPEAT = "repeat"
		const val TYPE_CALENDAR = "calendar"
		
		const val DAY_RULE_DAILY = "daily"
		const val DAY_RULE_WEEKLY = "weekly"
		const val DAY_RULE_MONTHLY_DATE = "monthly_date"
		const val DAY_RULE_MONTHLY_WEEKDAY = "monthly_weekday"
	}
	
}
