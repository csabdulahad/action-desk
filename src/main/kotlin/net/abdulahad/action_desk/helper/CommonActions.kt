package net.abdulahad.action_desk.helper

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.data.AppValues
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.engine.executor.PSExecutor
import net.abdulahad.action_desk.lib.util.Alert
import net.abdulahad.action_desk.model.PSAction
import net.abdulahad.action_desk.view.ActionDesk
import java.io.File

object CommonActions {
	
	fun runPowerShell(script: String, elevated: Boolean = false, windowStyle: String = "Hidden"): String {
		val base64 = PSExecutor.encodeToBase64Utf16LE(script.trimIndent())
		
		val command = if (elevated) {
			// Use Start-Process to re-run pwsh with RunAs
			listOf(
				App.getPSBin(),
				"-NoProfile",
				"-NoLogo",
				"-NonInteractive",
				"-Command",
				"""Start-Process pwsh -ArgumentList '-NoProfile -NoLogo -NonInteractive -EncodedCommand $base64' -Verb RunAs -WindowStyle $windowStyle"""
			)
		} else {
			listOf(
				App.getPSBin(),
				"-NoProfile",
				"-NoLogo",
				"-NonInteractive",
				"-EncodedCommand",
				base64
			)
		}
		
		val pb = ProcessBuilder(command)
			.redirectErrorStream(true)
		
		val process = pb.start()
		val output = process.inputStream.bufferedReader().readText()
		process.waitFor()
		return output
	}
	
	fun openInNotepad(path: String) {
		App.logInfo("Opened file in notepad: $path")
		ProcessBuilder("notepad", path).start()
	}
	
	fun openFolder(path: String) {
		App.logInfo("Folder opened: $path")
		ProcessBuilder(App.getPSBin(), "-Command", "Start-Process", """"$path"""").start()
	}
	
	fun writeToFile(file: File, text: String, append: Boolean = false) {
		file.writeText(
			if (append && file.exists()) file.readText() + text else text
		)
	}
	
	fun restartActionDesk() {
		val jarActionDesk = ProcessHelper.getActionDeskJarFile()
		
		if (jarActionDesk == null) {
			val msg = "Can't restart ActionDesk when it started from IDE (Dev Env)"
			println(msg)
			App.logWarn(msg)
			
			Alert.confirm(msg).title("Restart ActionDesk").show(ActionDesk)
			
			return
		}
		
		val pid = ProcessHelper.getCurrentADPID()
		val javaWin = Env.getJavaWin()
		
		val cmd = """
		    Stop-Process $pid
		    Start-Process -FilePath '$javaWin' -ArgumentList '-jar "$jarActionDesk"'
		""".trimIndent()
		
		println(cmd)
		
		val base64 = PSExecutor.encodeToBase64Utf16LE(cmd)
		
		val powershellCmd = listOf(
			App.getPSBin(),
			"-NoProfile",
			"-NoLogo",
			"-NonInteractive",
			"-EncodedCommand",
			base64
		)
		
		ProcessBuilder(powershellCmd)
			.redirectErrorStream(true)
			.redirectOutput(File("c:/users/ahad/desktop/meh.txt"))
			.start()
	}
	
	fun actionDeskShortcut(copyTo: String? = null) {
		if (File(AppValues.ACTION_DESK_LNK).exists() && copyTo == null) {
			return
		}
		
		var jarPath = ProcessHelper.getActionDeskJarFile()
		
		// TODO - should never happen on live, only dev!
		if (jarPath == null) {
			jarPath = File("c:/users/ahad/desktop/action_desk.jar")
		}
		
		val command = """
			. ${AppValues.CREATE_SHORTCUT_LNK};
			Create-Shortcut -ShortcutPath "${AppValues.ACTION_DESK_LNK}" `
			-TargetPath "${jarPath.absolutePath}" `
			-IconLocation "${AppValues.ACTION_DESK_ICON_ICO}"
		"""
		
		val action = PSAction("meh", command, singleton = true)
		
		PSExecutor.execute(action) { code, status ->
			if (code != 0 || !status) {
				val msg = "ActionDesk shortcut creation failed, exited code = $code with normal termination = $status"
				App.logErr(msg)
				
				return@execute
			}
			
			if (copyTo == null) return@execute
			
			val path = "$copyTo/ActionDesk.lnk"
			
			if (File(path).exists()) {
				App.logInfo("ActionDesk shortcut already exits at: $path")
				return@execute
			}
			
			ResourceHelper.copyFile(AppValues.ACTION_DESK_LNK, path)
			App.logInfo("ActionDesk shortcut created at: $path")
		}
	}
	
}