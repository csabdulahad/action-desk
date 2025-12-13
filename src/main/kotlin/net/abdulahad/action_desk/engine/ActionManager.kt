package net.abdulahad.action_desk.engine

import net.abdulahad.action_desk.model.ActionProcess
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.helper.ProcessHelper
import net.abdulahad.action_desk.model.Action
import java.io.File
import kotlin.math.abs

object ActionManager {
	
	fun getPIDLockFiles(actionId: Int): ActionProcess {
		val pidFolder = Env.getPIDFolder()
		
		val actionProcess = ActionProcess(actionId)
		
		File(pidFolder).listFiles { file -> file.extension == "txt" }?.forEach { file ->
			val nameWithoutExt = file.nameWithoutExtension
			val parts = nameWithoutExt.split("-")
			
			if (parts.size == 2) {
				val aId = parts[0].toIntOrNull()
				
				if (aId == null || aId != actionId) {
					return@forEach
				}
				
				
				val pid = try {
					val x = file.readText().trim()
					Regex("\\d+")
						.find(x)
						?.value
						?.replace("-", "")
						?.toLong()
				} catch (e: Exception) {
					val msg = "Failed reading PID lock file ${file.absolutePath}: ${e.message}"
					println(msg)
					App.logErr(msg)
					
					return@forEach
				}
				
				if (pid == null) return@forEach
				
				actionProcess.pidFiles[pid] = file
				actionProcess.insCount ++
			}
		}
		
		return actionProcess
	}
	
	fun readPIDFromLockFiles(appendActionId: Boolean = true, separator : String = ";"): List<String> {
		/*
		 * Read PID lock files & build action list
		 * */
		val folder = File("${Env.APP_FOLDER}/pid")
		if (!folder.exists() || !folder.isDirectory) return emptyList()
		
		val files = folder.listFiles { file ->
			file.isFile && file.extension == "txt"
		}?.map { it } ?: emptyList()
		
		
		/*
		 * Read PID from files
		 * */
		val pidAndFilePathList = mutableListOf<String>()
		
		files.forEach {
			val pid = ProcessHelper.readPIDFromFile(it)
			
			if (pid == null) {
				it.delete()
			} else {
				pidAndFilePathList.add("$pid;${it.absolutePath}")
			}
		}
		
		
		/*
		 * Build running process set
		 * */
		val process = ProcessBuilder("cmd", "/c", "tasklist /fo csv /nh").start()
		val output = process.inputStream.bufferedReader().readText()
		
		val runningPIDs = output
			.lineSequence()
			.filter { it.isNotBlank() }
			.mapNotNull { line ->
				val fields = line.split("\",\"")
				
				if (fields.size >= 2) {
					fields[1].replace("\"", "")
				} else null
			}
			.toSet()
		
		
		/*
		 * Filter pids which are running
		 * */
		return pidAndFilePathList.filter {
			val pid = it.substringBefore(";").replace("-", "")
			val alive = pid in runningPIDs
			
			if (!alive) {
				val path = it.substringAfter(";")
				val msg = "Cleaning dead PID ($pid) lock file: $path"
				
				println(msg)
				App.logInfo(msg)
				
				File(path).delete()
			}
			
			alive
		}.map {
			if (appendActionId) {
				val pid = it.substringBefore(";")
				val actionId = File(it.substringAfter(";"))
					.nameWithoutExtension
					.split("-")[0]
				"$pid$separator$actionId"
			} else {
				// PID only!
				it.substringBefore(";")
			}
		}
	}
	
	fun collectChildPIDs(parentPid: Long): List<Long> {
		val descendants = mutableListOf<Long>()
		
		val pid = abs(parentPid)
		
		ProcessHandle.of(pid).ifPresent { parent ->
			fun collect(handle: ProcessHandle) {
				handle.children().forEach { child ->
					descendants += child.pid()
					
					// Recursively collect child's children
					collect(child)
				}
			}
			
			collect(parent)
		}
		
		return descendants
	}
	
	fun isRunning(action: Action): Boolean {
		val actionProcess = getPIDLockFiles(action.id)
		val pidList: MutableSet<Long> = actionProcess.pidFiles.keys
		
		if (pidList.isEmpty()) return false
		
		val process = ProcessBuilder("cmd", "/c", "tasklist /fo csv /nh")
			.redirectErrorStream(true)
			.start()
		
		val pidRegex = Regex("^\"[^\"]+\",\"(\\d+)\"")
		val running = mutableSetOf<Long>()
		
		val lines = process.inputStream.bufferedReader().use { it.readLines() }
		
		lines.forEach { line ->
			pidRegex.find(line)
				?.groupValues?.getOrNull(1)
				?.toLongOrNull()
				?.let { running.add(it) }
		}
		
		val exitCode = process.waitFor()
		
		if (exitCode != 0) {
			val msg = "tasklist failed with code $exitCode"
			println(msg)
			App.logErr(msg)
			
			return false
		}
		
		val alivePIDs: Set<Long> = pidList.intersect(running)
		val deadPIDs = pidList.subtract(alivePIDs)
		
		deadPIDs.forEach { pid ->
			val file = actionProcess.pidFiles[pid] ?: return@forEach
			
			val path = file.absolutePath
			val msg = "Cleaning dead PID ($pid) lock file: $path"
			
			println(msg)
			App.logInfo(msg)
			
			file.delete()
		}
		
		return !alivePIDs.isEmpty()
	}
	
}
