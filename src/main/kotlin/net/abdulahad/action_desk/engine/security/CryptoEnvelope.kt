package net.abdulahad.action_desk.engine.security

/**
 * JSON-stored wrapper for encrypted security material.
 *
 * For the first security phase this stores the encrypted master key.
 * Java AES/GCM stores the authentication tag at the end of the ciphertext bytes.
 */
data class CryptoEnvelope(
	val version: Int = 1,
	val algorithm: String = "AES-GCM",
	val kdf: String = "PBKDF2WithHmacSHA256",
	val iterations: Int = PasswordKeyDeriver.DEFAULT_ITERATIONS,
	val keyLengthBits: Int = PasswordKeyDeriver.DEFAULT_KEY_LENGTH_BITS,
	val salt: String = "",
	val iv: String = "",
	val ciphertext: String = ""
)
