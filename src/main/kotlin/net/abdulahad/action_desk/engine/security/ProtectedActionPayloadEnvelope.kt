package net.abdulahad.action_desk.engine.security

/**
 * JSON-stored AES-GCM envelope for a protected action payload.
 *
 * This is encrypted directly with the Action Desk master key, so it does not
 * contain password KDF metadata. The password only unwraps the master key.
 */
data class ProtectedActionPayloadEnvelope(
	val version: Int = 1,
	val algorithm: String = "AES-GCM",
	val iv: String = "",
	val ciphertext: String = ""
)
