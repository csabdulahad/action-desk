package net.abdulahad.action_desk.engine.security

import net.abdulahad.action_desk.lib.json.Json
import net.abdulahad.action_desk.model.Action
import java.nio.charset.StandardCharsets
import java.util.Arrays
import javax.crypto.spec.SecretKeySpec

object ProtectedActionCrypto {
	
	fun payloadFromAction(action: Action): ProtectedActionPayload {
		return ProtectedActionPayload(
			command = action.command,
			arguments = action.arguments,
			startDirectory = action.startDirectory
		)
	}
	
	fun applyPayload(action: Action, payload: ProtectedActionPayload): Action {
		action.command = payload.command
		action.arguments = payload.arguments
		action.startDirectory = payload.startDirectory
		return action
	}
	
	fun clearPlainPayload(action: Action): Action {
		action.command = ""
		action.arguments = ""
		action.startDirectory = ""
		return action
	}
	
	fun encryptActionPayload(action: Action, password: String): Action {
		action.encryptedPayload = encryptPayload(payloadFromAction(action), password)
		clearPlainPayload(action)
		return action
	}
	
	fun decryptActionPayload(action: Action, password: String): Action {
		if (action.encryptedPayload.isBlank()) {
			if (!SecurityService.verifyPassword(password)) {
				throw InvalidPasswordException()
			}
			
			return action
		}
		
		return applyPayload(action, decryptPayload(action.encryptedPayload, password))
	}
	
	fun encryptPayload(payload: ProtectedActionPayload, password: String): String {
		val masterKey = MasterKeyStore.unwrap(password)
		var plainBytes: ByteArray? = null
		
		try {
			plainBytes = Json.stringify(payload).toByteArray(StandardCharsets.UTF_8)
			val secretKey = SecretKeySpec(masterKey, "AES")
			val encrypted = AesGcm.encrypt(plainBytes, secretKey)
			
			return Json.stringify(
				ProtectedActionPayloadEnvelope(
					version = 1,
					algorithm = "AES-GCM",
					iv = encrypted.first,
					ciphertext = encrypted.second
				)
			)
		} finally {
			Arrays.fill(masterKey, 0)
			plainBytes?.fill(0)
		}
	}
	
	fun decryptPayload(encryptedPayload: String, password: String): ProtectedActionPayload {
		if (encryptedPayload.isBlank()) {
			throw ProtectedActionPayloadException("Protected action payload is missing.")
		}
		
		val masterKey = MasterKeyStore.unwrap(password)
		var plainBytes: ByteArray? = null
		
		try {
			val envelope = Json.mapper.readValue(encryptedPayload, ProtectedActionPayloadEnvelope::class.java)
			validateEnvelope(envelope)
			
			val secretKey = SecretKeySpec(masterKey, "AES")
			plainBytes = AesGcm.decrypt(envelope.iv, envelope.ciphertext, secretKey)
			val json = String(plainBytes, StandardCharsets.UTF_8)
			
			return Json.mapper.readValue(json, ProtectedActionPayload::class.java)
		} catch (e: InvalidPasswordException) {
			throw e
		} catch (e: ProtectedActionPayloadException) {
			throw e
		} catch (e: Exception) {
			throw ProtectedActionPayloadException("Protected action payload could not be decrypted.", e)
		} finally {
			Arrays.fill(masterKey, 0)
			plainBytes?.fill(0)
		}
	}
	
	private fun validateEnvelope(envelope: ProtectedActionPayloadEnvelope) {
		if (envelope.version != 1) {
			throw ProtectedActionPayloadException("Unsupported protected payload version: ${envelope.version}")
		}
		
		if (envelope.algorithm != "AES-GCM") {
			throw ProtectedActionPayloadException("Unsupported protected payload algorithm: ${envelope.algorithm}")
		}
	}
	
}
