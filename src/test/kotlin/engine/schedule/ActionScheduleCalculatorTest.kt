package net.abdulahad.action_desk.engine.schedule

import net.abdulahad.action_desk.model.ActionSchedule
import java.time.LocalDateTime

/**
 * Dependency-free calculator tests for scheduled actions.
 *
 * Run from the IDE using the main() method, or copy this file into your test source set.
 * These tests do not touch the DB, UI, system clock, or ActionRunner.
 */
object ActionScheduleCalculatorTest {
	
	@JvmStatic
	fun main(args: Array<String>) {
		testDailyMultipleTimes()
		testWeeklySharedTimes()
		testWeeklyCustomOnlyTimes()
		testWeeklySharedAndCustomTimes()
		testNoNextRunCases()
		
		println("ActionScheduleCalculatorTest: all tests passed")
	}
	
	private fun testDailyMultipleTimes() {
		val schedule = calendar(
			dayRuleType = ActionSchedule.DAY_RULE_DAILY,
			timesJson = """["08:30","14:00","18:00"]"""
		)
		
		assertNext(
			"daily before first time",
			schedule,
			from = "2026-06-22T08:00:00",
			expected = "2026-06-22T08:30:00"
		)
		
		assertNext(
			"daily between times",
			schedule,
			from = "2026-06-22T13:00:00",
			expected = "2026-06-22T14:00:00"
		)
		
		assertNext(
			"daily before last time",
			schedule,
			from = "2026-06-22T17:59:00",
			expected = "2026-06-22T18:00:00"
		)
		
		assertNext(
			"daily after last time wraps to tomorrow",
			schedule,
			from = "2026-06-22T18:00:00",
			expected = "2026-06-23T08:30:00"
		)
	}
	
	private fun testWeeklySharedTimes() {
		val schedule = calendar(
			dayRuleType = ActionSchedule.DAY_RULE_WEEKLY,
			daysOfWeekJson = """["MON","WED","FRI"]""",
			timesJson = """["09:00","14:00"]"""
		)
		
		assertNext(
			"weekly shared Monday before first slot",
			schedule,
			from = "2026-06-22T08:00:00",
			expected = "2026-06-22T09:00:00"
		)
		
		assertNext(
			"weekly shared Monday between slots",
			schedule,
			from = "2026-06-22T10:00:00",
			expected = "2026-06-22T14:00:00"
		)
		
		assertNext(
			"weekly shared Monday after last slot",
			schedule,
			from = "2026-06-22T15:00:00",
			expected = "2026-06-24T09:00:00"
		)
		
		assertNext(
			"weekly shared Wednesday after last slot",
			schedule,
			from = "2026-06-24T15:00:00",
			expected = "2026-06-26T09:00:00"
		)
		
		assertNext(
			"weekly shared Friday after last slot wraps to next Monday",
			schedule,
			from = "2026-06-26T15:00:00",
			expected = "2026-06-29T09:00:00"
		)
		
		assertNext(
			"weekly shared Sunday wraps to next Monday",
			schedule,
			from = "2026-06-28T10:00:00",
			expected = "2026-06-29T09:00:00"
		)
	}
	
	private fun testWeeklyCustomOnlyTimes() {
		val schedule = calendar(
			dayRuleType = ActionSchedule.DAY_RULE_WEEKLY,
			daysOfWeekJson = """["MON","FRI"]""",
			timesJson = null,
			dayTimesJson = """{"MON":["13:00"],"FRI":["17:30"]}"""
		)
		
		assertNext(
			"weekly custom-only Monday before custom slot",
			schedule,
			from = "2026-06-22T12:00:00",
			expected = "2026-06-22T13:00:00"
		)
		
		assertNext(
			"weekly custom-only Monday after custom slot",
			schedule,
			from = "2026-06-22T14:00:00",
			expected = "2026-06-26T17:30:00"
		)
		
		assertNext(
			"weekly custom-only Friday after custom slot wraps to Monday",
			schedule,
			from = "2026-06-26T18:00:00",
			expected = "2026-06-29T13:00:00"
		)
	}
	
	private fun testWeeklySharedAndCustomTimes() {
		val schedule = calendar(
			dayRuleType = ActionSchedule.DAY_RULE_WEEKLY,
			daysOfWeekJson = """["MON","WED","FRI"]""",
			timesJson = """["09:00"]""",
			dayTimesJson = """{"MON":["13:00"],"FRI":["17:30"]}"""
		)
		
		assertNext(
			"weekly shared+custom Monday before shared slot",
			schedule,
			from = "2026-06-22T08:00:00",
			expected = "2026-06-22T09:00:00"
		)
		
		assertNext(
			"weekly shared+custom Monday between shared and custom slot",
			schedule,
			from = "2026-06-22T10:00:00",
			expected = "2026-06-22T13:00:00"
		)
		
		assertNext(
			"weekly shared+custom Monday after custom slot",
			schedule,
			from = "2026-06-22T14:00:00",
			expected = "2026-06-24T09:00:00"
		)
		
		assertNext(
			"weekly shared+custom Friday before custom slot",
			schedule,
			from = "2026-06-26T10:00:00",
			expected = "2026-06-26T17:30:00"
		)
		
		assertNext(
			"weekly shared+custom Friday after custom slot wraps to Monday shared slot",
			schedule,
			from = "2026-06-26T18:00:00",
			expected = "2026-06-29T09:00:00"
		)
	}
	
	private fun testNoNextRunCases() {
		assertNoNext(
			"disabled schedule has no next run",
			calendar(
				dayRuleType = ActionSchedule.DAY_RULE_DAILY,
				timesJson = """["08:30"]""",
				enabled = false
			),
			from = "2026-06-22T08:00:00"
		)
		
		assertNoNext(
			"weekly selected days without shared or custom times has no next run",
			calendar(
				dayRuleType = ActionSchedule.DAY_RULE_WEEKLY,
				daysOfWeekJson = """["MON","WED"]""",
				timesJson = null,
				dayTimesJson = null
			),
			from = "2026-06-22T08:00:00"
		)
		
		assertNoNext(
			"weekly custom time for unselected day is ignored",
			calendar(
				dayRuleType = ActionSchedule.DAY_RULE_WEEKLY,
				daysOfWeekJson = """["WED"]""",
				timesJson = null,
				dayTimesJson = """{"MON":["13:00"]}"""
			),
			from = "2026-06-22T08:00:00"
		)
	}
	
	private fun calendar(
		dayRuleType: String,
		daysOfWeekJson: String? = null,
		timesJson: String? = null,
		dayTimesJson: String? = null,
		enabled: Boolean = true
	): ActionSchedule {
		return ActionSchedule(
			enabled = enabled,
			type = ActionSchedule.TYPE_CALENDAR,
			dayRuleType = dayRuleType,
			daysOfWeekJson = daysOfWeekJson,
			timesJson = timesJson,
			dayTimesJson = dayTimesJson
		)
	}
	
	private fun assertNext(name: String, schedule: ActionSchedule, from: String, expected: String) {
		val actual = ActionScheduleCalculator.nextRun(schedule, dt(from))
		val expectedDateTime = dt(expected)
		
		check(actual == expectedDateTime) {
			"$name failed: expected $expectedDateTime but got $actual"
		}
		
		println("PASS: $name -> $actual")
	}
	
	private fun assertNoNext(name: String, schedule: ActionSchedule, from: String) {
		val actual = ActionScheduleCalculator.nextRun(schedule, dt(from))
		
		check(actual == null) {
			"$name failed: expected null but got $actual"
		}
		
		println("PASS: $name -> null")
	}
	
	private fun dt(value: String): LocalDateTime {
		return LocalDateTime.parse(value)
	}
	
}
