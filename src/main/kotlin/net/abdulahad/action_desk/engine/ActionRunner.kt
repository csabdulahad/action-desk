package net.abdulahad.action_desk.engine

import kotlinx.coroutines.*
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.data.AppConfig
import net.abdulahad.action_desk.engine.executor.ActionExecutor
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.helper.ProcessHelper
import net.abdulahad.action_desk.lib.util.Alert
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.ActionDesk
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

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
			
			delay(interval)
		}
		
		val msg = "grabPID: Timed out awaiting PID read from $filePath"
		println(msg)
		App.logWarn(msg)
		
		// Timeout
		return@withContext null
	}
	
	fun runAction(action: Action, diagnose: Boolean, bootupRun: Boolean = false) {
		if (diagnose) {
			App.logInfo("${action.name}: attempting diagnose run")
		} else {
			App.logInfo("${action.name}: attempting to run")
		}
		
		if (
			action.singleton &&
			ActionManager.isRunning(action) &&
			!bootupRun
		) {
			val msg = "${action.name}: already running"
			
			println(msg)
			App.logWarn(msg)
			Alert.confirm(msg).title("Singleton").show(ActionDesk)
			
			return
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
				var path = ProcessHelper.generatePIDLockFileName(action.id)
				
				while (true) {
					val file = File(path)
				
					if (file.exists()) {
						val msg = "Unique PID hash file collision happened: $path"
						println(msg)
						App.logWarn(msg)
						
						path = ProcessHelper.generatePIDLockFileName(action.id)
						continue
					}
					
					break
				}
				
				action.pidLockPath = path
				
				ActionExecutor.execute(action, diagnose)
				
				val	pid = grabPIDFromLockFile(path)
				
				val msg = "PID by grabPID: $pid"
				println(msg)
				App.logInfo(msg)
				
			} catch (e: Exception) {
				val msg = "${action.name}: ${e.message}"
				println(msg)
				
				if (diagnose) {
					App.logTagged("DIAGNOSIS", msg)
					
					Files.writeString(
						File(action.logPath).toPath(),
						e.message + System.lineSeparator(),
						StandardOpenOption.CREATE,
						StandardOpenOption.APPEND
					)
					
					CommonActions.openInNotepad(action.logPath)
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