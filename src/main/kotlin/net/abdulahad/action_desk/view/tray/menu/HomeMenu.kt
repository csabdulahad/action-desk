package net.abdulahad.action_desk.view.tray.menu

import dorkbox.systemTray.MenuItem
import net.abdulahad.action_desk.lib.tray.TrayItem
import net.abdulahad.action_desk.view.ActionDesk
import java.awt.Taskbar
import java.util.*
import javax.swing.SwingUtilities

class HomeMenu : TrayItem(ID) {
	
	companion object {
		const val ID: String = "action_desk"
	}
	
	override fun onClick() {
		ActionDesk.showFrame()
		
		SwingUtilities.invokeLater {
			val taskbar = Taskbar.getTaskbar()
			val random = Random()
			taskbar.setWindowProgressValue(ActionDesk, random.nextInt(10, 90))
		}
	}
	
	override fun prepareMenu(): MenuItem = newMenuItem("Open", "icon/actionDesk.svg")
	
	override fun hasSeparatorAbove() = true
	
}