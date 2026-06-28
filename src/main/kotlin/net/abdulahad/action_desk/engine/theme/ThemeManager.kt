package net.abdulahad.action_desk.engine.theme

import com.formdev.flatlaf.FlatLaf
import net.abdulahad.action_desk.config.ConfigKeys
import net.abdulahad.action_desk.config.ConfigService
import net.abdulahad.action_desk.lib.tray.TrayMan
import net.abdulahad.action_desk.model.ThemeDescriptor
import net.abdulahad.action_desk.onSwing
import net.abdulahad.action_desk.view.tray.A2Tray
import java.awt.Window
import javax.swing.SwingUtilities

object ThemeManager {
	
	const val LIGHT = "light"
	const val DARK = "dark"
	const val SYSTEM = "system"
	
	val themeOptions = listOf(LIGHT, DARK, SYSTEM)
	
	fun normalize(theme: String?): String {
		return when (theme?.trim()?.lowercase()) {
			LIGHT -> LIGHT
			DARK -> DARK
			SYSTEM, "system default" -> SYSTEM
			else -> LIGHT
		}
	}
	
	fun displayName(theme: String): String {
		return when (normalize(theme)) {
			LIGHT -> "Light"
			DARK -> "Dark"
			SYSTEM -> "System default"
			else -> "Light"
		}
	}
	
	fun configuredTheme(): String {
		return normalize(ConfigService.getString(ConfigKeys.THEME, DARK))
	}
	
	fun resolvedTheme(theme: String = configuredTheme()): String {
		return when (normalize(theme)) {
			SYSTEM -> if (WindowsSystemTheme.isDark()) DARK else LIGHT
			DARK -> DARK
			else -> LIGHT
		}
	}
	
	fun createConfiguredLookAndFeel(): FlatLaf {
		return ThemeDescriptor.getByThemeName(resolvedTheme()).createInstance()
	}
	
	fun applyConfiguredTheme(reinstallTray: Boolean = true) {
		applyResolvedTheme(resolvedTheme(), reinstallTray)
	}
	
	fun applyTheme(theme: String, reinstallTray: Boolean = true) {
		applyResolvedTheme(resolvedTheme(theme), reinstallTray)
	}
	
	fun startSystemThemeWatcher() {
		SystemThemeWatcher.start { _ ->
			if (configuredTheme() == SYSTEM) {
				onSwing {
					applyConfiguredTheme()
				}
			}
		}
	}
	
	private fun applyResolvedTheme(resolvedTheme: String, reinstallTray: Boolean) {
		ThemeDescriptor
			.getByThemeName(resolvedTheme)
			.classRef.java.getMethod("setup")
			.invoke(null)
		
		if (reinstallTray) {
			TrayMan.reinstall(A2Tray.ID, A2Tray::class.java)
		}
		
		FlatLaf.updateUI()
		
		Window.getWindows().forEach { window ->
			SwingUtilities.updateComponentTreeUI(window)
			window.invalidate()
			window.validate()
			window.repaint()
		}
	}
	
}
