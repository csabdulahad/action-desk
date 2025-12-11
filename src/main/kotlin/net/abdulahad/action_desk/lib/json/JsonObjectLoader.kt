package net.abdulahad.action_desk.lib.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*


class JsonObjectLoader(path: String) : JsonLoader<MutableMap<String, Any>> (path, object : TypeReference<MutableMap<String, Any>>() {}) {

	companion object {
		private val mapper = ObjectMapper()
	}
	
	private fun getNestedValue(key: String): Any? {
		val map = get()
		val parts: Array<String?> = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		
		var current: Any? = map
		
		for (part in parts) {
			if (current !is MutableMap<*, *>) return null
			
			current = current[part]
			
			if (current == null) return null
		}
		
		return current
	}
	
	fun getString(key: String, defVal: String? = null): String? {
		val value = getNestedValue(key)
		return value as? String ?: (value?.toString() ?: defVal)
	}
	
	fun getInt(key: String, defVal: Int? = null): Int? {
		val value = getNestedValue(key)
		return value as? Int ?: defVal
	}
	
	fun getDouble(key: String, defVal: Double? = null): Double? {
		val value = getNestedValue(key)
		return value as? Double ?: defVal
	}
	
	fun getRawList(key: String): List<Any?>? {
		val value = getNestedValue(key)
		return value as? List<*>
	}
	
	fun getList(key: String, defVal: MutableList<String>? = null): MutableList<String>? {
		val value = getNestedValue(key)
		
		if (value is String) {
			return mutableListOf(
				*value.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }
				.toTypedArray())
		}
		
		if (value is List<*>) {
			return value.mapNotNull { it?.toString() }.toMutableList()
		}
		
		return defVal
	}
	
	fun path(key: String): JsonValue {
		return JsonValue(getNestedValue(key))
	}
	
	@Suppress("UNCHECKED_CAST")
	fun set(key: String, value: Any) {
		val map = get() ?: return
		val parts = key.split('.').filter { it.isNotEmpty() }
		
		var current = map
		
		for (i in 0 until parts.size - 1) {
			val part = parts[i]
			var next = current[part]
			
			if (next !is MutableMap<*, *>) {
				next = LinkedHashMap<String, Any>()
				current.put(part, next)
			}
			
			current = next as MutableMap<String, Any>
		}
		
		current[parts.last()] = value
	}
	
	@Suppress("UNCHECKED_CAST")
	fun setStrict(key: String, value: Any) {
		val map = get() ?: return
		val parts = key.split('.').filter { it.isNotEmpty() }
		
		var current = map
		
		for (i in 0 until parts.size - 1) {
			val part = parts[i]
			current = current.getOrPut(part) { mutableMapOf<String, Any>() }
				as? MutableMap<String, Any>
				?: error("Cannot set value at '$part': not a mutable map")
		}
		
		current[parts.last()] = value
	}
	
}
