package net.abdulahad.action_desk.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.Bootstrap
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.engine.ActionManager
import net.abdulahad.action_desk.lib.util.Alert
import net.abdulahad.action_desk.onUI
import net.abdulahad.action_desk.runtimeException
import net.abdulahad.action_desk.view.ActionDesk
import java.io.File
import java.net.URISyntaxException
import java.security.SecureRandom
import kotlin.math.abs

object ProcessHelper {
	
	fun getCurrentADPID(): Long {
		return ProcessHandle.current().pid()
	}
	
	fun getActionDeskJarFile(): File? {
		return try {
			val url = Bootstrap::class.java.protectionDomain.codeSource.location.toURI()
			val file = File(url.path)
			
			if (file.exists() && file.extension == "jar") {
				file
			} else {
				// Not running from a JAR (probably IDE) → optionally return null
				null
			}
		} catch (e: URISyntaxException) {
			e.printStackTrace()
			null
		}
	}
	
	fun killProcess(pid: Long, callback: ((result: Boolean) -> Unit)? = null) {
		CoroutineScope(Dispatchers.Default).launch {
			var success = true
			var killMsg: String? = null
			
			val unsignedPID = abs(pid)
			val isAdminProcess = pid.toString().startsWith("-")
			val childPIDs = ActionManager.collectChildPIDs(pid)
			
			val cmdList = mutableListOf<String>()
			
			if (isAdminProcess) {
				cmdList.add("Start-Process")
				
				cmdList.add("-Verb")
				cmdList.add("RunAs")
				
				cmdList.add("-FilePath")
				cmdList.add("taskkill")
				
				cmdList.add("-ArgumentList")
			} else {
				cmdList.add("taskkill")
			}
			
			
			/*
			 * Build taskkill argument list
			 * */
			val taskKillArgs = mutableListOf<String>()
			
			taskKillArgs.add("/F")
			
			// Child PIDs
			childPIDs.forEach {
				taskKillArgs.add("/PID")
				taskKillArgs.add(it.toString())
			}
			
			// Parent PID
			taskKillArgs.add("/PID")
			taskKillArgs.add(unsignedPID.toString())
			
			
			/*
			 * Append taskkill argument list to cmd list
			 * */
			if (isAdminProcess) {
				cmdList.add("'${taskKillArgs.joinToString(" ")}'")
			} else {
				cmdList.addAll(taskKillArgs)
			}
			
			
			/*
			 * Build the final cmd list
			 * */
			val finalCMDList = if (isAdminProcess) {
				listOf(
					App.getPSBin(),
					"-Command",
					cmdList.joinToString(" ")
				)
			} else {
				cmdList
			}
			
			val logMsg = "Killing process $unsignedPID: ${finalCMDList.joinToString(" ")}"
			println(logMsg)
			App.logInfo(logMsg)
			
			
			try {
				val pb = ProcessBuilder(finalCMDList)
				pb.redirectErrorStream(true)
				
				val process = pb.start()
				
				val output = process.inputStream.bufferedReader().use { it.readText() }
				val exitCode = process.waitFor()
				
				var msg = "taskkill exited with code $exitCode"
				println(msg)
				App.logInfo(msg)
				
				if (exitCode != 0) {
					msg += "\n$output"
					runtimeException(msg, true)
				}
			} catch (e: Exception) {
				success = false
				
				killMsg = e.message
				println(killMsg)
				App.logErr(killMsg)
			}
			
			if (callback != null) {
				onUI {
					if (!success && killMsg != null) {
						Alert
							.confirm(killMsg)
							.title("Error")
							.show(ActionDesk)
					}
					
					callback.invoke(success)
				}
			}
		}
	}
	
	fun readPIDFromFile(pidFile: File): String? {
		return try {
			val x = pidFile.readText().trim()
			Regex("-?\\d+").find(x)?.value
		} catch (e: Exception) {
			null
		}
	}
	
	fun isProcessRunning(pid: Long): Boolean {
		val process = ProcessBuilder("tasklist", "/FI", "\"PID eq $pid\"")
			.redirectErrorStream(true)
			.start()
		
		return process.inputStream
			.bufferedReader()
			.readText()
			.contains(pid.toString())
	}
	
	fun isProcessRunning(pid: String): Boolean {
		return isProcessRunning(pid.toLong())
	}

	fun generatePIDLockFileName(actionId: Int): String {
		val random = SecureRandom()
		val bytes = ByteArray(4)
		random.nextBytes(bytes)
		val hash =  bytes.joinToString("") { "%02x".format(it) }
		
		return "${Env.getPIDFolder()}/$actionId-$hash.txt"
	}
	
}