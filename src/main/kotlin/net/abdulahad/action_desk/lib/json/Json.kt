package net.abdulahad.action_desk.lib.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

object Json {
	
	val mapper: ObjectMapper = ObjectMapper().apply {
		registerModule(KotlinModule.Builder().build())
		disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
	}
	
	fun parse(json: String): JsonValue {
		val value = mapper.readValue(
			json,
			object : TypeReference<Any?>() {}
		)
		
		return JsonValue(value)
	}
	
	fun parseObject(json: String): JsonValue {
		val root = parse(json)
		
		if (!root.isMap) {
			throw IllegalArgumentException("Root JSON value must be an object")
		}
		
		return root
	}
	
	fun stringify(value: Any?): String {
		return mapper.writeValueAsString(value)
	}
	
	fun stringifyPretty(value: Any?): String {
		return mapper
			.writerWithDefaultPrettyPrinter()
			.writeValueAsString(value)
	}
	
	fun <T> readFile(path: String, typeRef: TypeReference<T>): T {
		return mapper.readValue(File(path), typeRef)
	}
	
	fun writeFile(path: String, value: Any?, pretty: Boolean = true) {
		val writer = if (pretty) {
			mapper.writerWithDefaultPrettyPrinter()
		} else {
			mapper.writer()
		}
		
		writer.writeValue(File(path), value)
	}
	
}