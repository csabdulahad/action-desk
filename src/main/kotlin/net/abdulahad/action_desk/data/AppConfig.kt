package net.abdulahad.action_desk.data

import com.formdev.flatlaf.FlatLaf
import net.abdulahad.action_desk.engine.ActionRunner
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.lib.tray.TrayMan
import net.abdulahad.action_desk.model.ThemeDescriptor
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.view.tray.A2Tray
import java.io.File
import javax.swing.SwingUtilities

object AppConfig {

	fun flush() {
		Env.CONFIG.save()
	}
	
	
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
		
		Env.config.set("auto_restart", enable)
	}
	
	fun getAutoRun() : Boolean {
		return Env.config.path("auto_restart").bool(true)
	}
	
	fun isAutoRestarting(): Boolean {
		val lnk = AppValues.START_UP_FOLDER + "/ActionDesk.lnk"
		return File(lnk).exists()
	}
	
	
	/*
	 * Start minimized
	 * */
	fun setStartMinimized(enable: Boolean, apply: Boolean = false) {
		Env.config.set("start_minimized", enable)
	}
	
	fun getStartMinimized(): Boolean {
		return Env.config.path("start_minimized").bool(true)
	}
	
	
	/*
	 * Theme
	 * */
	fun applyTheme(theme: String) {
		SwingUtilities.invokeLater {
			ThemeDescriptor
				.getByThemeName(theme)
				.classRef.java.getMethod("setup")
				.invoke(null)
			
			TrayMan.reinstall(A2Tray.ID, A2Tray::class.java)
			FlatLaf.updateUI()
		}
	}
	
	fun setTheme(theme: String, apply: Boolean = false) {
		val t = theme.lowercase()
		val current = getTheme()
		
		if (apply && current != t) applyTheme(t)
		
		Env.config.set("theme", t)
	}
	
	fun getTheme(): String {
		return Env.config.getString("theme", "light")!!.lowercase()
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
		
		Env.config.set("focus_search", enable)
	}
	
	fun getSearchFocus(): Boolean {
		return Env.config.path("focus_search").bool(true)
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
		
		Env.config.set("always_on_top", enable)
	}
	
	fun getAlwaysOnTop(): Boolean {
		return Env.config.path("always_on_top").bool(true)
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
		
		Env.config.set("hide_after_action", enable)
	}
	
	fun getHideAfterAction(): Boolean {
		return Env.config.path("hide_after_action").bool(true)
	}
}