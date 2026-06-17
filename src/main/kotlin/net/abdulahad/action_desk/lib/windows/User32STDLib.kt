@file:Suppress("FunctionName", "SpellCheckingInspection")

package net.abdulahad.action_desk.lib.windows

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.win32.StdCallLibrary

interface User32STDLib : StdCallLibrary {
	fun SetForegroundWindow(hWnd: HWND): Boolean
	fun ShowWindow(hWnd: HWND, nCmdShow: Int): Boolean
	fun IsIconic(hWnd: HWND): Boolean
	fun EnumWindows(lpEnumFunc: EnumWindowsProc, data: Pointer?): Boolean
	fun GetWindowThreadProcessId(hWnd: HWND, lpdwProcessId: IntArray): Int
	fun IsWindowVisible(hWnd: HWND): Boolean
	fun GetWindow(hWnd: HWND, uCmd: Int): HWND?
	
	companion object {
		val INSTANCE: User32STDLib = Native.load("user32", User32STDLib::class.java)
	}
	
	interface EnumWindowsProc : StdCallLibrary.StdCallCallback {
		fun callback(hWnd: HWND, data: Pointer?): Boolean
	}
}
