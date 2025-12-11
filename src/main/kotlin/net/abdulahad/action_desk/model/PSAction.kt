package net.abdulahad.action_desk.model

data class PSAction(
	val name: String,
	val command: String,
	var isEncodedCommand: Boolean = true,
	var workingDirectory: String = "",
	var noExit: Boolean = false,
	var singleton: Boolean = false
)
