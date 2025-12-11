package net.abdulahad.action_desk.lib.json

import java.util.*


class JsonValue(private val value: Any?) {
	
	fun path(key: String?): JsonValue {
		
		if (value is MutableMap<*, *>) {
			val `val` = value[key]
			return JsonValue(`val`)
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
		return (value as? String) ?: value.toString()
	}
	
	fun bool(defVal: Boolean): Boolean {
		if (value is Boolean) return value
		
		if (value is String) {
			return when (value.lowercase(Locale.getDefault())) {
				"true", "yes", "1" -> true
				"false", "no", "0" -> false
				else -> defVal
			}
		}
		
		if (value is Number) return value.toInt() != 0
		
		return defVal
	}
	
	fun int(defVal: Int): Int  {
		return when (value) {
			is Number -> value.toInt()
			is String -> value.toIntOrNull() ?: defVal
			else -> value?.toString()?.toIntOrNull() ?: defVal
		}
	}
	
	fun double(defVal: Double): Double {
		return (value as? Number)?.toDouble()
			?: value?.toString()?.toDoubleOrNull()
			?: defVal
	}
	
	fun list(): MutableList<JsonValue?> {
		return (value as? List<*>)?.map { JsonValue(it) }?.toMutableList()
			?: mutableListOf()
	}
	
	fun map(): Map<String, JsonValue> {
		return (value as? Map<*, *>)?.mapNotNull { (k, v) ->
			(k as? String)?.let { it to JsonValue(v) }
		}?.toMap() ?: emptyMap()
	}
	
	val isNull: Boolean
		get() = value == null
	
	fun raw(): Any? {
		return value
	}
}
