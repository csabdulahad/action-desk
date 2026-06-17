package net.abdulahad.action_desk.view.tray

import com.formdev.flatlaf.extras.FlatSVGUtils
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.engine.notification.NotificationListener
import net.abdulahad.action_desk.engine.notification.NotificationManager
import net.abdulahad.action_desk.lib.tray.Tray
import net.abdulahad.action_desk.lib.util.Poth
import net.abdulahad.action_desk.model.Notification
import net.abdulahad.action_desk.view.tray.menu.ExitMenu
import net.abdulahad.action_desk.view.tray.menu.HomeMenu
import net.abdulahad.action_desk.view.tray.menu.RestartMenu
import net.abdulahad.action_desk.view.tray.menu.SettingsMenu

class A2Tray : Tray(ID, ICON), NotificationListener {
	
	private val actionDeskUpdateIcon = FlatSVGUtils.svg2image(Poth.getURL("icon/action_desk_update.svg"), 32, 32)
	private val actionDeskIcon = FlatSVGUtils.svg2image(Poth.getURL("icon/action_desk.svg"), 32, 32)
	
	companion object {
		const val ID: String = "action_desk_tray"
		const val ICON: String = "icon/action_desk.svg"
	}
	
	init {
		NotificationManager.listen(this)
	}
	
	override fun onNewNotification(notification: Notification) {
		if (notification.isSilent) return
		systemTray?.setImage(actionDeskUpdateIcon)
	}
	
	override fun onNotificationsAcknowledged() {
		systemTray?.setImage(actionDeskIcon)
	}
	
	override fun onNotificationClear() {
		systemTray?.setImage(actionDeskIcon)
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
