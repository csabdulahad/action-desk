package net.abdulahad.action_desk.view.tray

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.lib.tray.Tray
import net.abdulahad.action_desk.view.tray.menu.SettingsMenu
import net.abdulahad.action_desk.view.tray.menu.ExitMenu
import net.abdulahad.action_desk.view.tray.menu.HomeMenu
import net.abdulahad.action_desk.view.tray.menu.RestartMenu

class A2Tray : Tray(ID, ICON) {
	
	companion object {
		const val ID: String = "action_desk_tray"
		const val ICON: String = "icon/actionDesk.svg"
	}
	
	protected override fun registerItem() {
		addMenu(
			HomeMenu(),
			SettingsMenu(),
			RestartMenu(),
			ExitMenu()
		)
	}
	
	override fun postInstall() {
		setStatus(App.NAME)
	}
	
}
