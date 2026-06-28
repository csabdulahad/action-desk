package net.abdulahad.action_desk.engine.theme

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import net.abdulahad.action_desk.App

object WindowsSystemTheme {
	
	private const val PERSONALIZE_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"
	private const val APPS_USE_LIGHT_THEME = "AppsUseLightTheme"
	
	fun isDark(): Boolean {
		return try {
			Advapi32Util.registryGetIntValue(
				WinReg.HKEY_CURRENT_USER,
				PERSONALIZE_KEY,
				APPS_USE_LIGHT_THEME
			) == 0
		} catch (e: Exception) {
			App.logWarn("Could not read Windows app theme. Falling back to light theme. Cause: ${e.message}")
			false
		}
	}
	
}
