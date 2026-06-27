package net.abdulahad.action_desk.engine.action

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.engine.action.executor.ActionExecutor
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.helper.ProcessHelper
import net.abdulahad.action_desk.lib.util.Alert
import net.abdulahad.action_desk.lib.windows.WinHelper
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.engine.notification.NotificationManager
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.time.Duration.Companion.milliseconds

object ActionRunner {
	
	private var hideAfterAction = AppConfig.getHideAfterAction()
	
	suspend fun grabPIDFromLockFile(
		filePath: String,
		timeout: Long = 60000, // one minute!
		interval: Long = 100): Long? = withContext(Dispatchers.IO)
	{
		val file = File(filePath)
		val start = System.currentTimeMillis()
		
		while (System.currentTimeMillis() - start < timeout) {
			if (file.exists()) {
				val line = file.useLines { it.firstOrNull() }
				
				if (!line.isNullOrBlank()) {
					return@withContext line.toLong()
				}
			}
			
			delay(interval.milliseconds)
		}
		
		val msg = "grabPID: Timed out awaiting PID read from $filePath"
		println(msg)
		App.logWarn(msg)
		
		// Timeout
		return@withContext null
	}
	
	fun runAction(action: Action, diagnose: Boolean, bootupRun: Boolean = false, automaticRun: Boolean = bootupRun) {
		val byShortcut = action.byShortcut
		action.byShortcut = false
		
		if (diagnose) {
			App.logInfo("${action.name}: attempting diagnose run")
		} else {
			App.logInfo("${action.name}: attempting to run")
		}
		
		val pid = ActionManager.getActionPID(action)
		
		if (action.singleton && action.bringWindow && pid != null) {
			WinHelper.bringWindowInFrontByPID(pid)
			return
		}
		
		if (action.singleton && ActionManager.isRunning(action)) {
			if (!bootupRun) {
				val msg = "${action.name} is already running"

				App.logWarn(msg)
				NotificationManager.info(msg)
				
				if (App.isShown()) {
					App.setMessage(msg)
				} else {
					Alert.confirm(msg).title("Singleton").show(ActionDesk)
				}
			}

			return
		}
		
		val runnableAction = ActionRunGuard.prepareActionForRun(action, diagnose, automaticRun) ?: return
		
		if (!diagnose) {
			App.setMessage("Last action: ${runnableAction.name}")
			NotificationManager.info("Last action: ${runnableAction.name}", isSilent = !byShortcut)
		}
		
		if (!bootupRun && hideAfterAction) {
			ActionDesk.hideFrame()
		}
		
		CoroutineScope(Dispatchers.IO).launch {
			try {
				/*
				 * Get PID lock file, if exists with the hash combination
				 * then keep calling until we get a unique one!
				 * */
				var path = ProcessHelper.generatePIDLockFileName(runnableAction.id)
				
				while (true) {
					val file = File(path)
				
					if (file.exists()) {
						val msg = "Unique PID hash file collision happened: $path"
						println(msg)
						App.logWarn(msg)
						
						path = ProcessHelper.generatePIDLockFileName(runnableAction.id)
						continue
					}
					
					break
				}
				
				runnableAction.pidLockPath = path
				
				ActionExecutor.execute(runnableAction, diagnose)
				
				val pid = grabPIDFromLockFile(path)
				
				ActionManager.registerActionPID(runnableAction.id, pid)
				
				val msg = "PID by grabPID: $pid"
				println(msg)
				App.logInfo(msg)
				
			} catch (e: Exception) {
				val msg = "${runnableAction.name}: ${e.message}"
				println(msg)
				
				if (diagnose) {
					App.logTagged("DIAGNOSIS", msg)
					
					Files.writeString(
						File(runnableAction.logPath).toPath(),
						e.message + System.lineSeparator(),
						StandardOpenOption.CREATE,
						StandardOpenOption.APPEND
					)
					
					CommonActions.openInNotepad(runnableAction.logPath)
				} else {
					App.logErr(msg)
				}
			}
		}
	}
	
	fun setHideAfterAction(enable: Boolean) {
		hideAfterAction = enable
	}
	
}