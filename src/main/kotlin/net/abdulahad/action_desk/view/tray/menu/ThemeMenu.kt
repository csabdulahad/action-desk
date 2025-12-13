package net.abdulahad.action_desk.view.tray.menu

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.FlatSVGUtils
import dorkbox.systemTray.MenuItem
import net.abdulahad.action_desk.config.ConfigKeys
import net.abdulahad.action_desk.config.ConfigService
import net.abdulahad.action_desk.lib.tray.TrayItem
import net.abdulahad.action_desk.lib.tray.TrayMan
import net.abdulahad.action_desk.model.ThemeDescriptor
import net.abdulahad.action_desk.view.tray.A2Tray
import java.awt.event.ActionListener
import javax.swing.SwingUtilities

class ThemeMenu: TrayItem(ID) {
	
	companion object {
		const val ID: String = "action_desk_theme"
	}
	
	override fun prepareMenu(): MenuItem {
		val settings = newMenu("Theme", "icon/brush_16.png")
		
		val checkedIcon = FlatSVGUtils.svg2image("/icon/checked.svg", 16, 16)
		val currentTheme = ConfigService.getString(ConfigKeys.THEME, "dark")
		
		listOf<MenuItem>(
			newMenuItem("Light"),
			newMenuItem("Dark")
		).forEach { item ->
			
			item.callback = ActionListener {
				this@ThemeMenu.toggleTheme(item.text)
			}
			
			if (item.text.lowercase() == currentTheme.lowercase()) {
				item.setImage(checkedIcon)
			}
			
			settings.add(item)
		}
		
		return settings
	}
	
	private fun toggleTheme(theme: String) {
		val themeLower = theme.lowercase()
		
		ConfigService.commit(ConfigKeys.THEME, themeLower)
		ConfigService.flush()
		
		SwingUtilities.invokeLater {
			ThemeDescriptor
				.getByThemeName(themeLower)
				.classRef.java.getMethod("setup")
				.invoke(null)

			TrayMan.reinstall(A2Tray.ID, A2Tray::class.java)
			FlatLaf.updateUI()
		}
	}
	
}