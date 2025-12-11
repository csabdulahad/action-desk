package net.abdulahad.action_desk.data

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.helper.ResourceHelper
import net.abdulahad.action_desk.lib.json.JsonObjectLoader
import net.abdulahad.action_desk.lib.util.Poth
import net.abdulahad.action_desk.lib.util.Pref
import net.abdulahad.action_desk.normalizeSlashes
import net.abdulahad.action_desk.runtimeException
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.*

object Env {
	
	private lateinit var appFolder: String
	val APP_FOLDER: String
		get() = appFolder
	
	lateinit var config: JsonObjectLoader
	val CONFIG: JsonObjectLoader
		get() = config
	
	fun printSystemProperties() {
		val path = System.getProperties()
		
		for (key in path.keys) {
			val value = path[key]
			println("$key: $value")
		}
	}
	
	fun getJavaWin(): String {
		// e.g., C:\Program Files\Java\jdk-22
		val javaHome = System.getProperty("java.home")
		return File(javaHome, "bin/javaw.exe").absolutePath
	}
	
	fun getUserHomeDir(): String {
		return System.getProperty("user.home")
	}
	
	fun getUserName(): String {
		return System.getProperty("user.name")
	}
	
	fun getSystemPath(): Array<String?>? {
		val path = System.getenv("PATH") ?: return null
		
		return path.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
	}
	
	fun getOS(): String {
		return System.getProperty("os.name").lowercase(Locale.getDefault())
	}
	
	fun getLogFolder(): String {
		return "$APP_FOLDER/logs"
	}
	
	fun getPIDFolder(): String {
		return "$APP_FOLDER/pid"
	}
	
	fun init(args: Map<String, String>) {
		var folder: String? = null
		
		val cmlAppFolder  = args["app-folder"]
		val prefAppFolder = Pref.getString("ad_app_folder", null)
		
		// #1 - Prefer --app-folder flag
		if (cmlAppFolder != null) {
			folder = cmlAppFolder
			App.debug("Using cmd line --app-folder: $folder")
		}
		
		// #2 - Check if any location was saved in pref
		if (folder == null && prefAppFolder != null) {
			folder = prefAppFolder
			App.debug("Using pref --app-folder: $folder")
		}
		
		// #3 - Fallback if the above two locations fail
		if (folder == null) {
			folder = getUserHomeDir()
			App.debug("Using fallback --app-folder: $folder")
		}
		
		
		// Cache the location into pref, if not set!
		appFolder = folder.normalizeSlashes()
		
		if (prefAppFolder == null) {
			Pref.putString("ad_app_folder", appFolder)
			App.debug("Action Desk app folder saved into pref: $appFolder")
		}
		
		appFolder = "$appFolder/${App.CODE_NAME}"
		setupAppFolders(appFolder)
		
		val logPath = "$appFolder/logs"
		System.setProperty("app.log.dir", logPath)
		
		val configPath = "$appFolder/config.json"
		loadConfig(configPath)
		
		checkDB("$appFolder/action_desk.db")
		
		createFolder("$appFolder/icons")
		createFile("$appFolder/icons.txt")
		
		ResourceHelper.copyResourceFile("config/action_desk.ico", "$appFolder/action_desk.ico")
		ResourceHelper.copyResourceFolder("scripts", "$appFolder/scripts")
	}
	
	private fun getDefaultConfigJSON(): String {
		val stream = Poth.getAsStream("config/config.json")
		val json = stream?.bufferedReader()?.use { it.readText() }
		
		if (json == null) {
			runtimeException("Failed to clone config.json", true)
			return ""
		}
		
		return json.trimIndent()
	}
	
	private fun loadConfig(configPath: String) {
		createFile(configPath) { file ->
			writeToFile(file, getDefaultConfigJSON())
		}
		
		config = JsonObjectLoader(configPath)
	}
	
	private fun setupAppFolders(path: String) {
		createFolder("$path/")
		createFolder("$path/logs/")
		createFolder("$path/pid/")
	}
	
	private fun checkDB(path: String) {
		val folder = File(path)
		
		if (!folder.exists()) {
			val inputStream = Poth.getAsStream("config/action_desk.db")
			
			if (inputStream == null)
				runtimeException("Couldn't clone db to: $path")
			
			FileOutputStream(path).use { outputStream ->
				inputStream.use { input ->
					input?.copyTo(outputStream)
				}
			}
		}
		
		DB.init(path)
	}
	
	private fun createFolder(path: String, exception: Boolean = true) {
		val folder = File(path)
		
		if (folder.exists()) return
		
		folder.mkdirs()
		
		if (!folder.exists()) {
			val msg = "Failed to create folder: $path"
			App.debug(msg)
			runtimeException(msg, exception)
			
			return
		}
		
		App.debug("Folder created: $path")
		
		/*
		 * Make sure we have rwx permission on the folder!
		 * */
		if (!folder.canExecute() || !folder.canWrite() || !folder.canRead()) {
			val msg = "Folder missing required permissions (rwx): $path"
			App.debug(msg)
			runtimeException(msg, exception)
		}
	}
	
	private fun createFile(path: String, exception: Boolean = true, initContent: ((path: String) -> Unit)? = null): Int {
		val file = File(path)
		
		if (file.exists()) return 0
		
		// Create the parent folders if needed
		createFolder(file.parent)
		
		file.createNewFile()
		
		if (!file.exists()) {
			val msg = "Failed to create file: $path"
			App.debug(msg)
			
			runtimeException(msg, exception)
			return -1
		}
		
		App.debug("File created: $path")
		
		if (initContent == null) return 1
		
		initContent(path)
		return 1
	}
	
	private fun writeToFile(path: String, content: String, exception: Boolean = true) {
		try {
			val writer = FileWriter(path)
			writer.write(content)
			writer.flush()
			writer.close()
		} catch (e: Exception) {
			val msg = "Failed to write to: $path\nCause: ${e.message}"
			runtimeException(msg, exception)
		}
	}
	
}
