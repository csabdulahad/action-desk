package net.abdulahad.action_desk.engine.action

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.engine.notification.NotificationManager
import net.abdulahad.action_desk.engine.security.InvalidPasswordException
import net.abdulahad.action_desk.engine.security.ProtectedActionCrypto
import net.abdulahad.action_desk.engine.security.SecurityService
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.view.settings.dialog.SecurityPasswordDialog
import java.awt.GraphicsEnvironment
import java.lang.reflect.InvocationTargetException
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

object ActionRunGuard {
	
	fun prepareActionForRun(action: Action, diagnose: Boolean, automaticRun: Boolean = false): Action? {
		if (!runConfirmationGuard(action, diagnose)) {
			return null
		}
		
		return preparePasswordProtectedAction(action, automaticRun)
	}
	
	fun canRun(action: Action, diagnose: Boolean, automaticRun: Boolean = false): Boolean {
		return prepareActionForRun(action, diagnose, automaticRun) != null
	}
	
	private fun runConfirmationGuard(action: Action, diagnose: Boolean): Boolean {
		if (!shouldConfirm(action)) {
			return true
		}
		
		val confirmed = askForConfirmation(action, diagnose)
		
		if (!confirmed) {
			val msg = "${action.name}: run cancelled by confirmation"
			App.logInfo(msg)
			
			if (App.isShown()) {
				App.setMessage(msg)
			}
		}
		
		return confirmed
	}
	
	private fun shouldConfirm(action: Action): Boolean {
		return action.confirmationBeforeRun && !AppConfig.getDisableConfirmationOnAllActions()
	}
	
	private fun askForConfirmation(action: Action, diagnose: Boolean): Boolean {
		if (GraphicsEnvironment.isHeadless()) {
			App.logWarn("${action.name}: confirmation required but UI is not available")
			return false
		}
		
		val title = if (diagnose) "Confirm Diagnose Run" else "Confirm Action"
		
		val message = if (diagnose) {
			"Run diagnosis for '${action.name}'?"
		} else {
			"Run '${action.name}'?"
		}
		
		if (SwingUtilities.isEventDispatchThread()) {
			return showConfirmationDialog(title, message)
		}
		
		var confirmed = false
		
		try {
			SwingUtilities.invokeAndWait {
				confirmed = showConfirmationDialog(title, message)
			}
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
			App.logWarn("${action.name}: confirmation interrupted")
			return false
		} catch (e: InvocationTargetException) {
			App.logErr("${action.name}: confirmation failed: ${e.targetException.message}")
			return false
		}
		
		return confirmed
	}
	
	private fun showConfirmationDialog(title: String, message: String): Boolean {
		val result = JOptionPane.showConfirmDialog(
			ActionDesk,
			message,
			title,
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		)
		
		return result == JOptionPane.YES_OPTION
	}
	
	private fun preparePasswordProtectedAction(action: Action, automaticRun: Boolean): Action? {
		if (!shouldAskPassword(action)) {
			return action
		}
		
		if (!SecurityService.hasPassword()) {
			val msg = "${action.name}: protected action blocked because no security password is configured"
			App.logWarn(msg)
			NotificationManager.warn(msg)
			
			if (App.isShown()) {
				App.setMessage(msg)
			}
			
			return null
		}
		
		if (automaticRun) {
			val msg = "${action.name}: protected action skipped because automatic runs cannot ask for password"
			App.logWarn(msg)
			NotificationManager.warn(msg)
			
			if (App.isShown()) {
				App.setMessage(msg)
			}
			
			return null
		}
		
		val preparedAction = askForPasswordAndPrepareAction(action)
		
		if (preparedAction == null) {
			val msg = "${action.name}: run cancelled by password protection"
			App.logInfo(msg)
			
			if (App.isShown()) {
				App.setMessage(msg)
			}
		}
		
		return preparedAction
	}
	
	private fun shouldAskPassword(action: Action): Boolean {
		return action.passwordProtected &&
			(AppConfig.getPasswordProtectionEnabled() || action.encryptedPayload.isNotBlank())
	}
	
	private fun askForPasswordAndPrepareAction(action: Action): Action? {
		if (GraphicsEnvironment.isHeadless()) {
			App.logWarn("${action.name}: password required but UI is not available")
			return null
		}
		
		if (SwingUtilities.isEventDispatchThread()) {
			return showPasswordDialogAndPrepareAction(action)
		}
		
		var preparedAction: Action? = null
		
		try {
			SwingUtilities.invokeAndWait {
				preparedAction = showPasswordDialogAndPrepareAction(action)
			}
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
			App.logWarn("${action.name}: password prompt interrupted")
			return null
		} catch (e: InvocationTargetException) {
			App.logErr("${action.name}: password prompt failed: ${e.targetException.message}")
			return null
		}
		
		return preparedAction
	}
	
	private fun showPasswordDialogAndPrepareAction(action: Action): Action? {
		var preparedAction: Action? = null
		
		val verified = SecurityPasswordDialog.showVerifiedPassword(
			parent = ActionDesk,
			title = "Protected Action",
			invalidPasswordMessage = "Password did not work.",
			verifier = { password ->
				try {
					preparedAction = prepareProtectedAction(action, password)
					true
				} catch (_: InvalidPasswordException) {
					false
				}
			}
		)
		
		return if (verified) preparedAction else null
	}
	
	private fun prepareProtectedAction(action: Action, password: String): Action {
		return ProtectedActionCrypto.decryptActionPayload(action.copy(), password)
	}
	
}
