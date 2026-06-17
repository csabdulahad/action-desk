package net.abdulahad.action_desk.engine.shortcut

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.engine.action.ActionRunner
import net.abdulahad.action_desk.onSwing
import net.abdulahad.action_desk.repo.action.ActionRepo
import net.abdulahad.action_desk.repo.action.ActionRepoListener
import java.util.concurrent.ConcurrentLinkedQueue

object ShortcutManager {
	
	private val user32   = User32.INSTANCE
	private val kernel32 = Kernel32.INSTANCE
	
	// Maps Action Unique ID (from DB) -> Current Registration Info
	private val activeShortcuts = mutableMapOf<Int, RegisteredHotkey>()
	
	// Map: KeyCode -> List of possible shortcuts (e.g., 'M' could be Win+M or Win+Shift+M)
	private val winKeyShortcuts = mutableMapOf<Int, MutableList<WinShortcut>>()
	
	// Windows Hotkey IDs must be between 0x0000 and 0xBFFF
	private var idCounter = 1000
	
	private val requests = ConcurrentLinkedQueue<() -> Unit>()
	
	private var hhk: WinUser.HHOOK? = null
	
	// Storage for Win Shortcuts
	data class WinShortcut(val modifiers: Int, val action: () -> Unit)
	
	data class RegisteredHotkey(val hotkeyId: Int, val combo: String, val action: () -> Unit)
	
	private val keyboardHook = WinUser.LowLevelKeyboardProc { nCode, wParam, info ->
		if (nCode >= 0) {
			val vkCode = info.vkCode
			val event = wParam.toInt()
			val isKeyDown = event == WinUser.WM_KEYDOWN || event == WinUser.WM_SYSKEYDOWN
			
			if (isKeyDown && winKeyShortcuts.containsKey(vkCode)) {
				val currentMods = getCurrentModifiers()
				
				// Only trigger if WIN is one of the pressed modifiers
				if ((currentMods and ShortcutParser.MOD_WIN) != 0) {
					val possibleShortcuts = winKeyShortcuts[vkCode]
					val match = possibleShortcuts?.find { it.modifiers == currentMods }
					
					if (match != null) {
						onSwing {
							// TODO - check if any action is editor/create mode
							//  if so then don't invoke the action here!
							match.action()
						}
						
						return@LowLevelKeyboardProc WinDef.LRESULT(1) // Block OS
					}
				}
			}
		}
		
		user32.CallNextHookEx(hhk, nCode, wParam, WinDef.LPARAM(Pointer.nativeValue(info.pointer)))
	}
	
	/**
	 * Helper to check current state of Ctrl, Alt, Shift, and Win keys
	 */
	private fun getCurrentModifiers(): Int {
		var mods = 0
		
		// GetAsyncKeyState checks if the key is currently down (high bit set)
		if ((user32.GetAsyncKeyState(0x11).toInt() and 0x8000) != 0) mods = mods or ShortcutParser.MOD_CONTROL
		if ((user32.GetAsyncKeyState(0x12).toInt() and 0x8000) != 0) mods = mods or ShortcutParser.MOD_ALT
		if ((user32.GetAsyncKeyState(0x10).toInt() and 0x8000) != 0) mods = mods or ShortcutParser.MOD_SHIFT
		if ((user32.GetAsyncKeyState(0x5B).toInt() and 0x8000) != 0 ||
			(user32.GetAsyncKeyState(0x5C).toInt() and 0x8000) != 0) mods = mods or ShortcutParser.MOD_WIN
		return mods
	}
	
	init {
		ActionRepo.addListener(object : ActionRepoListener {
			override fun onActionRepoLoaded() {
				ActionRepo.list().forEach { action ->
					if (action.globalKey.isNotBlank()) {
						registerOrUpdate(action.id, action.globalKey) {
							action.byShortcut = true
							ActionRunner.runAction(action, false)
						}
					}
				}
			}
		})
	}
	
	fun registerOrUpdate(actionId: Int, combo: String, onExecute: () -> Unit) {
		requests.add {
			// Unregister logic
			activeShortcuts[actionId]?.let { old ->
				if (old.combo.contains("Win", ignoreCase = true)) {
					val vk = ShortcutParser.getKeyCode(old.combo)
					val mods = ShortcutParser.getModifiers(old.combo)
					
					winKeyShortcuts[vk]?.removeIf { it.modifiers == mods }
				} else {
					user32.UnregisterHotKey(null, old.hotkeyId)
				}
				
				App.logInfo("Unregistered old shortcut: ${old.combo}")
			}
			
			if (combo.isBlank()) return@add
			
			val vk = ShortcutParser.getKeyCode(combo)
			val mods = ShortcutParser.getModifiers(combo)
			
			if (combo.contains("Win", ignoreCase = true)) {
				winKeyShortcuts.getOrPut(vk) { mutableListOf() }.add(WinShortcut(mods, onExecute))
				activeShortcuts[actionId] = RegisteredHotkey(-1, combo, onExecute)
				App.logInfo("Registered Win-Hook: $combo")
			} else {
				val id = idCounter++
				if (user32.RegisterHotKey(null, id, mods, vk)) {
					activeShortcuts[actionId] = RegisteredHotkey(id, combo, onExecute)
					App.logInfo("Registered Hotkey: $combo")
				}
			}
		}
	}
	
	fun startShortcutListener() {
		Thread {
			// 1. Install Hook
			val hMod = kernel32.GetModuleHandle(null)
			hhk = user32.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardHook, hMod, 0)
			
			// 2. Message Loop
			val msg = WinUser.MSG()
			
			while (true) {
				// Process registration requests without blocking the hook
				while (requests.isNotEmpty()) {
					requests.poll()?.invoke()
				}
				
				// Use PeekMessage instead of GetMessage so the 'requests' loop above actually runs
				if (user32.PeekMessage(msg, null, 0, 0, 1)) {
					if (msg.message == WinUser.WM_HOTKEY) {
						val id = msg.wParam.toInt()
						activeShortcuts.values.find { it.hotkeyId == id }?.action?.invoke()
					}
					
					user32.TranslateMessage(msg)
					user32.DispatchMessage(msg)
				}
				
				// Tiny sleep to prevent 100% CPU usage since we are Peeking
				Thread.sleep(1)
			}
		}.apply {
			name = "ShortcutManager-Thread"
			isDaemon = true
			start()
		}
	}
	
	fun shutdown() {
		hhk?.let { user32.UnhookWindowsHookEx(it) }
	}
	
}