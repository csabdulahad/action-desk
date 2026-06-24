package net.abdulahad.action_desk.config

import com.formdev.flatlaf.FlatLaf
import net.abdulahad.action_desk.data.AppValues
import net.abdulahad.action_desk.engine.action.ActionRunner
import net.abdulahad.action_desk.engine.adcd.AdcdDaemon
import net.abdulahad.action_desk.engine.shortcut.ActionDeskHKAction
import net.abdulahad.action_desk.engine.shortcut.ShortcutManager
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.lib.tray.TrayMan
import net.abdulahad.action_desk.model.ThemeDescriptor
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.view.tray.A2Tray
import java.awt.Point
import java.io.File
import javax.swing.SwingUtilities

object AppConfig {
	
	const val DEFAULT_ADCD_PORT = 4788
	const val DEFAULT_ADCD_HOST_LOCAL = "127.0.0.1"
	const val DEFAULT_ADCD_HOST_NETWORK = "0.0.0.0"
	
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
		
		ConfigService.commit(ConfigKeys.THEME, t)
	}
	
	fun getTheme(): String {
		return ConfigService.getString(ConfigKeys.THEME, "light").lowercase()
	}
	
	
	/*
	 * Search bar focus
	 * */
	fun applyWindowSize(value: Point) {
		ActionDesk.applyWindowSize(value)
	}
	
	fun setWindowSize(value: Point, apply: Boolean = false) {
		val current = getWindowSize()
		
		if (apply && current != value) applyWindowSize(value)
		
		ConfigService.commit(ConfigKeys.WINDOW_SIZE, "${value.x}x${value.y}")
	}
	
	fun getWindowSize(): Point {
		val value  = ConfigService.getString(ConfigKeys.WINDOW_SIZE, "450x500")
		val values = value.split("x")
		return Point(values[0].toInt(), values[1].toInt())
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
	
	
	/*
	 * Enable auto start actions setting
	 * */
	fun getEnableAutoStartActions(): Boolean {
		return ConfigService.getBool(ConfigKeys.ENABLE_AUTO_START_ACTIONS, true)
	}
	
	fun setEnableAutoStartActions(value: Boolean) {
		ConfigService.commit(ConfigKeys.ENABLE_AUTO_START_ACTIONS, value)
	}
	
	
	/*
	 * ActionDesk window in center
	 * */
	fun getShowWindowInCenter(): Boolean {
		return ConfigService.getBool(ConfigKeys.SHOW_WINDOW_IN_CENTER, false)
	}
	
	fun setShowWindowInCenter(selected: Boolean) {
		ActionDesk.applyShowWindowInCenter(selected)
		
		if (getShowWindowInCenter() != selected) {
			ConfigService.commit(ConfigKeys.SHOW_WINDOW_IN_CENTER, selected)
		}
	}
	
	/*
	 * ActionDesk global hotkey
	 * */
	fun applyADHotKey(value: String) {
		val action  = ActionDeskHKAction
		action.globalKey = value
		
		ShortcutManager.registerOrUpdate(action.ID, action.globalKey, action::run)
	}
	
	fun getADHotkey(): String {
		return ConfigService.getString(ConfigKeys.ACTION_DESK_HOTKEY, "Alt+Back Quote")
	}
	
	fun setADHotkey(value: String) {
		val lastHotkey = getADHotkey()
		
		if (lastHotkey != value) {
			applyADHotKey(value)
		}
		
		ConfigService.commit(ConfigKeys.ACTION_DESK_HOTKEY, value)
	}
	
	fun setPSBin(value: String) {
		ConfigService.commit(ConfigKeys.PS_BIN, value)
	}
	
	fun getPSBin(): String {
		return ConfigService.getString(ConfigKeys.PS_BIN, "powershell")
	}
	
	
	/*
	 * ADCD
	 * */
	fun applyAdcd() {
		if (getAdcdEnabled()) {
			AdcdDaemon.start(getAdcdHost(), getAdcdPort())
		} else {
			AdcdDaemon.stop()
		}
	}
	
	fun getAdcdEnabled(): Boolean {
		return ConfigService.getBool(ConfigKeys.ADCD_ENABLED, true)
	}
	
	fun setAdcdEnabled(enable: Boolean, apply: Boolean = false) {
		val current = getAdcdEnabled()
		
		ConfigService.commit(ConfigKeys.ADCD_ENABLED, enable)
		
		if (apply && current != enable) {
			applyAdcd()
		}
	}
	
	fun getAdcdAllowNetwork(): Boolean {
		return ConfigService.getBool(ConfigKeys.ADCD_ALLOW_NETWORK, false)
	}
	
	fun setAdcdAllowNetwork(enable: Boolean, apply: Boolean = false) {
		val current = getAdcdAllowNetwork()
		
		ConfigService.commit(ConfigKeys.ADCD_ALLOW_NETWORK, enable)
		
		if (apply && current != enable) {
			applyAdcd()
		}
	}
	
	fun getAdcdHost(): String {
		return if (getAdcdAllowNetwork()) {
			DEFAULT_ADCD_HOST_NETWORK
		} else {
			DEFAULT_ADCD_HOST_LOCAL
		}
	}
	
	fun getAdcdPort(): Int {
		return try {
			ConfigService
				.getInt(ConfigKeys.ADCD_PORT, DEFAULT_ADCD_PORT)
				.coerceIn(1024, 65535)
		} catch (_: Exception) {
			DEFAULT_ADCD_PORT
		}
	}
	
	fun setAdcdPort(port: Int, apply: Boolean = false) {
		val cleanPort = port.coerceIn(1024, 65535)
		val current = getAdcdPort()
		
		ConfigService.commit(ConfigKeys.ADCD_PORT, cleanPort)
		
		if (apply && current != cleanPort) {
			applyAdcd()
		}
	}
	
	fun getAdcdMuteSound(): Boolean {
		return ConfigService.getBool(ConfigKeys.ADCD_MUTE_SOUND, false)
	}
	
	fun setAdcdMuteSound(enable: Boolean) {
		ConfigService.commit(ConfigKeys.ADCD_MUTE_SOUND, enable)
	}
	
	fun getAdcdDisableDialog(): Boolean {
		return ConfigService.getBool(ConfigKeys.ADCD_DISABLE_DIALOG, false)
	}
	
	fun setAdcdDisableDialog(enable: Boolean) {
		ConfigService.commit(ConfigKeys.ADCD_DISABLE_DIALOG, enable)
	}
	
}