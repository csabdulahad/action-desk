package net.abdulahad.action_desk.engine.security

open class ActionDeskSecurityException(message: String, cause: Throwable? = null): RuntimeException(message, cause)

class PasswordNotConfiguredException: ActionDeskSecurityException("Action Desk security password is not configured.")

class PasswordAlreadyConfiguredException: ActionDeskSecurityException("Action Desk security password is already configured.")

class InvalidPasswordException(cause: Throwable? = null): ActionDeskSecurityException("Invalid password.", cause)

class ProtectedActionPayloadException(
	message: String,
	cause: Throwable? = null
): ActionDeskSecurityException(message, cause)
