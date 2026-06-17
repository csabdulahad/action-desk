package net.abdulahad.action_desk

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinUser
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.engine.shortcut.ShortcutManager
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.helper.ProcessHelper
import net.abdulahad.action_desk.job.StartupJobs
import net.abdulahad.action_desk.lib.util.Alert
import net.abdulahad.action_desk.lib.util.Ben
import net.abdulahad.action_desk.repo.action.ActionRepo
import net.abdulahad.action_desk.view.ActionDesk
import java.io.File
import kotlin.system.exitProcess

object Bootstrap {
	
	// 0x0001;
	// val MOD_ALT: Int = WinUser.MOD_ALT
	
	// 0x0002;
	private const val MOD_CONTROL: Int = WinUser.MOD_CONTROL
	
	// 0x0004;
	private const val MOD_SHIFT: Int = WinUser.MOD_SHIFT
	
	// 0x0001;
	private const val MOD_ALT: Int = WinUser.MOD_ALT
	
	// 0x58;
	private const val VK_X: Int = 0x58
	
	private const val VK_BACKTICK: Int = 0xC0
	
	private const val HOTKEY_ID: Int = 1

	fun kickoff(args: Map<String, String>) {
		try {
			/*
			 * Set up the ActionDesk env & app config
			 * */
			Ben.start("envSetup")
			Env.init(args)
			Ben.end("envSetup")
			
			Ben.start("appSetup")
			App.setup()
			Ben.end("appSetup")
			
			
			/*
			 * Check for number of running instances
			 * */
			Ben.start("writeInstanceLock")
			
			if (!writeInstanceLock()) {
				return
			}
			
			Ben.end("writeInstanceLock")
			Ben.endAllPrint("Bootstrap Report")
			
			App.logInfo("ActionDesk started")
			
			
			/*
			 * Start the UI
			 * */
			if (!App.getStartMinimized()) {
				ActionDesk.showFrame()
			}
			
			
			/*
			 * Initialize shortcut manager service
			 * */
			ShortcutManager.startShortcutListener()
			
			
			/*
			 * Background startup jobs
			 * */
			onDefault {
				StartupJobs.registerADHotkey()
				StartupJobs.validateADAutoStartLink()
				StartupJobs.runAutoStartActions()
				StartupJobs.installTray()
			}
			
			
			/*
			 * Finally load the Actions ⚡
			 * */
			ActionRepo.init()
		} catch (e: java.lang.Exception) {
			try {
				App.logErr(e.stackTrace.contentToString())
			} catch (_: Exception) {}
			
			e.printStackTrace()
		}
	}
	
	private fun registerADGlobalShortcut() {
		val user32 = User32.INSTANCE
		
		if (!user32.RegisterHotKey(
				null,
				HOTKEY_ID,
				MOD_ALT,
				VK_BACKTICK))
		{
			App.logErr("Failed to register global shortcut Alt+`")
			exitProcess(0)
		}
		
		App.logInfo("ActionDesk: global shortcut Alt+` registered")
		
		// Message loop to listen for the hotkey event
		val msg = WinUser.MSG()
		
		while (user32.GetMessage(msg, null, 0, 0) != 0) {
			if (msg.message == WinUser.WM_HOTKEY) {
				val id = msg.wParam.toInt()
				
				if (id == HOTKEY_ID) {
					println(Thread.currentThread().name)
					onSwing(ActionDesk::showFrame)
				}
			}
		}
		
		// Unregister the hotkey before exiting
		user32.UnregisterHotKey(null, HOTKEY_ID)
	}
	
	private fun writeInstanceLock(): Boolean {
		val lockFile = "${Env.APP_FOLDER}/instance.lock"
		
		val file = File(lockFile)
		file.createNewFile()
		
		val lastPID = ProcessHelper.readPIDFromFile(file)
		
		if (lastPID != null && ProcessHelper.isProcessRunning(lastPID)) {
			Alert
				.confirm("An instance of ActionDesk is already running. This will quit now!")
				.title("Already Running")
				.onAck(App::close)
				.onCancel(App::close)
				.show()
			
			return false
		}
		
		val handle = ProcessHandle.current().pid()
		CommonActions.writeToFile(file, handle.toString())
		
		return true
	}
	
}