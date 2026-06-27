package net.abdulahad.action_desk.engine.action

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.engine.notification.NotificationManager
import net.abdulahad.action_desk.engine.security.InvalidPasswordException
import net.abdulahad.action_desk.engine.security.ProtectedActionCrypto
import net.abdulahad.action_desk.engine.security.SecurityService
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.view.settings.dialog.SecurityPasswordDialog
import java.awt.GraphicsEnvironment
import java.awt.Window
import java.lang.reflect.InvocationTargetException
import javax.swing.SwingUtilities

object ActionEditGuard {
	
	data class PreparedAction(
		val action: Action,
		val unlockedPassword: String? = null
	)
	
	fun prepareActionForEdit(action: Action, parent: Window): PreparedAction? {
		if (!action.passwordProtected) {
			return PreparedAction(action)
		}
		
		if (!SecurityService.hasPassword()) {
			val msg = "${action.name}: protected action cannot be edited because no security password is configured"
			App.logWarn(msg)
			NotificationManager.warn(msg)
			setStatusMessage(msg)
			return null
		}
		
		val preparedAction = askForPasswordAndPrepareAction(action, parent)
		
		if (preparedAction == null) {
			val msg = "${action.name}: edit cancelled by password protection"
			App.logInfo(msg)
			setStatusMessage(msg)
		}
		
		return preparedAction
	}
	
	fun canEdit(action: Action, parent: Window): Boolean {
		return prepareActionForEdit(action, parent) != null
	}
	
	private fun askForPasswordAndPrepareAction(action: Action, parent: Window): PreparedAction? {
		if (GraphicsEnvironment.isHeadless()) {
			App.logWarn("${action.name}: password required for edit but UI is not available")
			return null
		}
		
		if (SwingUtilities.isEventDispatchThread()) {
			return showPasswordDialogAndPrepareAction(action, parent)
		}
		
		var preparedAction: PreparedAction? = null
		
		try {
			SwingUtilities.invokeAndWait {
				preparedAction = showPasswordDialogAndPrepareAction(action, parent)
			}
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
			App.logWarn("${action.name}: edit password prompt interrupted")
			return null
		} catch (e: InvocationTargetException) {
			App.logErr("${action.name}: edit password prompt failed: ${e.targetException.message}")
			return null
		}
		
		return preparedAction
	}
	
	private fun showPasswordDialogAndPrepareAction(action: Action, parent: Window): PreparedAction? {
		var preparedAction: PreparedAction? = null
		
		val verified = SecurityPasswordDialog.showVerifiedPassword(
			parent = parent,
			title = "Unlock Protected Action",
			invalidPasswordMessage = "Password did not work.",
			verifier = { password ->
				try {
					val editableAction = ProtectedActionCrypto.decryptActionPayload(action.copy(), password)
					preparedAction = PreparedAction(editableAction, password)
					true
				} catch (_: InvalidPasswordException) {
					false
				}
			}
		)
		
		return if (verified) preparedAction else null
	}
	
	private fun setStatusMessage(message: String) {
		if (App.isShown()) {
			ActionDesk.setMessage(message)
		}
	}
	
}
