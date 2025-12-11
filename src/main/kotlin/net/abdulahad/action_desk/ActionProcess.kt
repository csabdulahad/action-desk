package net.abdulahad.action_desk

import java.io.File

data class ActionProcess(
	val actionId: Int,
	val pidFiles: MutableMap<Long, File> = mutableMapOf(),
	var insCount: Int = 0
)
