package net.abdulahad.action_desk.engine.security

import net.abdulahad.action_desk.config.AppConfig

object SecurityService {
	
	fun hasPassword(): Boolean {
		return MasterKeyStore.hasWrappedMasterKey()
	}
	
	fun setupPassword(newPassword: String) {
		requireUsablePassword(newPassword)
		MasterKeyStore.createAndStore(newPassword)
	}
	
	fun verifyPassword(password: String): Boolean {
		if (password.isBlank()) {
			return false
		}
		
		return MasterKeyStore.verify(password)
	}
	
	fun changePassword(oldPassword: String, newPassword: String): Boolean {
		if (oldPassword.isBlank()) {
			return false
		}
		
		requireUsablePassword(newPassword)
		
		return try {
			MasterKeyStore.rewrap(oldPassword, newPassword)
			true
		} catch (_: InvalidPasswordException) {
			false
		}
	}
	
	fun enablePasswordProtection(password: String): Boolean {
		return setPasswordProtectionEnabled(password, true)
	}
	
	fun disablePasswordProtection(password: String): Boolean {
		return setPasswordProtectionEnabled(password, false)
	}
	
	private fun setPasswordProtectionEnabled(password: String, enabled: Boolean): Boolean {
		if (!verifyPassword(password)) {
			return false
		}
		
		AppConfig.setPasswordProtectionEnabled(enabled)
		return true
	}
	
	private fun requireUsablePassword(password: String) {
		if (password.isBlank()) {
			throw IllegalArgumentException("Password is required.")
		}
	}
	
}
