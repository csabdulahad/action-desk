package net.abdulahad.action_desk.engine.schedule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.engine.action.ActionRunner
import net.abdulahad.action_desk.repo.action.ActionDao
import net.abdulahad.action_desk.repo.action_schedule.ActionScheduleDao
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

object ActionScheduleService {
	
	private const val TICK_SECONDS = 30
	
	private var job: Job? = null
	
	fun start() {
		if (job?.isActive == true) return
		
		job = CoroutineScope(Dispatchers.IO).launch {
			App.logInfo("ActionSchedule: service started")
			repairMissingNextRunTimes()
			
			while (isActive) {
				runDueSchedules()
				delay(TICK_SECONDS.seconds)
			}
		}
	}
	
	fun stop() {
		job?.cancel()
		job = null
		App.logInfo("ActionSchedule: service stopped")
	}
	
	private fun repairMissingNextRunTimes() {
		try {
			val now = LocalDateTime.now()
			val schedules = ActionScheduleDao.fetchEnabledWithoutNextRun()
			
			schedules.forEach { schedule ->
				val nextRunAt = ActionScheduleCalculator.nextRun(schedule, now)
				ActionScheduleDao.updateNextRunAt(schedule.id, nextRunAt)
				
				if (nextRunAt == null) {
					App.logWarn("ActionSchedule: could not calculate next_run_at for schedule ${schedule.id}")
				}
			}
		} catch (e: Exception) {
			App.logErr("ActionSchedule: repair failed - ${e.message}")
		}
	}
	
	private fun runDueSchedules() {
		try {
			val now = LocalDateTime.now().withNano(0)
			val schedules = ActionScheduleDao.fetchDue(now)
			
			if (schedules.isEmpty()) return
			
			schedules.forEach { schedule ->
				try {
					val action = ActionDao.fetchById(schedule.actionId)
					
					if (action == null || !action.enabled) {
						val nextRunAt = ActionScheduleCalculator.nextRun(schedule, now)
						ActionScheduleDao.updateNextRunAt(schedule.id, nextRunAt)
						return@forEach
					}
					
					App.logInfo("ActionSchedule: running '${action.name}'")
					ActionRunner.runAction(action, diagnose = false, automaticRun = true)
					
					val nextRunAt = ActionScheduleCalculator.nextRun(schedule, now)
					ActionScheduleDao.markRan(schedule.id, now, nextRunAt)
					
					if (nextRunAt == null) {
						App.logWarn("ActionSchedule: no next run calculated for schedule ${schedule.id}; it will not run again until updated")
					}
				} catch (e: Exception) {
					App.logErr("ActionSchedule: failed to run schedule ${schedule.id} - ${e.message}")
				}
			}
		} catch (e: Exception) {
			App.logErr("ActionSchedule: tick failed - ${e.message}")
		}
	}
}
