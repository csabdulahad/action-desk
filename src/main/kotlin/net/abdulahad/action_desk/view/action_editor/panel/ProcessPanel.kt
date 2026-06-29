package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.action_editor.ActionEditorPanel
import org.jdesktop.swingx.VerticalLayout
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton

class ProcessPanel: JPanel(), ActionEditorPanel {
	
	private var runAs = FlatCheckBox()
	private var singleton = FlatCheckBox()
	private var startWithAD = FlatCheckBox()
	private var bringWindowFront = FlatCheckBox()
	
	private lateinit var group: ButtonGroup
	
	private var buttonList = mutableListOf<JRadioButton>()
	
	init {
		setupPanel()
		setupLabels()
		addFields()
		addSingletonCheckListener()
	}
	
	private fun addSingletonCheckListener() {
		singleton.addChangeListener {
			if (!singleton.isSelected) {
				bringWindowFront.isSelected = false
			}
		}
	}
	
	private fun addFields() {
		add(runAs)
		add(startWithAD)
		add(singleton)
		add(bringWindowFront)
	}
	
	private fun setupLabels() {
		runAs.text = "Run as administrator"
		singleton.text = "Single instance"
		startWithAD.text = "Start with ActionDesk"
		bringWindowFront.text = "Bring window front (experimental)"
	}
	
	private fun setupPanel() {
		layout = VerticalLayout(6)
	}
	
	override fun save(action: Action): String? {
		action.runAsAdmin = runAs.isSelected
		action.singleton = singleton.isSelected
		action.startWithAD = startWithAD.isSelected
		
		if (singleton.isSelected) {
			action.bringWindow = bringWindowFront.isSelected
		} else {
			action.bringWindow = false
		}
		
		return null
	}
	
	override fun setData(action: Action) {
		runAs.isSelected = action.runAsAdmin
		singleton.isSelected = action.singleton
		startWithAD.isSelected = action.startWithAD
		
		if (singleton.isSelected) {
			bringWindowFront.isSelected = action.bringWindow
		}
	}
	
}
