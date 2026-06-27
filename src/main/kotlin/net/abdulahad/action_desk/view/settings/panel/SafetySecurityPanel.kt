package net.abdulahad.action_desk.view.settings.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.engine.security.PasswordAlreadyConfiguredException
import net.abdulahad.action_desk.engine.security.SecurityService
import net.abdulahad.action_desk.helper.ViewHelper
import net.abdulahad.action_desk.lib.view.ButtonUnderlined
import net.abdulahad.action_desk.view.settings.SettingsPanel
import net.abdulahad.action_desk.view.settings.dialog.SecurityPasswordDialog
import org.jdesktop.swingx.VerticalLayout
import java.awt.FlowLayout
import java.awt.Window
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class SafetySecurityPanel(
	private val frame: Window,
	private val showFeedback: (String, Boolean) -> Unit = { _, _ -> }
): JPanel(), SettingsPanel {
	
	private val disableConfirmationCheckbox = FlatCheckBox()
	private val passwordProtectionCheckbox = FlatCheckBox()
	private val changePasswordButton = ButtonUnderlined("Change password")
	
	private var suppressPasswordCheckboxEvent = false
	private var currentPasswordProtectionEnabled = false
	
	init {
		layout = VerticalLayout(8)
		
		setupPlaceholders()
		addFields()
		addListeners()
	}
	
	private fun setupPlaceholders() {
		disableConfirmationCheckbox.text = "Disable confirmation on all actions"
		disableConfirmationCheckbox.toolTipText =
			"Suppress confirmation prompts for every action, even if individual actions request confirmation."
		
		passwordProtectionCheckbox.text = "Enable password protection"
		passwordProtectionCheckbox.toolTipText =
			"Require the Action Desk password for protected actions once password support is configured."
		
		changePasswordButton.toolTipText = "Create or change the Action Desk security password."
	}
	
	private fun addFields() {
		add(ViewHelper.wrappedHint(
			"Safety and Security\n\n" +
			"Use these options to control global confirmation behaviour and password protection.",
			300
		))
		
		add(disableConfirmationCheckbox.apply {
			border = EmptyBorder(5, 0, 0, 0)
		})
		
		add(passwordProtectionCheckbox)
		
		val passwordActionRow = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
			isOpaque = false
			add(changePasswordButton)
		}
		
		add(passwordActionRow)
		
		add(ViewHelper.wrappedHint(
			"Password protection only controls whether protected actions require the Action Desk password. " +
			"Disabling it does not delete your password or encrypted master key.",
			300
		))
	}
	
	private fun addListeners() {
		passwordProtectionCheckbox.addActionListener {
			if (!suppressPasswordCheckboxEvent) {
				onPasswordProtectionRequested(passwordProtectionCheckbox.isSelected)
			}
		}
		
		changePasswordButton.addActionListener {
			onChangePasswordRequested()
		}
	}
	
	private fun onPasswordProtectionRequested(targetEnabled: Boolean) {
		if (targetEnabled == currentPasswordProtectionEnabled) {
			return
		}
		
		setPasswordProtectionCheckbox(currentPasswordProtectionEnabled)
		
		if (!SecurityService.hasPassword()) {
			handleFirstPasswordSetup(targetEnabled)
			return
		}
		
		handlePasswordProtectionToggle(targetEnabled)
	}
	
	private fun handleFirstPasswordSetup(targetEnabled: Boolean) {
		if (!targetEnabled) {
			refreshPasswordProtectionState()
			return
		}
		
		val newPassword = SecurityPasswordDialog.showSetPassword(frame) ?: run {
			refreshPasswordProtectionState()
			return
		}
		
		try {
			SecurityService.setupPassword(newPassword)
			AppConfig.setPasswordProtectionEnabled(true)
			refreshPasswordProtectionState()
			showFeedback("Password saved and password protection enabled.", false)
		} catch (_: PasswordAlreadyConfiguredException) {
			refreshPasswordProtectionState()
			showFeedback("Password is already configured. Use Change password instead.", true)
		} catch (e: Exception) {
			refreshPasswordProtectionState()
			showFeedback(e.message ?: "Password could not be saved.", true)
		}
	}
	
	private fun handlePasswordProtectionToggle(targetEnabled: Boolean) {
		val success = SecurityPasswordDialog.showVerifiedPassword(
			parent = frame,
			title = if (targetEnabled) "Enable Password Protection" else "Disable Password Protection",
			invalidPasswordMessage = "Password did not work.",
			verifier = { password ->
				if (targetEnabled) {
					SecurityService.enablePasswordProtection(password)
				} else {
					SecurityService.disablePasswordProtection(password)
				}
			}
		)
		
		refreshPasswordProtectionState()
		
		if (success) {
			showFeedback(
				if (targetEnabled) "Password protection enabled." else "Password protection disabled.",
				false
			)
		}
	}

	private fun onChangePasswordRequested() {
		if (!SecurityService.hasPassword()) {
			val newPassword = SecurityPasswordDialog.showSetPassword(frame) ?: return
			
			try {
				SecurityService.setupPassword(newPassword)
				refreshPasswordProtectionState()
				showFeedback("Password saved.", false)
			} catch (_: PasswordAlreadyConfiguredException) {
				refreshPasswordProtectionState()
				showFeedback("Password is already configured. Use Change password instead.", true)
			} catch (e: Exception) {
				refreshPasswordProtectionState()
				showFeedback(e.message ?: "Password could not be saved.", true)
			}
			
			return
		}
		
		val changed = SecurityPasswordDialog.showVerifiedChangePassword(
			parent = frame,
			invalidCurrentPasswordMessage = "Current password did not work.",
			changePassword = { currentPassword, newPassword ->
				SecurityService.changePassword(currentPassword, newPassword)
			}
		)
		
		if (changed) {
			showFeedback("Password changed.", false)
		}
	}
	
	private fun refreshPasswordProtectionState() {
		val configured = SecurityService.hasPassword()
		
		if (!configured && AppConfig.getPasswordProtectionEnabled()) {
			AppConfig.setPasswordProtectionEnabled(false)
		}
		
		currentPasswordProtectionEnabled = configured && AppConfig.getPasswordProtectionEnabled()
		setPasswordProtectionCheckbox(currentPasswordProtectionEnabled)
	}
	
	private fun setPasswordProtectionCheckbox(selected: Boolean) {
		suppressPasswordCheckboxEvent = true
		passwordProtectionCheckbox.isSelected = selected
		suppressPasswordCheckboxEvent = false
	}
	
	override fun initUI() {
		disableConfirmationCheckbox.isSelected = AppConfig.getDisableConfirmationOnAllActions()
		refreshPasswordProtectionState()
	}
	
	override fun saveConfig() {
		AppConfig.setDisableConfirmationOnAllActions(disableConfirmationCheckbox.isSelected)
		// Password protection is saved immediately after password verification.
		// Do not persist the checkbox state here, otherwise Save could bypass password checks.
	}
	
}
