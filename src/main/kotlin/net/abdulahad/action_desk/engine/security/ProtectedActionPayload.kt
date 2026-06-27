package net.abdulahad.action_desk.engine.security

/**
 * Sensitive action execution data that should not remain readable in the DB
 * once an action is password protected.
 */
data class ProtectedActionPayload(
	val command: String = "",
	val arguments: String = "",
	val startDirectory: String = ""
)
