package net.abdulahad.action_desk.engine.executor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.model.PSAction
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object PSExecutor {
	
	private val runningActionList = mutableSetOf<String>()
	private val psBin = App.getPSBin()
	
	// PowerShell requires Base64 encoded string in UTF-16LE for -EncodedCommand
	fun encodeToBase64Utf16LE(command: String): String {
		val bytes = command.toByteArray(Charsets.UTF_16LE)
		return Base64.getEncoder().encodeToString(bytes)
	}
	
	private fun buildCommand(psAction: PSAction): List<String> {
		val powershellCmd = mutableListOf<String>()
		
		powershellCmd.add(psBin)
		
		if (psAction.workingDirectory.isNotEmpty()) {
			powershellCmd.add("-WorkingDirectory")
			powershellCmd.add("'${psAction.workingDirectory}'")
		}
		
		if (psAction.noExit) {
			powershellCmd.add("-NoExit")
		}
		
		// Window Style
		powershellCmd.add("-WindowStyle")
		powershellCmd.add("Hidden")
		
		/*
		 * Encode command to avoid quote escaping mess!
		 * */
		var args = psAction.command
			.trimIndent()
			.lines()
			.map { it.trim() }
			.filter { it.isNotEmpty() }
			.joinToString(" ")
		
		if (psAction.isEncodedCommand) {
			powershellCmd.add("-ec")
			args = encodeToBase64Utf16LE(args)
			powershellCmd.add(args)
		} else {
			powershellCmd.add("'${args}'")
		}
		
		return powershellCmd.toList()
	}
	
	fun execute(
		psAction: PSAction,
		waitTime: Long = 60,
		onFinish: ((exitCode: Int, normalTermination: Boolean) -> Unit)? = null
	): Boolean {
		
		if (psAction.singleton && !runningActionList.add(psAction.name)) {
			return false
		}
		
		CoroutineScope(Dispatchers.IO).launch {
			val cmd = buildCommand(psAction)
			println(cmd.joinToString(" "))
			
			val processBuilder =
				ProcessBuilder(cmd)
				.redirectErrorStream(true)
				// TODO - remove this
				.redirectOutput(File("c:/users/ahad/desktop/meh.txt"))
			
			if (psAction.workingDirectory.isNotEmpty()) {
				processBuilder.directory(File(psAction.workingDirectory))
			}
			
			val process = processBuilder.start()
			val termination = process.waitFor(waitTime, TimeUnit.SECONDS)
			
			if (psAction.singleton) {
				runningActionList.remove(psAction.name)
			}
			
			if (onFinish != null) {
				onFinish(process.exitValue(), termination)
			}
		}
		
		return true
	}

}