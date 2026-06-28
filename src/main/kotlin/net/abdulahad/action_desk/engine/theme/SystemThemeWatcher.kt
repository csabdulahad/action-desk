package net.abdulahad.action_desk.engine.theme

import com.sun.jna.platform.win32.Advapi32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinReg
import net.abdulahad.action_desk.App
import java.util.concurrent.atomic.AtomicBoolean

object SystemThemeWatcher {
	
	private const val PERSONALIZE_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"
	private const val REG_NOTIFY_CHANGE_LAST_SET = 0x00000004
	
	private val started = AtomicBoolean(false)
	
	fun start(onThemeChanged: (Boolean) -> Unit) {
		if (!started.compareAndSet(false, true)) {
			return
		}
		
		Thread {
			watch(onThemeChanged)
		}.apply {
			name = "SystemThemeWatcher-Thread"
			isDaemon = true
			start()
		}
	}
	
	private fun watch(onThemeChanged: (Boolean) -> Unit) {
		val hKeyRef = WinReg.HKEYByReference()
		
		val openResult = Advapi32.INSTANCE.RegOpenKeyEx(
			WinReg.HKEY_CURRENT_USER,
			PERSONALIZE_KEY,
			0,
			WinNT.KEY_READ,
			hKeyRef
		)
		
		if (openResult != 0) {
			App.logWarn("Could not open Windows theme registry key. System theme auto-update disabled. Code: $openResult")
			started.set(false)
			return
		}
		
		val hKey = hKeyRef.value
		
		try {
			while (true) {
				val result = Advapi32.INSTANCE.RegNotifyChangeKeyValue(
					hKey,
					false,
					REG_NOTIFY_CHANGE_LAST_SET,
					null,
					false
				)
				
				if (result != 0) {
					App.logWarn("Windows theme registry watcher stopped. Code: $result")
					return
				}
				
				onThemeChanged(WindowsSystemTheme.isDark())
			}
		} finally {
			Advapi32.INSTANCE.RegCloseKey(hKey)
			started.set(false)
		}
	}
	
}
