package net.abdulahad.action_desk.config

import com.formdev.flatlaf.FlatLaf
import net.abdulahad.action_desk.data.AppValues
import net.abdulahad.action_desk.engine.ActionRunner
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.lib.tray.TrayMan
import net.abdulahad.action_desk.model.ThemeDescriptor
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.view.tray.A2Tray
import java.io.File
import javax.swing.SwingUtilities

object AppConfig {
	/*
	 * Auto run
	 * */
	fun applyAutoRun(enable: Boolean) {
		if (enable) {
			CommonActions.actionDeskShortcut(AppValues.START_UP_FOLDER)
		} else {
			
			if (!isAutoRestarting()) {
				return
			}
			
			val lnk = AppValues.START_UP_FOLDER + "/ActionDesk.lnk"
			val file = File(lnk)
			
			file.delete()
		}
	}
	
	fun setAutoRun(enable: Boolean, apply: Boolean = false) {
		val current = getAutoRun()
		if (apply && current != enable) applyAutoRun(enable)
		
		ConfigService.commit(ConfigKeys.AUTOSTART, enable)
	}
	
	fun getAutoRun() : Boolean {
		return ConfigService.getBool(ConfigKeys.AUTOSTART, true)
	}
	
	fun isAutoRestarting(): Boolean {
		val lnk = AppValues.START_UP_FOLDER + "/ActionDesk.lnk"
		return File(lnk).exists()
	}
	
	
	/*
	 * Start minimized
	 * */
	fun setStartMinimized(enable: Boolean, apply: Boolean = false) {
		ConfigService.commit(ConfigKeys.START_MINIMIZED, enable)
	}
	
	fun getStartMinimized(): Boolean {
		return ConfigService.getBool(ConfigKeys.START_MINIMIZED, true)
	}
	
	
	/*
	 * Theme
	 * */
	fun applyTheme(theme: String) {
		SwingUtilities.invokeLater {
			ThemeDescriptor.Companion
				.getByThemeName(theme)
				.classRef.java.getMethod("setup")
				.invoke(null)
			
			TrayMan.reinstall(A2Tray.Companion.ID, A2Tray::class.java)
			FlatLaf.updateUI()
		}
	}
	
	fun setTheme(theme: String, apply: Boolean = false) {
		val t = theme.lowercase()
		val current = getTheme()
		
		if (apply && current != t) applyTheme(t)
		
		ConfigService.commit(ConfigKeys.THEME, t)
	}
	
	fun getTheme(): String {
		return ConfigService.getString(ConfigKeys.THEME, "light").lowercase()
	}
	
	
	/*
	 * Search bar focus
	 * */
	fun applySearchFocus(enable: Boolean) {
		ActionDesk.applyFocusSearch(enable)
	}
	
	fun setSearchFocus(enable: Boolean, apply: Boolean = false) {
		val current = getSearchFocus()
		if (apply && current != enable) applySearchFocus(enable)
		
		ConfigService.commit(ConfigKeys.FOCUS_SEARCH, enable)
	}
	
	fun getSearchFocus(): Boolean {
		return ConfigService.getBool(ConfigKeys.FOCUS_SEARCH, true)
	}
	
	
	/*
	 * Always on top
	 * */
	fun applyAlwaysOnTop(enable: Boolean) {
		ActionDesk.applyAlwaysOnTop(enable)
	}
	
	fun setAlwaysOnTop(enable: Boolean, apply: Boolean = false) {
		val current = getAlwaysOnTop()
		if (apply && current != enable) applyAlwaysOnTop(enable)
		
		ConfigService.commit(ConfigKeys.ALWAYS_ON_TOP, enable)
	}
	
	fun getAlwaysOnTop(): Boolean {
		return ConfigService.getBool(ConfigKeys.ALWAYS_ON_TOP, true)
	}
	
	
	/*
	 * Hide after action execution
	 * */
	fun applyHideAfterAction(enable: Boolean) {
		ActionRunner.setHideAfterAction(enable)
	}
	
	fun setHideAfterAction(enable: Boolean, apply: Boolean = false) {
		val current = getHideAfterAction()
		if (apply && current != enable) applyHideAfterAction(enable)
		
		ConfigService.commit(ConfigKeys.HIDE_AFTER_ACTION, enable)
	}
	
	fun getHideAfterAction(): Boolean {
		return ConfigService.getBool(ConfigKeys.HIDE_AFTER_ACTION, true)
	}
	
}