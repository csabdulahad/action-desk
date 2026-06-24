package net.abdulahad.action_desk.repo.action_schedule

import net.abdulahad.action_desk.data.DB
import net.abdulahad.action_desk.engine.schedule.ActionScheduleCalculator
import net.abdulahad.action_desk.engine.schedule.ActionScheduleTime
import net.abdulahad.action_desk.jooq.Tables.ACTION
import net.abdulahad.action_desk.jooq.Tables.ACTION_SCHEDULE
import net.abdulahad.action_desk.model.ActionSchedule
import java.time.LocalDateTime

object ActionScheduleDao {
	
	fun fetchById(id: Int): ActionSchedule? {
		return DB.DSL
			.selectFrom(ACTION_SCHEDULE)
			.where(ACTION_SCHEDULE.ID.eq(id))
			.fetchOne()
			?.let { record ->
				ActionSchedule(
					id = record.get(ACTION_SCHEDULE.ID),
					actionId = record.get(ACTION_SCHEDULE.ACTION_ID),
					enabled = record.get(ACTION_SCHEDULE.ENABLED) == 1,
					type = record.get(ACTION_SCHEDULE.TYPE) ?: ActionSchedule.TYPE_REPEAT,
					repeatEveryMinutes = record.get(ACTION_SCHEDULE.REPEAT_EVERY_MINUTES),
					dayRuleType = record.get(ACTION_SCHEDULE.DAY_RULE_TYPE),
					daysOfWeekJson = record.get(ACTION_SCHEDULE.DAYS_OF_WEEK_JSON),
					daysOfMonthJson = record.get(ACTION_SCHEDULE.DAYS_OF_MONTH_JSON),
					weekPosition = record.get(ACTION_SCHEDULE.WEEK_POSITION),
					weekday = record.get(ACTION_SCHEDULE.WEEKDAY),
					timesJson = record.get(ACTION_SCHEDULE.TIMES_JSON),
					dayTimesJson = record.get(ACTION_SCHEDULE.DAY_TIMES_JSON),
					lastRunAt = ActionScheduleTime.parse(record.get(ACTION_SCHEDULE.LAST_RUN_AT)),
					nextRunAt = ActionScheduleTime.parse(record.get(ACTION_SCHEDULE.NEXT_RUN_AT)),
					createdAt = ActionScheduleTime.parse(record.get(ACTION_SCHEDULE.CREATED_AT)) ?: LocalDateTime.now(),
					updatedAt = ActionScheduleTime.parse(record.get(ACTION_SCHEDULE.UPDATED_AT)) ?: LocalDateTime.now(),
				)
			}
	}
	
	fun fetchByActionId(actionId: Int): ActionSchedule? {
		return DB.DSL
			.select(ACTION_SCHEDULE.ID)
			.from(ACTION_SCHEDULE)
			.where(ACTION_SCHEDULE.ACTION_ID.eq(actionId))
			.fetchOne(ACTION_SCHEDULE.ID)
			?.let { id -> fetchById(id) }
	}
	
	fun fetchDue(now: LocalDateTime): List<ActionSchedule> {
		val nowText = ActionScheduleTime.stringify(now) ?: return emptyList()
		
		return DB.DSL
			.select(ACTION_SCHEDULE.ID)
			.from(ACTION_SCHEDULE)
			.join(ACTION).on(ACTION.ID.eq(ACTION_SCHEDULE.ACTION_ID))
			.where(ACTION_SCHEDULE.ENABLED.eq(1))
			.and(ACTION.ENABLED.eq(1))
			.and(ACTION_SCHEDULE.NEXT_RUN_AT.isNotNull())
			.and(ACTION_SCHEDULE.NEXT_RUN_AT.le(nowText))
			.orderBy(ACTION_SCHEDULE.NEXT_RUN_AT.asc())
			.fetch { record -> record.get(ACTION_SCHEDULE.ID) }
			.mapNotNull { id -> fetchById(id) }
	}
	
	fun fetchEnabledWithoutNextRun(): List<ActionSchedule> {
		return DB.DSL
			.select(ACTION_SCHEDULE.ID)
			.from(ACTION_SCHEDULE)
			.join(ACTION).on(ACTION.ID.eq(ACTION_SCHEDULE.ACTION_ID))
			.where(ACTION_SCHEDULE.ENABLED.eq(1))
			.and(ACTION.ENABLED.eq(1))
			.and(ACTION_SCHEDULE.NEXT_RUN_AT.isNull())
			.fetch { record -> record.get(ACTION_SCHEDULE.ID) }
			.mapNotNull { id -> fetchById(id) }
	}
	
	fun insert(schedule: ActionSchedule): Int {
		val now = LocalDateTime.now()
		schedule.createdAt = now
		schedule.updatedAt = now
		
		val result = DB.DSL
			.insertInto(ACTION_SCHEDULE)
			.set(ACTION_SCHEDULE.ACTION_ID, schedule.actionId)
			.set(ACTION_SCHEDULE.ENABLED, if (schedule.enabled) 1 else 0)
			.set(ACTION_SCHEDULE.TYPE, schedule.type)
			.set(ACTION_SCHEDULE.REPEAT_EVERY_MINUTES, schedule.repeatEveryMinutes)
			.set(ACTION_SCHEDULE.DAY_RULE_TYPE, schedule.dayRuleType)
			.set(ACTION_SCHEDULE.DAYS_OF_WEEK_JSON, schedule.daysOfWeekJson)
			.set(ACTION_SCHEDULE.DAYS_OF_MONTH_JSON, schedule.daysOfMonthJson)
			.set(ACTION_SCHEDULE.WEEK_POSITION, schedule.weekPosition)
			.set(ACTION_SCHEDULE.WEEKDAY, schedule.weekday)
			.set(ACTION_SCHEDULE.TIMES_JSON, schedule.timesJson)
			.set(ACTION_SCHEDULE.DAY_TIMES_JSON, schedule.dayTimesJson)
			.set(ACTION_SCHEDULE.LAST_RUN_AT, ActionScheduleTime.stringify(schedule.lastRunAt))
			.set(ACTION_SCHEDULE.NEXT_RUN_AT, ActionScheduleTime.stringify(schedule.nextRunAt))
			.set(ACTION_SCHEDULE.CREATED_AT, ActionScheduleTime.stringify(schedule.createdAt))
			.set(ACTION_SCHEDULE.UPDATED_AT, ActionScheduleTime.stringify(schedule.updatedAt))
			.returning(ACTION_SCHEDULE.ID)
			.fetchOne()
		
		return result?.get(ACTION_SCHEDULE.ID) ?: -1
	}
	
	fun update(schedule: ActionSchedule): Int {
		schedule.updatedAt = LocalDateTime.now()
		
		DB.DSL
			.update(ACTION_SCHEDULE)
			.set(ACTION_SCHEDULE.ACTION_ID, schedule.actionId)
			.set(ACTION_SCHEDULE.ENABLED, if (schedule.enabled) 1 else 0)
			.set(ACTION_SCHEDULE.TYPE, schedule.type)
			.set(ACTION_SCHEDULE.REPEAT_EVERY_MINUTES, schedule.repeatEveryMinutes)
			.set(ACTION_SCHEDULE.DAY_RULE_TYPE, schedule.dayRuleType)
			.set(ACTION_SCHEDULE.DAYS_OF_WEEK_JSON, schedule.daysOfWeekJson)
			.set(ACTION_SCHEDULE.DAYS_OF_MONTH_JSON, schedule.daysOfMonthJson)
			.set(ACTION_SCHEDULE.WEEK_POSITION, schedule.weekPosition)
			.set(ACTION_SCHEDULE.WEEKDAY, schedule.weekday)
			.set(ACTION_SCHEDULE.TIMES_JSON, schedule.timesJson)
			.set(ACTION_SCHEDULE.DAY_TIMES_JSON, schedule.dayTimesJson)
			.set(ACTION_SCHEDULE.LAST_RUN_AT, ActionScheduleTime.stringify(schedule.lastRunAt))
			.set(ACTION_SCHEDULE.NEXT_RUN_AT, ActionScheduleTime.stringify(schedule.nextRunAt))
			.set(ACTION_SCHEDULE.UPDATED_AT, ActionScheduleTime.stringify(schedule.updatedAt))
			.where(ACTION_SCHEDULE.ID.eq(schedule.id))
			.execute()
		
		return schedule.id
	}
	
	fun save(schedule: ActionSchedule, recalculateNextRunAt: Boolean = true): Int {
		val existing = if (schedule.id > 0) fetchById(schedule.id) else fetchByActionId(schedule.actionId)
		
		if (recalculateNextRunAt) {
			schedule.nextRunAt = ActionScheduleCalculator.nextRun(schedule, LocalDateTime.now())
		}
		
		return if (existing == null) {
			insert(schedule)
		} else {
			schedule.id = existing.id
			update(schedule)
		}
	}
	
	fun markRan(id: Int, lastRunAt: LocalDateTime, nextRunAt: LocalDateTime?) {
		DB.DSL
			.update(ACTION_SCHEDULE)
			.set(ACTION_SCHEDULE.LAST_RUN_AT, ActionScheduleTime.stringify(lastRunAt))
			.set(ACTION_SCHEDULE.NEXT_RUN_AT, ActionScheduleTime.stringify(nextRunAt))
			.set(ACTION_SCHEDULE.UPDATED_AT, ActionScheduleTime.stringify(LocalDateTime.now()))
			.where(ACTION_SCHEDULE.ID.eq(id))
			.execute()
	}
	
	fun updateNextRunAt(id: Int, nextRunAt: LocalDateTime?) {
		DB.DSL
			.update(ACTION_SCHEDULE)
			.set(ACTION_SCHEDULE.NEXT_RUN_AT, ActionScheduleTime.stringify(nextRunAt))
			.set(ACTION_SCHEDULE.UPDATED_AT, ActionScheduleTime.stringify(LocalDateTime.now()))
			.where(ACTION_SCHEDULE.ID.eq(id))
			.execute()
	}
	
	fun deleteByActionId(actionId: Int) {
		DB.DSL
			.deleteFrom(ACTION_SCHEDULE)
			.where(ACTION_SCHEDULE.ACTION_ID.eq(actionId))
			.execute()
	}
	
}
