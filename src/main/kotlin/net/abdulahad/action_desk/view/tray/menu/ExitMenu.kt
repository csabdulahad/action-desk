package net.abdulahad.action_desk.view.tray.menu

import dorkbox.systemTray.MenuItem
import net.abdulahad.action_desk.lib.tray.TrayItem
import kotlin.system.exitProcess

class ExitMenu: TrayItem(ID){
	
	companion object {
		const val ID: String = "action_desk_exit"
	}
	
	override fun onClick() = exitProcess(0)
	
	override fun prepareMenu(): MenuItem = newMenuItem("Exit", "icon/close_16.png")
	
	override fun hasSeparatorAbove(): Boolean = false

}