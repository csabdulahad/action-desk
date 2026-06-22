package net.abdulahad.action_desk.lib.json

import java.util.*

class JsonValue(private val value: Any?) {
	
	fun path(key: String?): JsonValue {
		if (key == null) return JsonValue(null)
		
		if (value is Map<*, *>) {
			return JsonValue(value[key])
		}
		
		return JsonValue(null)
	}
	
	fun index(i: Int): JsonValue {
		val list = value as? List<*>
		return JsonValue(list?.getOrNull(i))
	}
	
	fun first(): JsonValue = JsonValue((value as? List<*>)?.firstOrNull())
	
	fun last(): JsonValue = JsonValue((value as? List<*>)?.lastOrNull())
	
	fun string(defVal: String? = null): String? {
		if (value == null) return defVal
		
		return (value as? String) ?: value.toString()
	}
	
	fun bool(defVal: Boolean): Boolean {
		return boolOrNull() ?: defVal
	}
	
	fun boolOrNull(): Boolean? {
		if (value is Boolean) return value
		
		if (value is String) {
			return when (value.trim().lowercase(Locale.getDefault())) {
				"true", "yes", "1" -> true
				"false", "no", "0" -> false
				else -> null
			}
		}
		
		if (value is Number) {
			return value.toInt() != 0
		}
		
		return null
	}
	
	fun int(defVal: Int): Int {
		return intOrNull() ?: defVal
	}
	
	fun intOrNull(): Int? {
		return when (value) {
			is Number -> value.toInt()
			is String -> value.trim().toIntOrNull()
			else -> value?.toString()?.trim()?.toIntOrNull()
		}
	}
	
	fun double(defVal: Double): Double {
		return doubleOrNull() ?: defVal
	}
	
	fun doubleOrNull(): Double? {
		return when (value) {
			is Number -> value.toDouble()
			is String -> value.trim().toDoubleOrNull()
			else -> value?.toString()?.trim()?.toDoubleOrNull()
		}
	}
	
	fun float(defVal: Float): Float {
		return floatOrNull() ?: defVal
	}
	
	fun floatOrNull(): Float? {
		return doubleOrNull()?.toFloat()
	}
	
	fun list(): List<JsonValue> {
		return (value as? List<*>)?.map { JsonValue(it) } ?: emptyList()
	}
	
	fun map(): Map<String, JsonValue> {
		return (value as? Map<*, *>)?.mapNotNull { (k, v) ->
			(k as? String)?.let { it to JsonValue(v) }
		}?.toMap() ?: emptyMap()
	}
	
	fun stringList(defVal: List<String> = emptyList(), splitCommaString: Boolean = false): List<String> {
		if (value == null) return defVal
		
		if (value is String) {
			if (splitCommaString) {
				return value
					.split("\\s*,\\s*".toRegex())
					.filter { it.isNotEmpty() }
			}
			
			return listOf(value)
		}
		
		if (value is List<*>) {
			return value.mapNotNull {
				when (it) {
					null -> null
					is String -> it
					is Number -> it.toString()
					is Boolean -> it.toString()
					else -> null
				}
			}
		}
		
		return defVal
	}
	
	val isNull: Boolean
		get() = value == null
	
	val isList: Boolean
		get() = value is List<*>
	
	val isMap: Boolean
		get() = value is Map<*, *>
	
	fun raw(): Any? {
		return value
	}
	
}
