package net.abdulahad.action_desk.model

import net.abdulahad.action_desk.data.Env
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Action (
	var id: Int = 0,
	var icon: String = "",
	var name: String = "",
	var description: String = "",
	var enabled: Boolean = true,
	
	var startDirectory: String = "",
	var command: String = "",
	var arguments: String = "",
	
	var runAsAdmin: Boolean = false,
	var singleton: Boolean = false,
	var startWithAD: Boolean = false,
	
	var showWindow: Boolean = false,
	var windowStyle: String = "Normal",
	var keepWindowOpen: Boolean = false,
	
	var hotkey: String = "",
	var globalKey: String = "",
	
	
	var usePIDLock: Boolean = true,
	
	var logPath: String = "",
	var pidLockPath: String = ""
) {
	
	companion object {
		fun Action.pidLockFile(): String {
			return "${Env.APP_FOLDER}/pid/$id.txt"
		}
		
		fun Action.logFolder(): String {
			return name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
		}
		
		fun Action.newLogFile(): String {
			val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")
			val timestamp = LocalDateTime.now().format(formatter)
			
			// Replace unsafe characters
			val safeName = logFolder()
			
			return "${Env.getLogFolder()}/$safeName/$timestamp.log"
		}
		
		fun Action.newLogFilePath(): String {
			val logFile = File(newLogFile())
			
			logFile.parentFile.mkdirs()
			logFile.createNewFile()
			
			return logFile.absolutePath
		}
		
		fun Action.windowHidden(): Boolean = windowStyle == "Hidden"
		
	}
	
	override fun toString(): String {
		return buildString {
			appendLine("id = $id, icon = $icon, enabled = $enabled")
			appendLine("name = $name")
			appendLine("description = $description")
			
			appendLine("startDirectory = $startDirectory")
			appendLine("command = $command")
			appendLine("arguments = $arguments")
			
			appendLine("runAsAdmin = $runAsAdmin, singleton = $singleton, startWithAD = $startWithAD")
			
			appendLine("showWindow = $showWindow, windowStyle = $windowStyle, keepWindowOpen = $keepWindowOpen")
			
			appendLine("hotkey = $hotkey, globalKey = $globalKey")
		}
	}

}
