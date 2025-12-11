package net.abdulahad.action_desk.helper

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.lib.util.Poth
import net.abdulahad.action_desk.runtimeException
import java.io.File
import java.net.JarURLConnection

object ResourceHelper {
	
	fun createFolder(path: String, exception: Boolean = true) {
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
	
	fun createFile(path: String, exception: Boolean = true, initContent: ((path: String) -> Unit)? = null): Int {
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
	
	fun copyFile(from: String, to: String, override: Boolean = false, exception: Boolean = true): Int {
		val src = File(from)
		val dest = File(to)
		
		if (!src.exists() || !src.isFile) {
			val msg = "Source file does not exist: $from"
			App.debug(msg)
			
			return -1
		}
		
		// Create parent folder for destination
		createFolder(dest.parent, exception)
		
		if (dest.exists() && !override) {
			App.debug("File already exists, no override: $to")
			return 0
		}
		
		try {
			src.copyTo(dest, overwrite = override)
		} catch (e: Exception) {
			val msg = "Failed to copy file $from -> $to. Error: ${e.message}"
			App.debug(msg)
			
			return -1
		}
		
		App.debug("File copied: $from -> $to")
		return 1
	}
	
	fun copyResourceFile(resourceName: String, targetPath: String, override: Boolean = false): Boolean {
		val resourceStream = Poth.getAsStream(resourceName) ?: return false
		
		val targetFile = File(targetPath)
		
		if (!override && targetFile.exists()) {
			return false
		}
		
		// Ensure parent directory exists
		targetFile.parentFile?.mkdirs()
		
		resourceStream.use { input ->
			targetFile.outputStream().use { output ->
				input.copyTo(output)
			}
		}
		
		return true
	}
	
	fun copyResourceFolder(resourcePath: String, targetDir: String, override: Boolean = false): Boolean {
		val url = ResourceHelper::class.java.classLoader.getResource(resourcePath)
			?: return false
		
		val target = File(targetDir)
		
		if (!target.exists()) target.mkdirs()
		
		val protocol = url.protocol
		
		if (protocol == "file") {
			println("Copying $resourcePath")
			// Development mode (running from classes, not JAR)
			val sourceFile = File(url.toURI())
			
			if (sourceFile.isDirectory) {
				sourceFile.walkTopDown().forEach { src ->
					if (src.isFile) {
						val relative = src.relativeTo(sourceFile).path
						val destFile = File(target, relative)
						copyResourceFile(src.readBytes(), destFile, override)
					}
				}
			} else {
				val destFile = File(target, sourceFile.name)
				copyResourceFile(sourceFile.readBytes(), destFile, override)
			}
			return true
		}
		
		// Packaged JAR resources
		val jarConnection = url.openConnection() as JarURLConnection
		val jarFile = jarConnection.jarFile
		
		jarFile.entries().asSequence()
			.filter { it.name.startsWith(resourcePath) && !it.isDirectory }
			.forEach { entry ->
				val relative = entry.name.removePrefix(resourcePath).trimStart('/')
				val destFile = File(target, relative)
				jarFile.getInputStream(entry).use { input ->
					copyResourceFile(input.readBytes(), destFile, override)
				}
			}
		
		
		return true
	}
	
	private fun copyResourceFile(bytes: ByteArray, destFile: File, override: Boolean) {
		if (destFile.exists() && !override) return
		
		destFile.parentFile?.mkdirs()
		destFile.writeBytes(bytes)
	}
	
}
