package net.abdulahad.action_desk.lib.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.abdulahad.action_desk.runtimeException
import java.io.File

open class JsonLoader<T> (protected val path: String, private val typeRef: TypeReference<T>) {
	
	protected var data: T? = null
	
	fun get(): T? {
		if (data == null) load()
		return data
	}
	
	private fun load() {
		try {
			val mapper = ObjectMapper()
			mapper.registerModule(KotlinModule.Builder().build())
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			data = mapper.readValue(File(path), typeRef)
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
			val mapper = ObjectMapper()
			mapper.writerWithDefaultPrettyPrinter().writeValue(File(path), data)
		} catch (e: Exception) {
			throw RuntimeException("Failed to save config: " + e.message, e)
		}
	}
	
}
