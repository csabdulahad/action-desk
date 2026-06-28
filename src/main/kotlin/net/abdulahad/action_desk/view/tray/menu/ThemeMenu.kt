package net.abdulahad.action_desk.view.tray.menu

import com.formdev.flatlaf.extras.FlatSVGUtils
import dorkbox.systemTray.MenuItem
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.lib.tray.TrayItem
import net.abdulahad.action_desk.engine.theme.ThemeManager
import java.awt.event.ActionListener

class ThemeMenu: TrayItem(ID) {
	
	companion object {
		const val ID: String = "action_desk_theme"
	}
	
	override fun prepareMenu(): MenuItem {
		val settings = newMenu("Theme", "icon/brush_16.png")
		
		val checkedIcon = FlatSVGUtils.svg2image("/icon/checked.svg", 16, 16)
		val currentTheme = AppConfig.getTheme()
		
		listOf<MenuItem>(
			newMenuItem("Light"),
			newMenuItem("Dark"),
			newMenuItem("System default")
		).forEach { item ->
			
			item.callback = ActionListener {
				this@ThemeMenu.toggleTheme(item.text)
			}
			
			if (ThemeManager.normalize(item.text) == currentTheme) {
				item.setImage(checkedIcon)
			}
			
			settings.add(item)
		}
		
		return settings
	}
	
	private fun toggleTheme(theme: String) {
		AppConfig.setTheme(theme, true)
	}
	
}