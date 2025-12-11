package net.abdulahad.action_desk.helper

import net.abdulahad.action_desk.data.AppValues
import java.io.File

object AutoRun {
	
	fun isAutoRestarting(): Boolean {
		val lnk = AppValues.START_UP_FOLDER + "/ActionDesk.lnk"
		return File(lnk).exists()
	}
	
	fun disableAutoRestart() {
		if (!isAutoRestarting()) {
			return
		}
		
		val lnk = AppValues.START_UP_FOLDER + "/ActionDesk.lnk"
		val file = File(lnk)
		
		file.delete()
	}
	
	fun enableAutoRestart() {
		CommonActions.actionDeskShortcut(AppValues.START_UP_FOLDER)
	}
	
}