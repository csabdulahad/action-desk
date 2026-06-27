package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import net.abdulahad.action_desk.engine.security.SecurityService
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.action_editor.ActionEditorPanel
import org.jdesktop.swingx.VerticalLayout
import javax.swing.JPanel

class SafetySecurityPanel: JPanel(), ActionEditorPanel {
	
	private var confirmationBeforeRun = FlatCheckBox()
	private var passwordProtected = FlatCheckBox()
	
	init {
		setupPanel()
		setupLabels()
		addFields()
	}
	
	private fun setupPanel() {
		layout = VerticalLayout(6)
	}
	
	private fun setupLabels() {
		confirmationBeforeRun.text = "Ask for confirmation before running"
		passwordProtected.text = "Protect this action with password"
		passwordProtected.toolTipText = "Requires Action Desk security password to be set in Settings first"
	}
	
	private fun addFields() {
		add(confirmationBeforeRun)
		add(passwordProtected)
	}
	
	override fun save(action: Action): String? {
		if (passwordProtected.isSelected && !SecurityService.hasPassword()) {
			return "Safety & Security:Set security password in Settings first"
		}
		
		action.confirmationBeforeRun = confirmationBeforeRun.isSelected
		action.passwordProtected = passwordProtected.isSelected
		
		return null
	}
	
	override fun setData(action: Action) {
		confirmationBeforeRun.isSelected = action.confirmationBeforeRun
		passwordProtected.isSelected = action.passwordProtected
	}
	
}
