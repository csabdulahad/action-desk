package net.abdulahad.action_desk.engine.security

import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.lib.json.Json
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.SecretKey

object MasterKeyStore {
	private const val MASTER_KEY_LENGTH_BYTES = 32
	
	private val random = SecureRandom()
	
	fun hasWrappedMasterKey(): Boolean {
		return AppConfig.getSecurityMasterKeyEnvelope().isNotBlank()
	}
	
	fun createAndStore(password: String) {
		if (hasWrappedMasterKey()) {
			throw PasswordAlreadyConfiguredException()
		}
		
		val masterKey = newMasterKey()
		
		try {
			val envelope = wrapMasterKey(masterKey, password)
			AppConfig.setSecurityMasterKeyEnvelope(Json.stringify(envelope))
		} finally {
			masterKey.fill(0)
		}
	}
	
	fun unwrap(password: String): ByteArray {
		val envelope = readEnvelope()
		val wrappingKey = deriveWrappingKey(password, envelope)
		
		return try {
			AesGcm.decrypt(envelope.iv, envelope.ciphertext, wrappingKey)
		} catch (e: AEADBadTagException) {
			throw InvalidPasswordException(e)
		} catch (e: Exception) {
			throw InvalidPasswordException(e)
		}
	}
	
	fun verify(password: String): Boolean {
		return try {
			val masterKey = unwrap(password)
			masterKey.fill(0)
			true
		} catch (_: InvalidPasswordException) {
			false
		} catch (_: PasswordNotConfiguredException) {
			false
		}
	}
	
	fun rewrap(oldPassword: String, newPassword: String) {
		val masterKey = unwrap(oldPassword)
		
		try {
			val envelope = wrapMasterKey(masterKey, newPassword)
			AppConfig.setSecurityMasterKeyEnvelope(Json.stringify(envelope))
		} finally {
			masterKey.fill(0)
		}
	}
	
	private fun readEnvelope(): CryptoEnvelope {
		val json = AppConfig.getSecurityMasterKeyEnvelope()
		
		if (json.isBlank()) {
			throw PasswordNotConfiguredException()
		}
		
		return Json.mapper.readValue(json, CryptoEnvelope::class.java)
	}
	
	private fun wrapMasterKey(masterKey: ByteArray, password: String): CryptoEnvelope {
		val salt = PasswordKeyDeriver.newSaltBase64()
		val wrappingKey = PasswordKeyDeriver.deriveKey(password, salt)
		val encrypted = AesGcm.encrypt(masterKey, wrappingKey)
		
		return CryptoEnvelope(
			version = 1,
			algorithm = "AES-GCM",
			kdf = PasswordKeyDeriver.ALGORITHM,
			iterations = PasswordKeyDeriver.DEFAULT_ITERATIONS,
			keyLengthBits = PasswordKeyDeriver.DEFAULT_KEY_LENGTH_BITS,
			salt = salt,
			iv = encrypted.first,
			ciphertext = encrypted.second
		)
	}
	
	private fun deriveWrappingKey(password: String, envelope: CryptoEnvelope): SecretKey {
		if (envelope.version != 1) {
			throw ActionDeskSecurityException("Unsupported security envelope version: ${envelope.version}")
		}
		
		if (envelope.algorithm != "AES-GCM") {
			throw ActionDeskSecurityException("Unsupported encryption algorithm: ${envelope.algorithm}")
		}
		
		if (envelope.kdf != PasswordKeyDeriver.ALGORITHM) {
			throw ActionDeskSecurityException("Unsupported password key derivation algorithm: ${envelope.kdf}")
		}
		
		return PasswordKeyDeriver.deriveKey(
			password = password,
			saltBase64 = envelope.salt,
			iterations = envelope.iterations,
			keyLengthBits = envelope.keyLengthBits
		)
	}
	
	private fun newMasterKey(): ByteArray {
		val masterKey = ByteArray(MASTER_KEY_LENGTH_BYTES)
		random.nextBytes(masterKey)
		return masterKey
	}
	
}
