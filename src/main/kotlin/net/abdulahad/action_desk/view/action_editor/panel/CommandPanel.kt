package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatTextArea
import com.formdev.flatlaf.ui.FlatScrollPaneBorder
import net.abdulahad.action_desk.lib.view.ScrollableTextField
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.action_editor.ActionEditorPanel
import org.jdesktop.swingx.VerticalLayout
import org.jdesktop.swingx.prompt.PromptSupport
import java.awt.Dimension
import java.awt.KeyboardFocusManager
import java.awt.event.ActionEvent
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.KeyStroke
import javax.swing.text.TextAction
import javax.swing.text.Utilities

class CommandPanel() : JPanel(), ActionEditorPanel {
	
	private var hostPanel = JPanel()
	
	private var directoryLabel = JLabel("Start Directory")
	private var directoryField = ScrollableTextField()
	
	private var commandLabel = JLabel("Command*")
	private var commandField = FlatTextArea()
	
	private var argLabel = JLabel("Arguments")
	private var argField = FlatTextArea()
	
	private var icon: String = "breakpointexception"
	
	init {
		setupPanel()
		applyMaxSizeOnFields()
		addFields()
	}
	
	private fun applyMaxSizeOnFields() {
		directoryField.preferredSize = Dimension(100, directoryField.preferredSize.height)
	}
	
	private fun addFields() {
		// Directory
		add(directoryLabel)
		add(directoryField)
		
		setupAndAddTextAreas()
	}
	
	private fun setupPanel() {
		layout = VerticalLayout()
		
		directoryLabel.border = BorderFactory.createEmptyBorder(0, 0, 3, 0)
		commandLabel.border = BorderFactory.createEmptyBorder(12, 0, 3, 0)
		argLabel.border = BorderFactory.createEmptyBorder(12, 0, 3, 0)
	}
	
	override fun save(action: Action): String? {
		if (commandField.text.trim().isEmpty()) {
			return "Command:Command is required"
		}
		
		action.startDirectory = directoryField.text
		action.command = commandField.text
		action.arguments = argField.text
		
		return null
	}
	
	override fun setData(action: Action) {
		directoryField.text = action.startDirectory
		commandField.text = action.command
		argField.text = action.arguments
	}
	
	private fun setupAndAddTextAreas() {
		mapOf(
			commandLabel to commandField,
			argLabel to argField
		).forEach { (labelField, textArea) ->
			
			add(labelField)
			
			textArea.apply {
				lineWrap = true
				wrapStyleWord = true
			}
			
			/*
			 * Handle tab and shift+tab to navigate focus in inputs elements!
			 * */
			/*textArea.setFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
					KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS)
			)
			
			textArea.setFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
				KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(
					KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS)
			)*/


// Keep normal focus traversal on Tab / Shift+Tab
			textArea.setFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS)
			)
			textArea.setFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
				KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS)
			)

// Add Ctrl+Tab = insert tab
			textArea.inputMap.put(
				KeyStroke.getKeyStroke("control TAB"),
				"insertTab"
			)
			textArea.actionMap.put(
				"insertTab",
				object : TextAction("insertTab") {
					override fun actionPerformed(e: ActionEvent?) {
						textArea.replaceSelection("\t")
					}
				}
			)

			// Add Ctrl+Shift+Tab = remove leading tab (outdent)
			textArea.inputMap.put(
				KeyStroke.getKeyStroke("control shift TAB"),
				"removeTab"
			)
			textArea.actionMap.put(
				"removeTab",
				object : TextAction("removeTab") {
					override fun actionPerformed(e: ActionEvent?) {
						val caretPos = textArea.caretPosition
						val doc = textArea.document
						val start = textArea.selectionStart
						val end = textArea.selectionEnd
						
						// Find line start
						val lineStart = Utilities.getRowStart(textArea, start)
						if (doc.getText(lineStart, 1) == "\t") {
							doc.remove(lineStart, 1)
						}
					}
				}
			)
			
			
			val scrollPane = JScrollPane().apply {
				border = FlatScrollPaneBorder()
				setViewportView(textArea)
			}
			
			add(scrollPane)
		}
		
		commandField.rows = 3
		argField.rows = 5
		
		PromptSupport.setPrompt("e.g. php", commandField)
		PromptSupport.setPrompt("comma separated values", argField)
	}
	
}