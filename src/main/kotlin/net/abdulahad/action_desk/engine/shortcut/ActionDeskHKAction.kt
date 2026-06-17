package net.abdulahad.action_desk.engine.shortcut

import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.onUI
import net.abdulahad.action_desk.view.ActionDesk

object ActionDeskHKAction {
	
	const val ID 	= 999999
	const val NAME  = "_ACTION_DESK_HOTKEY_ACTION"
	
	var globalKey   = AppConfig.getADHotkey()
	
	fun run() {
		onUI {
			ActionDesk.showFrame()
		}
	}
	
}
