package net.abdulahad.action_desk.lib.json

import com.fasterxml.jackson.core.type.TypeReference
import net.abdulahad.action_desk.runtimeException

open class JsonLoader<T>(protected val path: String, private val typeRef: TypeReference<T>) {
	
	protected var data: T? = null
	
	fun get(): T? {
		if (data == null) load()
		return data
	}
	
	private fun load() {
		try {
			data = Json.readFile(path, typeRef)
		} catch (e: Exception) {
			runtimeException("Failed to parse JSON file at: $path\nCause: ${e.message}")
		}
	}
	
	fun reload() {
		data = null
		load()
	}
	
	fun save() {
		try {
			Json.writeFile(path, data)
		} catch (e: Exception) {
			throw RuntimeException("Failed to save config: " + e.message, e)
		}
	}
	
}