package net.abdulahad.action_desk.engine.adcd

import net.abdulahad.action_desk.lib.json.JsonValue
import kotlin.math.roundToInt

object DialogStyleParser {
	
	fun dimension(node: JsonValue, defaultValue: String): String {
		return when (val raw = node.raw()) {
			null -> defaultValue
			is Number -> raw.toInt().toString()
			is String -> raw
			else -> defaultValue
		}
	}
	
	fun nullableDimension(node: JsonValue): String? {
		return when (val raw = node.raw()) {
			null -> null
			is Number -> raw.toInt().toString()
			is String -> raw
			else -> null
		}
	}
	
	fun padding(node: JsonValue, defaultValue: DialogPadding): DialogPadding {
		if (node.isNull) {
			return defaultValue
		}
		
		val values = when (val raw = node.raw()) {
			is Number -> listOf(raw.toInt())
			
			is String -> raw
				.trim()
				.split(Regex("[\\s,]+"))
				.filter { it.isNotBlank() }
				.mapNotNull { paddingToken(it) }
			
			is List<*> -> raw.mapNotNull { item ->
				when (item) {
					is Number -> item.toInt()
					is String -> paddingToken(item)
					else -> null
				}
			}
			
			else -> emptyList()
		}
		
		return paddingValues(values, defaultValue)
	}
	
	fun nullablePadding(node: JsonValue): DialogPadding? {
		if (node.isNull) {
			return null
		}
		
		return padding(node, DialogPadding.zero())
	}
	
	fun isAutoSize(value: String?): Boolean {
		return value == null || value.trim().equals("auto", ignoreCase = true)
	}
	
	fun sizeValue(value: String?, screenAxisSize: Int, defaultValue: Int): Int {
		val cleanValue = value?.trim()?.lowercase() ?: return defaultValue
		
		if (cleanValue == "auto") {
			return defaultValue
		}
		
		if (cleanValue.endsWith("vh") || cleanValue.endsWith("vw")) {
			val number = cleanValue
				.dropLast(2)
				.trim()
				.toDoubleOrNull()
				?: return defaultValue
			
			return ((screenAxisSize * number) / 100.0).roundToInt()
		}
		
		if (cleanValue.endsWith("px")) {
			return cleanValue
				.dropLast(2)
				.trim()
				.toIntOrNull()
				?: defaultValue
		}
		
		return cleanValue.toIntOrNull() ?: defaultValue
	}
	
	private fun paddingToken(token: String): Int? {
		val clean = token
			.trim()
			.lowercase()
			.removeSuffix("px")
			.trim()
		
		return clean
			.toDoubleOrNull()
			?.roundToInt()
			?.coerceAtLeast(0)
	}
	
	private fun paddingValues(values: List<Int>, defaultValue: DialogPadding): DialogPadding {
		return when (values.size) {
			1 -> DialogPadding.all(values[0])
			
			2 -> DialogPadding(
				top = values[0],
				right = values[1],
				bottom = values[0],
				left = values[1]
			)
			
			3 -> DialogPadding(
				top = values[0],
				right = values[1],
				bottom = values[2],
				left = values[1]
			)
			
			4 -> DialogPadding(
				top = values[0],
				right = values[1],
				bottom = values[2],
				left = values[3]
			)
			
			else -> defaultValue
		}
	}
	
}
