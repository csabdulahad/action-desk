package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatTextArea
import com.formdev.flatlaf.ui.FlatScrollPaneBorder
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.lib.view.ScrollableTextField
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.IconGridPanel
import net.abdulahad.action_desk.view.action_editor.ActionEditorPanel
import org.jdesktop.swingx.VerticalLayout
import org.jdesktop.swingx.prompt.PromptSupport
import java.awt.FlowLayout
import java.awt.Insets
import java.awt.Window
import javax.swing.*

class GeneralPanel(private val frame: Window): JPanel(), ActionEditorPanel {
	
	private var nameField = ScrollableTextField()
	private var chooseIconBtn = JButton()
	
	private var descriptionLabel = JLabel("Description")
	private var descriptionField = FlatTextArea()
	
	private var iconName: String = "actionDesk"
	
	init {
		chooseIconBtn.margin = Insets(6, 6, 6, 6)
		setupPanel()
		setupIconBtn()
		setupPlaceholders()
		addFields()
		
		descriptionField.apply {
			lineWrap = true
			wrapStyleWord = true
		}
	}
	
	private fun addFields() {
		/*
		 * Icon & name
		 * */
		val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
			add(chooseIconBtn)
		}
		
		add(panel)
		add(nameField)
		
		val descriptionScrollPane = JScrollPane().apply {
			border = FlatScrollPaneBorder()
			setViewportView(descriptionField)
		}
		
		add(descriptionScrollPane)
	}
	
	private fun setupPlaceholders() {
		descriptionField.rows = 3
		nameField.placeholderText = "Name*"
		PromptSupport.setPrompt("Description", descriptionField)
	}
	
	private fun setFrameIcon() {
		val icon = iconName.icon(32)
		(frame as JDialog).setIconImage(icon.image)
	}
	
	private fun setupPanel() {
		layout = VerticalLayout(12)
		setFrameIcon()
	}
	
	private fun setupIconBtn() {
		val icon = iconName.icon(32)
		chooseIconBtn.icon = icon
		
		chooseIconBtn.addActionListener {
			IconGridPanel(frame) { x ->
				this@GeneralPanel.iconName = x
				setFrameIcon()
				
				val icon = x.icon(32)
				chooseIconBtn.icon = icon
			}
		}
	}
	
	override fun save(action: Action): String? {
		if (nameField.text.trim().isEmpty()) {
			return "General:Action name is required"
		}
		
		action.icon = iconName
		action.name = nameField.text
		action.description = descriptionField.text
		
		return null
	}
	
	override fun setData(action: Action) {
		iconName = action.icon
		setFrameIcon()
		chooseIconBtn.icon = iconName.icon(32)
		
		nameField.text = action.name
		descriptionField.text = action.description
	}
	
}