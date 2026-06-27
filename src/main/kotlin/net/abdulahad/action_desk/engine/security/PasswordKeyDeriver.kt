package net.abdulahad.action_desk.engine.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object PasswordKeyDeriver {
	
	const val ALGORITHM = "PBKDF2WithHmacSHA256"
	const val DEFAULT_ITERATIONS = 600_000
	const val DEFAULT_KEY_LENGTH_BITS = 256
	const val SALT_LENGTH_BYTES = 16
	
	private val random = SecureRandom()
	
	fun newSaltBase64(): String {
		val salt = ByteArray(SALT_LENGTH_BYTES)
		random.nextBytes(salt)
		return Base64.getEncoder().encodeToString(salt)
	}
	
	fun deriveKey(
		password: String,
		saltBase64: String,
		iterations: Int = DEFAULT_ITERATIONS,
		keyLengthBits: Int = DEFAULT_KEY_LENGTH_BITS
	): SecretKey {
		val passwordChars = password.toCharArray()
		
		try {
			val salt = Base64.getDecoder().decode(saltBase64)
			val spec = PBEKeySpec(passwordChars, salt, iterations, keyLengthBits)
			val factory = SecretKeyFactory.getInstance(ALGORITHM)
			val keyBytes = factory.generateSecret(spec).encoded
			
			return SecretKeySpec(keyBytes, "AES")
		} finally {
			passwordChars.fill('\u0000')
		}
	}
	
}
