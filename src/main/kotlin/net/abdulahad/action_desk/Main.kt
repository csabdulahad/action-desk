package net.abdulahad.action_desk

import javax.swing.SwingUtilities

fun runtimeException(msg: String, exception: Boolean = true) {
	try {
		App.logErr(msg)
	} catch (_: Exception) {}
	
	if (exception) throw RuntimeException(msg)
}

fun onUI(runnable: () -> Unit) {
	SwingUtilities.invokeLater {
		runnable()
	}
}

fun parseArgs(args: Array<String>): Map<String, String> {
	val map = mutableMapOf<String, String>()
	var i 	= 0
	
	while (i < args.size) {
		val arg = args[i]
		
		if (arg.startsWith("--")) {
			val key   = arg.removePrefix("--")
			val value = args.getOrNull(i + 1)?.takeUnless { it.startsWith("--") } ?: "true"
			
			map[key] = value
			
			i += if (value == "true") 1 else 2
		} else {
			i++
		}
	}
	
	return map
}

fun String.normalizeSlashes(): String = this.replace('\\', '/')

fun String.capitalizeFirst(): String {
	return this.replaceFirstChar {
		if (it.isLowerCase()) it.titlecase() else it.toString()
	}
}

fun main(args: Array<String>) {
	val argsMap = parseArgs(args)
	Bootstrap.kickoff(argsMap)
}