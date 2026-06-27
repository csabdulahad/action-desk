package net.abdulahad.action_desk.engine.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AesGcm {
	
	private const val TRANSFORMATION = "AES/GCM/NoPadding"
	private const val TAG_LENGTH_BITS = 128
	private const val IV_LENGTH_BYTES = 12
	
	private val random = SecureRandom()
	
	fun encrypt(plainBytes: ByteArray, key: SecretKey): Pair<String, String> {
		val iv = ByteArray(IV_LENGTH_BYTES)
		random.nextBytes(iv)
		
		val cipher = Cipher.getInstance(TRANSFORMATION)
		cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
		
		val ciphertext = cipher.doFinal(plainBytes)
		
		return Pair(
			Base64.getEncoder().encodeToString(iv),
			Base64.getEncoder().encodeToString(ciphertext)
		)
	}
	
	fun decrypt(ivBase64: String, ciphertextBase64: String, key: SecretKey): ByteArray {
		val iv = Base64.getDecoder().decode(ivBase64)
		val ciphertext = Base64.getDecoder().decode(ciphertextBase64)
		
		val cipher = Cipher.getInstance(TRANSFORMATION)
		cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
		
		return cipher.doFinal(ciphertext)
	}
	
}
