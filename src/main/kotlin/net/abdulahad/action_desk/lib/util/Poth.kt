package net.abdulahad.action_desk.lib.util

import java.io.File
import java.io.InputStream
import java.net.URISyntaxException
import java.net.URL

object Poth {
	
	fun path(vararg path: String?): String {
		return path.filterNot { it.isNullOrBlank() }.joinToString("/")
	}
	
	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
	fun getURL(path: String): URL {
		val normalizedPath = if (path.startsWith("/")) path else "/$path"
		return this.javaClass.getResource(normalizedPath)
	}
	
	fun fileExists(path: String): Boolean {
		return File(path).exists()
	}
	
	fun resourceExists(path: String): Boolean {
		return try {
			val normalizedPath = if (path.startsWith("/")) path else "/$path"
			this.javaClass.getResource(normalizedPath) != null
		} catch (ex: Exception) {
			false
		}
	}
	
	fun getPath(path: String): String {
		return try {
			getURL(path).toURI()?.toString() ?: ""
		} catch (e: URISyntaxException) {
			""
		}
	}
	
	fun getAsStream(path: String): InputStream? {
		var path = path
		if (!path.startsWith("/")) path = "/$path"
		
		return this.javaClass.getResourceAsStream(path)
	}
	
}