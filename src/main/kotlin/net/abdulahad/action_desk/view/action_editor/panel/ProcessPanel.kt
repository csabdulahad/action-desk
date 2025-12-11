package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.action_editor.ActionEditorPanel
import org.jdesktop.swingx.VerticalLayout
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton

class ProcessPanel(): JPanel(), ActionEditorPanel {
	
	private var runAs = FlatCheckBox()
	private var singleton = FlatCheckBox()
	private var startWithAD = FlatCheckBox()
	
	private lateinit var group: ButtonGroup
	
	private var buttonList = mutableListOf<JRadioButton>()
	
	init {
		setupPanel()
		setupLabels()
		addFields()
	}
	
	private fun addFields() {
		add(runAs)
		add(singleton)
		add(startWithAD)
	}
	
	private fun setupLabels() {
		runAs.text 		 = "Run as administrator"
		singleton.text 	 = "Single instance"
		startWithAD.text = "Start with ActionDesk"
	}
	
	private fun setupPanel() {
		layout = VerticalLayout(6)
	}
	
	override fun save(action: Action): String? {
		action.runAsAdmin = runAs.isSelected
		action.singleton = singleton.isSelected
		action.startWithAD = startWithAD.isSelected
		
		return null
	}
	
	override fun setData(action: Action) {
		runAs.isSelected = action.runAsAdmin
		singleton.isSelected = action.singleton
		startWithAD.isSelected = action.startWithAD
	}
	
}