package net.abdulahad.action_desk.lib.windows

import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.HWND
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.event.InputEvent

object WinHelper {
	
	val INSTANCE: User32STDLib = Native.load("user32", User32STDLib::class.java)
	
	private const val GW_OWNER = 4
	private const val SW_SHOW = 5
	private const val SW_RESTORE = 9
	
	private fun ghostClickNudge(pid: Long) {
		val user32 = INSTANCE
		var targetHWnd: HWND? = null
		
		user32.EnumWindows(object : User32STDLib.EnumWindowsProc {
			override fun callback(hWnd: HWND, data: com.sun.jna.Pointer?): Boolean {
				val windowPid = IntArray(1)
				user32.GetWindowThreadProcessId(hWnd, windowPid)
				
				if (windowPid[0].toLong() == pid) {
					val isVisible = user32.IsWindowVisible(hWnd)
					val hasNoOwner = user32.GetWindow(hWnd, GW_OWNER) == null
					
					if (isVisible && hasNoOwner) {
						targetHWnd = hWnd
						return false
					}
				}
				
				return true
			}
		}, null)
		
		targetHWnd?.let { hwnd ->
			if (user32.IsIconic(hwnd)) {
				user32.ShowWindow(hwnd, SW_RESTORE)
				Thread.sleep(100)
			}
			
			user32.ShowWindow(hwnd, SW_SHOW)
			user32.SetForegroundWindow(hwnd)
		}
	}
	
	fun bringWindowInFrontByPID(targetPid: Long) {
		val robot = Robot()
		val oldPos = MouseInfo.getPointerInfo().location
		
		robot.mouseMove(0, 0)
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
		
		robot.mouseMove(oldPos.x, oldPos.y)
		
		ghostClickNudge(targetPid)
	}
	
}
