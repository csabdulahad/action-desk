package net.abdulahad.action_desk.engine.executor

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.model.Action.Companion.newLogFilePath
import net.abdulahad.action_desk.model.Action.Companion.windowHidden
import java.io.File
import java.util.concurrent.TimeUnit

object ActionExecutor {
	
	val psBin: String = App.getPSBin()
	
	fun buildCommand(action: Action): List<String> {
		val powershellCmd = mutableListOf(
			"Start-Process",
			"-FilePath", action.command,
			"-PassThru"
		)
		
		if (!action.showWindow) {
			if (!action.runAsAdmin) {
				powershellCmd.add("-NoNewWindow")
			} else {
				// TODO - attention - is it dangerous?
				powershellCmd.add("-WindowStyle")
				powershellCmd.add("Hidden")
			}
		} else {
			powershellCmd.add("-WindowStyle")
			powershellCmd.add(action.windowStyle)
		}
		
		if (action.startDirectory.isNotEmpty()) {
			powershellCmd.add("-WorkingDirectory")
			powershellCmd.add("'${action.startDirectory}'")
		}
		
		if (action.runAsAdmin) {
			powershellCmd.add("-Verb")
			powershellCmd.add("RunAs")
		}
		
		/*
		 * Encode command to avoid quote escaping mess!
		 * */
		if (action.arguments.trim().isNotEmpty()) {
			var args = action.arguments
				.lines()
				.map { it.trim() }
				.filter { it.isNotEmpty() }
				.joinToString(" ")
			
			val flag = when {
				args.contains("-ec") -> "-ec"
				args.contains("-EncodedCommand") -> "-EncodedCommand"
				args.contains("-e") -> "-e"
				else -> null
			}
			
			if (flag != null) {
				val before = args.substringBefore(flag).trim()
				
				var cmdValue = args.substringAfter(flag).trim()
				cmdValue = cmdValue.substring(1, cmdValue.length - 1)
				
				val encoded = PSExecutor.encodeToBase64Utf16LE(cmdValue)
				
				args = "$before $flag $encoded"
			}
			
			powershellCmd.add("-ArgumentList")
			powershellCmd.add("'${args}'")
		}
		
		var cmd = powershellCmd
			.filter { it.isNotEmpty() }
			.joinToString(" ")
		
		val adminPrefix = if (action.runAsAdmin) "-1" else "1"
		
		cmd = "$adminPrefix * ($cmd).Id | Out-File '${action.pidLockPath}' -Encoding utf8"
		
		return listOf(psBin, "-Command", cmd)
	}
	
	fun execute(
		action: Action,
		diagnose: Boolean = false,
		waitTime: Long = 60
	) {
		
		if (diagnose || action.windowHidden()) {
			action.logPath = action.newLogFilePath()
		}
		
		val cmd = buildCommand(action)
		
		val msg = "Running command: ${cmd.joinToString(" ")}"
		App.logInfo(msg)
		
		val processBuilder = ProcessBuilder(cmd)
		
		if (action.startDirectory.isNotEmpty()) {
			processBuilder.directory(File(action.startDirectory))
		}
		
		if (diagnose) {
			processBuilder.redirectErrorStream(true)
			processBuilder.redirectOutput(File(action.logPath))
		}
		
		val process = processBuilder.start()
		
		if (diagnose) {
			process.waitFor(waitTime, TimeUnit.SECONDS)
			process.destroy()
		}
	}
	
}