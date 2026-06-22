package net.abdulahad.action_desk.lib.view

import java.awt.Color

object ColorUtil {
	
	fun parse(value: String?): Color? {
		val clean = value
			?.trim()
			?.takeIf { it.isNotBlank() }
			?: return null
		
		return try {
			if (clean.startsWith("#")) {
				parseHex(clean)
			} else {
				Color.decode(clean)
			}
		} catch (_: Exception) {
			null
		}
	}
	
	private fun parseHex(value: String): Color? {
		val hex = value.removePrefix("#")
		
		return when (hex.length) {
			3 -> {
				val r = "${hex[0]}${hex[0]}".toInt(16)
				val g = "${hex[1]}${hex[1]}".toInt(16)
				val b = "${hex[2]}${hex[2]}".toInt(16)
				
				Color(r, g, b)
			}
			
			6 -> {
				val r = hex.substring(0, 2).toInt(16)
				val g = hex.substring(2, 4).toInt(16)
				val b = hex.substring(4, 6).toInt(16)
				
				Color(r, g, b)
			}
			
			8 -> {
				val a = hex.substring(0, 2).toInt(16)
				val r = hex.substring(2, 4).toInt(16)
				val g = hex.substring(4, 6).toInt(16)
				val b = hex.substring(6, 8).toInt(16)
				
				Color(r, g, b, a)
			}
			
			else -> null
		}
	}
	
}
