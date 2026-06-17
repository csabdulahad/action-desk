package net.abdulahad.action_desk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.swing.SwingUtilities
import javax.swing.Timer

/*
 * Thread helper functions
 * */

// Use a SupervisorJob so if one task fails, the whole scope doesn't die
val AppScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


/**
 * For Database and File I/O.
 * Reuses the AppScope but switches the worker thread to IO.
 */
fun onIO(runnable: suspend () -> Unit) {
	AppScope.launch(Dispatchers.IO) {
		runnable()
	}
}

/**
 * Uses the immediate dispatcher to update the UI
 * instantly if already on the EDT.
 */
fun onUI (runnable: suspend () -> Unit) {
	AppScope.launch {
		runnable()
	}
}

/**
 * For heavy CPU math or sorting.
 */
fun onDefault(runnable: suspend () -> Unit) {
	AppScope.launch(Dispatchers.Default) {
		runnable()
	}
}

fun onSwing (runnable: () -> Unit) {
	SwingUtilities.invokeLater {
		runnable()
	}
}

fun runDelayOnUI(delay: Float, runnable: () -> Unit) {
	Timer((delay * 1000).toInt()) {
		runnable()
	}.apply {
		isRepeats = false
		start()
	}
}

fun onUIDelayed(delay: Float, runnable: () -> Unit) = runDelayOnUI(delay, runnable)

fun runtimeException(msg: String, exception: Boolean = true) {
	try {
		App.logErr(msg)
	} catch (_: Exception) {}
	
	if (exception) throw RuntimeException(msg)
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