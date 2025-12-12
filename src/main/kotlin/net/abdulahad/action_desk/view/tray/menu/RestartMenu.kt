package net.abdulahad.action_desk.view.tray.menu

import dorkbox.systemTray.MenuItem
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.lib.tray.TrayItem
import kotlin.system.exitProcess

class RestartMenu: TrayItem(ID){
	
	companion object {
		const val ID: String = "action_desk_restart"
	}
	
	override fun onClick() {
		CommonActions.restartActionDesk()
	}
	
	override fun prepareMenu(): MenuItem = newMenuItem("Restart")
	
	override fun hasSeparatorAbove(): Boolean = true

}