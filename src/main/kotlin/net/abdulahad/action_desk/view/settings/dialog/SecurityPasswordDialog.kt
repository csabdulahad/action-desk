package net.abdulahad.action_desk.view.settings.dialog

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Window
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.UIManager

object SecurityPasswordDialog {
	
	private const val DIALOG_WIDTH = 360
	private const val FIELD_WIDTH = 320
	
	data class ChangePasswordRequest(
		val currentPassword: String,
		val newPassword: String
	)
	
	fun showSetPassword(parent: Window): String? {
		var result: String? = null
		lateinit var dialog: JDialog
		
		val newPasswordField = passwordField()
		val confirmPasswordField = passwordField()
		val errorLabel = errorLabel()
		
		val saveButton = JButton("Save").apply {
			addActionListener {
				val newPassword = passwordValue(newPasswordField)
				val confirmPassword = passwordValue(confirmPasswordField)
				
				when {
					newPassword.isBlank() -> showError(errorLabel, "New password is required.")
					newPassword != confirmPassword -> showError(errorLabel, "New password does not match confirmation.")
					else -> {
						result = newPassword
						clearPasswords(newPasswordField, confirmPasswordField)
						dialog.dispose()
					}
				}
			}
		}
		
		dialog = createDialog(
			parent = parent,
			title = "Set Security Password",
			fieldPanel = fieldPanel(
				"New password" to newPasswordField,
				"Confirm password" to confirmPasswordField
			),
			errorLabel = errorLabel,
			primaryButton = saveButton
		)
		
		newPasswordField.requestFocusInWindow()
		dialog.isVisible = true
		return result
	}
	
	fun showConfirmPassword(
		parent: Window,
		title: String,
		message: String
	): String? {
		var result: String? = null
		lateinit var dialog: JDialog
		
		val passwordField = passwordField()
		val errorLabel = errorLabel()
		
		val confirmButton = JButton("Confirm").apply {
			addActionListener {
				val password = passwordValue(passwordField)
				
				if (password.isBlank()) {
					showError(errorLabel, "Password is required.")
					return@addActionListener
				}
				
				result = password
				clearPasswords(passwordField)
				dialog.dispose()
			}
		}
		
		dialog = createDialog(
			parent = parent,
			title = title,
			fieldPanel = fieldPanel("Password" to passwordField),
			errorLabel = errorLabel,
			primaryButton = confirmButton
		)
		
		passwordField.requestFocusInWindow()
		dialog.isVisible = true
		return result
	}
	
	fun showVerifiedPassword(
		parent: Window,
		title: String,
		invalidPasswordMessage: String = "Invalid password.",
		verifier: (String) -> Boolean
	): Boolean {
		var verified = false
		lateinit var dialog: JDialog
		
		val passwordField = passwordField()
		val errorLabel = errorLabel()
		
		val confirmButton = JButton("Confirm").apply {
			addActionListener {
				val password = passwordValue(passwordField)
				
				if (password.isBlank()) {
					showError(errorLabel, "Password is required.")
					return@addActionListener
				}
				
				val success = try {
					verifier(password)
				} catch (e: Exception) {
					clearPasswords(passwordField)
					showError(errorLabel, e.message ?: invalidPasswordMessage)
					passwordField.requestFocusInWindow()
					return@addActionListener
				}
				
				if (success) {
					verified = true
					clearPasswords(passwordField)
					dialog.dispose()
				} else {
					clearPasswords(passwordField)
					showError(errorLabel, invalidPasswordMessage)
					passwordField.requestFocusInWindow()
				}
			}
		}
		
		dialog = createDialog(
			parent = parent,
			title = title,
			fieldPanel = fieldPanel("Password" to passwordField),
			errorLabel = errorLabel,
			primaryButton = confirmButton
		)
		
		passwordField.requestFocusInWindow()
		dialog.isVisible = true
		return verified
	}

	fun showChangePassword(parent: Window): ChangePasswordRequest? {
		var result: ChangePasswordRequest? = null
		
		showChangePasswordDialog(parent) { currentPassword, newPassword, _ ->
			result = ChangePasswordRequest(currentPassword, newPassword)
			true
		}
		
		return result
	}

	fun showVerifiedChangePassword(
		parent: Window,
		invalidCurrentPasswordMessage: String = "Current password did not work.",
		changePassword: (currentPassword: String, newPassword: String) -> Boolean
	): Boolean {
		return showChangePasswordDialog(parent) { currentPassword, newPassword, controls ->
			val success = try {
				changePassword(currentPassword, newPassword)
			} catch (e: Exception) {
				controls.showError(e.message ?: "Password could not be changed.")
				controls.focusCurrentPassword()
				return@showChangePasswordDialog false
			}
			
			if (!success) {
				controls.clearCurrentPassword()
				controls.showError(invalidCurrentPasswordMessage)
				controls.focusCurrentPassword()
			}
			
			success
		}
	}
	
	private fun showChangePasswordDialog(
		parent: Window,
		onValidSubmit: (
			currentPassword: String,
			newPassword: String,
			controls: ChangePasswordDialogControls
		) -> Boolean
	): Boolean {
		var completed = false
		lateinit var dialog: JDialog
		
		val currentPasswordField = passwordField()
		val newPasswordField = passwordField()
		val confirmPasswordField = passwordField()
		val errorLabel = errorLabel()
		val controls = ChangePasswordDialogControls(
			currentPasswordField = currentPasswordField,
			newPasswordField = newPasswordField,
			confirmPasswordField = confirmPasswordField,
			errorLabel = errorLabel
		)
		
		val saveButton = JButton("Save").apply {
			addActionListener {
				val currentPassword = passwordValue(currentPasswordField)
				val newPassword = passwordValue(newPasswordField)
				val confirmPassword = passwordValue(confirmPasswordField)
				
				when {
					currentPassword.isBlank() -> showError(errorLabel, "Current password is required.")
					newPassword.isBlank() -> showError(errorLabel, "New password is required.")
					newPassword != confirmPassword -> showError(errorLabel, "New password does not match confirmation.")
					onValidSubmit(currentPassword, newPassword, controls) -> {
						completed = true
						controls.clearAllPasswords()
						dialog.dispose()
					}
				}
			}
		}
		
		dialog = createDialog(
			parent = parent,
			title = "Change Security Password",
			fieldPanel = fieldPanel(
				"Current password" to currentPasswordField,
				"New password" to newPasswordField,
				"Confirm password" to confirmPasswordField
			),
			errorLabel = errorLabel,
			primaryButton = saveButton
		)
		
		currentPasswordField.requestFocusInWindow()
		dialog.isVisible = true
		return completed
	}
	
	private class ChangePasswordDialogControls(
		private val currentPasswordField: JPasswordField,
		private val newPasswordField: JPasswordField,
		private val confirmPasswordField: JPasswordField,
		private val errorLabel: JLabel
	) {
		fun showError(message: String) {
			showError(errorLabel, message)
		}
		
		fun clearCurrentPassword() {
			clearPasswords(currentPasswordField)
		}
		
		fun clearAllPasswords() {
			clearPasswords(currentPasswordField, newPasswordField, confirmPasswordField)
		}
		
		fun focusCurrentPassword() {
			currentPasswordField.requestFocusInWindow()
		}
	}

	private fun createDialog(
		parent: Window,
		title: String,
		fieldPanel: JPanel,
		errorLabel: JLabel,
		primaryButton: JButton
	): JDialog {
		val dialog = JDialog(parent, title, Dialog.ModalityType.APPLICATION_MODAL).apply {
			defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
			isResizable = false
			layout = BorderLayout()
		}
		
		val contentPanel = JPanel(BorderLayout(0, 10)).apply {
			border = BorderFactory.createEmptyBorder(16, 16, 8, 16)
			add(fieldPanel, BorderLayout.CENTER)
			add(errorLabel, BorderLayout.PAGE_END)
		}
		
		val cancelButton = JButton("Cancel").apply {
			addActionListener { dialog.dispose() }
		}
		
		val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
			add(primaryButton)
			add(cancelButton)
		}
		
		dialog.rootPane.defaultButton = primaryButton
		dialog.add(contentPanel, BorderLayout.CENTER)
		dialog.add(buttonPanel, BorderLayout.PAGE_END)
		dialog.pack()
		dialog.minimumSize = Dimension(DIALOG_WIDTH, dialog.preferredSize.height)
		dialog.setLocationRelativeTo(parent)
		
		return dialog
	}
	
	private fun fieldPanel(vararg fields: Pair<String, JPasswordField>): JPanel {
		return JPanel().apply {
			layout = BoxLayout(this, BoxLayout.Y_AXIS)
			isOpaque = false
			alignmentX = Component.LEFT_ALIGNMENT
			
			fields.forEachIndexed { index, field ->
				if (index > 0) {
					add(verticalGap(14))
				}
				
				add(fieldGroup(field.first, field.second))
			}
		}
	}
	
	private fun fieldGroup(labelText: String, field: JPasswordField): JPanel {
		return JPanel().apply {
			layout = BoxLayout(this, BoxLayout.Y_AXIS)
			isOpaque = false
			alignmentX = Component.LEFT_ALIGNMENT
			
			val label = JLabel(labelText).apply {
				alignmentX = Component.LEFT_ALIGNMENT
				horizontalAlignment = JLabel.LEFT
			}
			
			add(label)
			add(verticalGap(4))
			add(field.apply { alignmentX = Component.LEFT_ALIGNMENT })
		}
	}
	
	private fun passwordField(): JPasswordField {
		return JPasswordField().apply {
			preferredSize = Dimension(FIELD_WIDTH, 28)
			minimumSize = Dimension(FIELD_WIDTH, 28)
			maximumSize = Dimension(FIELD_WIDTH, 28)
			alignmentX = Component.LEFT_ALIGNMENT
			putClientProperty("FlatLaf.style", "minimumWidth: $FIELD_WIDTH")
		}
	}
	
	private fun errorLabel(): JLabel {
		return JLabel(" ").apply {
			foreground = UIManager.getColor("Component.error.focusedBorderColor") ?: UIManager.getColor("Label.foreground")
		}
	}
	
	private fun showError(label: JLabel, message: String) {
		label.text = message
	}
	
	private fun passwordValue(field: JPasswordField): String {
		return String(field.password)
	}
	
	private fun clearPasswords(vararg fields: JPasswordField) {
		fields.forEach { it.text = "" }
	}
	
	private fun verticalGap(height: Int): JPanel {
		return JPanel().apply {
			isOpaque = false
			preferredSize = Dimension(1, height)
			minimumSize = Dimension(1, height)
			maximumSize = Dimension(Int.MAX_VALUE, height)
		}
	}
	
}
