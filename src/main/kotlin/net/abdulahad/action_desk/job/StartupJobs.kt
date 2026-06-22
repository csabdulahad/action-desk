package net.abdulahad.action_desk.job

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.data.AppValues
import net.abdulahad.action_desk.engine.action.ActionRunner
import net.abdulahad.action_desk.engine.adcd.AdcdDaemon
import net.abdulahad.action_desk.lib.tray.TrayMan
import net.abdulahad.action_desk.onIO
import net.abdulahad.action_desk.repo.action.ActionDao
import net.abdulahad.action_desk.view.tray.A2Tray
import java.io.File

object StartupJobs {
	
	fun runAutoStartActions() {
		onIO {
			/*
			 * Check if auto start actions setting is enabled
			 * */
			if (!AppConfig.getEnableAutoStartActions()) {
				App.logInfo("ActionDesk: auto start actions disabled")
				return@onIO
			}
			
			val actions = ActionDao.fetchAutoRunActions()
			
			if (actions.isEmpty()) return@onIO
			
			App.logInfo("ActionDesk: starting actions with AD")
			
			actions.forEach { action ->
				ActionRunner.runAction(action, diagnose = false, bootupRun = true)
			}
		}
	}
	
	fun validateADAutoStartLink() {
		onIO {
			App.logInfo("ActionDesk: validating AD auto start link")
			
			val autoStart = AppConfig.getAutoRun()
			val shortcut  = "${AppValues.START_UP_FOLDER}/ActionDesk.lnk"
			val exists    = File(shortcut).exists()
			
			if (!autoStart && exists) {
				AppConfig.applyAutoRun(false)
				return@onIO
			}
			
			if (autoStart && !exists) {
				AppConfig.applyAutoRun(true)
			}
		}
	}
	
	fun registerADHotkey() {
		val hotkey = AppConfig.getADHotkey()
		AppConfig.applyADHotKey(hotkey)
	}
	
	fun installTray() {
		TrayMan.install(A2Tray::class.java)
	}
	
	fun startAdcd() {
		if (!AppConfig.getAdcdEnabled()) {
			App.logInfo("ADCD: disabled")
			return
		}
		
		AdcdDaemon.start(AppConfig.getAdcdHost(), AppConfig.getAdcdPort())
	}
	
}