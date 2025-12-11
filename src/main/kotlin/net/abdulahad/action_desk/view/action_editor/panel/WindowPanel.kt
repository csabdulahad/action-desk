package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.action_editor.ActionEditorPanel
import org.jdesktop.swingx.VerticalLayout
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

class WindowPanel(): JPanel(), ActionEditorPanel {
	
	private var showWindowCheckbox = FlatCheckBox()
	
	private val windowStyleOptions = arrayOf("Normal", "Minimized", "Maximized", "Hidden")
	private val windowStyleDropdown = JComboBox(windowStyleOptions)
	
	init {
		setupPanel()
		setupLabels()
		addFields()
		
		setupNoNewWindowCheckbox()
	}
	
	private fun setupNoNewWindowCheckbox() {
		showWindowCheckbox.isSelected = false
		toggleProcessOptions()
		
		showWindowCheckbox.addActionListener {
			toggleProcessOptions()
		}
	}
	
	private fun toggleProcessOptions() {
		windowStyleDropdown.isEnabled = showWindowCheckbox.isSelected
	}
	
	private fun addFields() {
		add(showWindowCheckbox)
		
		JLabel("Window style").apply {
			border = BorderFactory.createEmptyBorder(12, 0, 0, 0)
			this@WindowPanel.add(this)
		}
		
		add(getWindowStyleDropdown())
	}
	
	private fun getWindowStyleDropdown(): JPanel {
		return JPanel().apply {
			layout = FlowLayout(FlowLayout.LEFT)
			add(windowStyleDropdown)
		}
	}
	
	private fun setupLabels() {
		showWindowCheckbox.text = "Show window"
	}
	
	private fun setupPanel() {
		layout = VerticalLayout()
		border = BorderFactory.createEmptyBorder()
	}
	
	private fun getSelectedRadioButton(group: ButtonGroup): JRadioButton? {
		val selectedModel = group.selection ?: return null
		
		val buttons = group.elements
		
		while (buttons.hasMoreElements()) {
			val button = buttons.nextElement()
			if (button.model == selectedModel) {
				return button as JRadioButton?
			}
		}
		
		return null
	}
	
	override fun save(action: Action): String? {
		action.showWindow = showWindowCheckbox.isSelected
		
		var windowStyle = "Normal"
		
		if (windowStyleDropdown.selectedIndex != -1) {
			windowStyle = windowStyleDropdown.selectedItem?.toString() ?: "Normal"
		}
		
		action.windowStyle = windowStyle
		
		return null
	}
	
	override fun setData(action: Action) {
		showWindowCheckbox.isSelected = action.showWindow
		
		val windowStyleIndex = windowStyleOptions.indexOf(action.windowStyle)
		windowStyleDropdown.selectedIndex = windowStyleIndex
		
		toggleProcessOptions()
	}
	
}