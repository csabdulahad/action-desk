package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import com.formdev.flatlaf.extras.components.FlatTextArea
import com.formdev.flatlaf.extras.components.FlatTextField
import com.formdev.flatlaf.ui.FlatScrollPaneBorder
import net.abdulahad.action_desk.helper.Icons
import net.abdulahad.action_desk.repo.action.ActionDao
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.helper.Icons.toImageIcon
import net.abdulahad.action_desk.lib.view.ScrollableTextField
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.IconGridPanel
import net.abdulahad.action_desk.view.action_editor.ActionEditorPanel
import org.jdesktop.swingx.HorizontalLayout
import org.jdesktop.swingx.VerticalLayout
import org.jdesktop.swingx.prompt.PromptSupport
import java.awt.FlowLayout
import java.awt.Insets
import java.awt.Window
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

class GeneralPanel(private val frame: Window): JPanel(), ActionEditorPanel {
	
	private var nameField = ScrollableTextField()
	private var chooseIconBtn = JButton()
	
	private var descriptionLabel = JLabel("Description")
	private var descriptionField = FlatTextArea()
	
	private var globalShortcutField = FlatTextField()
	private var globalKeyList: List<String>
	
	private val winCheckBox = FlatCheckBox()
	
	private var feedback: ((String) -> Unit)? = null
	
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
		
		/*
		 * Shortcut
		 * */
		globalKeyList = ActionDao.listKeys()
		
		val disallowed = listOf(KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_ESCAPE)
		setupKeyListener(globalShortcutField, disallowed)
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
		
		/*
		 * Description field
		 * */
		val descriptionScrollPane = JScrollPane().apply {
			border = FlatScrollPaneBorder()
			setViewportView(descriptionField)
		}
		
		add(descriptionScrollPane)
		
		/*
		 * Global shortcut
		 * */
		addWinKeyCheckBoxPanel()
	}
	
	private fun addWinKeyCheckBoxPanel() {
		val windowKeyCheckbox = JPanel(HorizontalLayout()).apply {
			winCheckBox.text = "Make it Windows shortcut"
			add(winCheckBox)
			
			val label = JLabel(Icons.WINDOWS.icon(16))
			add(label)
		}
		
		val panel = JPanel(VerticalLayout()).apply {
			
			val label = JLabel("Global shortcut").apply {
				border = EmptyBorder(8, 0, 0, 0)
			}
			
			add(label)
			add(globalShortcutField)
			add(windowKeyCheckbox)
		}
		
		add(panel)
	}
	private fun setupPlaceholders() {
		descriptionField.rows = 3
		nameField.placeholderText = "Name*"
		PromptSupport.setPrompt("Description", descriptionField)
		
		PromptSupport.setPrompt("Global shortcut", globalShortcutField)
		globalShortcutField.toolTipText = "Assign global shortcut to this action to run it from anywhere"
	}
	
	private fun setFrameIcon() {
		val icon = iconName.icon(32).toImageIcon()
		(frame as JDialog).setIconImage(icon.image)
	}
	
	private fun setupPanel() {
		layout = VerticalLayout(12)
		setFrameIcon()
		
		globalShortcutField.isEditable = false
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
	
	private fun setupKeyListener(field: JTextField, disallowedKeys: List<Int> = emptyList()) {
		field.addKeyListener(object : KeyAdapter() {
			override fun keyPressed(e: KeyEvent) {
				e.consume() // Prevent default behavior like beeps
				
				if (e.keyCode == KeyEvent.VK_BACK_SPACE) {
					field.text = ""
					return
				}
				
				val keyText = KeyEvent.getKeyText(e.keyCode)
				
				// Combo mode
				val modifiers = mutableListOf<String>()
				if (!winCheckBox.isSelected) {
					if (e.isControlDown) modifiers.add("Ctrl")
					if (e.isAltDown) modifiers.add("Alt")
					if (e.isShiftDown) modifiers.add("Shift")
					if (e.isMetaDown) modifiers.add("Meta")
				
					if (modifiers.isEmpty()) return
				}
				
				// Avoid just modifiers alone (e.g., "Ctrl")
				if (keyText !in listOf("Ctrl", "Shift", "Alt", "Meta")) {
					modifiers.add(keyText)
					val x = modifiers.joinToString("+")
					
					if (x in globalShortcutField.text) {
						feedback?.invoke("Global shortcut $x is already in use")
						return
					}
					
					field.text = x
				}
			}
		})
	}
	
	override fun save(action: Action): String? {
		if (nameField.text.trim().isEmpty()) {
			return "General:Action name is required"
		}
		
		action.icon = iconName
		action.name = nameField.text
		action.description = descriptionField.text
		
		/*
		 * Shortcut
		 * */
		if (globalShortcutField.text.isBlank()) {
			action.globalKey = ""
		} else {
			val win = if (winCheckBox.isSelected) "Win+" else ""
			action.globalKey = "$win${globalShortcutField.text}"
		}
		
		return null
	}
	
	override fun setData(action: Action) {
		iconName = action.icon
		setFrameIcon()
		chooseIconBtn.icon = iconName.icon(32)
		
		nameField.text = action.name
		descriptionField.text = action.description
		
		setWinShortcutUI(action)
	}
	
	fun setWinShortcutUI(action: Action) {
		/*
		 * Shortcut
		 * */
		val winShortcut = action.globalKey.startsWith("Win")
		
		if (winShortcut) {
			action.globalKey = action.globalKey.replaceFirst("Win+", "")
		}
		
		winCheckBox.isSelected = winShortcut
		globalShortcutField.text = action.globalKey
	}
	
	fun setFeedbacker(feedback: (String) -> Unit) {
		this.feedback = feedback
	}
	
}