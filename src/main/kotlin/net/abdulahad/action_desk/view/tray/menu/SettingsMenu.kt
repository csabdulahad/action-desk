package net.abdulahad.action_desk.view.tray.menu

import dorkbox.systemTray.MenuItem
import net.abdulahad.action_desk.helper.ViewHelper
import net.abdulahad.action_desk.lib.tray.TrayItem
import net.abdulahad.action_desk.view.settings.Settings

class SettingsMenu: TrayItem(ID) {
	
	companion object {
		const val ID: String = "action_desk_settings"
	}
	
	override fun onClick() {
		ViewHelper.hideAndSeekWindow(
			Settings(),
			null,
			false)
	}
	
	override fun prepareMenu(): MenuItem = newMenuItem("Settings", "icon/setting_16.png")
	
	override fun hasSeparatorAbove(): Boolean {
		return true
	}
	
}