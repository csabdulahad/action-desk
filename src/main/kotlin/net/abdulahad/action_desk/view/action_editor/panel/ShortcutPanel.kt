package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatTextField
import info.clearthought.layout.TableLayout
import info.clearthought.layout.TableLayoutConstraints
import net.abdulahad.action_desk.dao.ActionDao
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.action_editor.ActionEditorPanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class ShortcutPanel(): JPanel(), ActionEditorPanel {
	
	private var hostPanel = JPanel()
	
	private var hotkeyLabel = JLabel("Hotkey")
	private var hotkeyField = FlatTextField()
	
	private var globalShortcutLabel = JLabel("Global")
	private var globalShortcutField = FlatTextField()
	
	private var hotKeyList: List<String>
	private var globalKeyList: List<String>
	
	private var feedback: ((String) -> Unit)? = null
	
	init {
		setupPanel()
		addFields()
		setupHotKeyListener()
		
		hotKeyList 	  = ActionDao.listKeys("hotkey")
		globalKeyList = ActionDao.listKeys()
		
		val disallowed = listOf(KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_ESCAPE)
		setupKeyListener(globalShortcutField, true, disallowed)
	}
	
	private fun setupPanel() {
		layout = BorderLayout()
		
		hostPanel.layout = TableLayout(
			arrayOf(
				doubleArrayOf(TableLayout.PREFERRED, TableLayout.PREFERRED),
				doubleArrayOf(
					TableLayout.PREFERRED,
					TableLayout.PREFERRED,
					TableLayout.PREFERRED,
					TableLayout.PREFERRED,
				)
			)
		)
		
		add(hostPanel, BorderLayout.CENTER)
		
		val tableLayout = hostPanel.layout as TableLayout
		tableLayout.hGap = 6
		tableLayout.vGap = 6
		
		hotkeyField.isEditable = false
		globalShortcutField.isEditable = false
		
		hotkeyField.preferredSize = Dimension(200, 25)
		globalShortcutField.preferredSize = Dimension(200, 25)
	}
	
	private fun addFields() {
		/*
		 * Hotkey
		 * */
		hostPanel.add(hotkeyLabel,
			TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL)
		)
		hostPanel.add(hotkeyField,
			TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL)
		)
		
		/*
		 * Global Shortcut
		 * */
		hostPanel.add(globalShortcutLabel,
			TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL)
		)
		hostPanel.add(globalShortcutField,
			TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL)
		)
	}
	
	private fun setupKeyListener(field: JTextField, allowCombos: Boolean = false, disallowedKeys: List<Int> = emptyList()) {
		field.addKeyListener(object : KeyAdapter() {
			override fun keyPressed(e: KeyEvent) {
				e.consume() // Prevent default behavior like beeps
				
				if (e.keyCode == KeyEvent.VK_BACK_SPACE) {
					field.text = ""
					return
				}
				
				// Filter disallowed keys if not in combo mode
				if (!allowCombos && e.keyCode in disallowedKeys) return
				
				val keyText = KeyEvent.getKeyText(e.keyCode)
				
				if (!allowCombos) {
					if (keyText in hotKeyList) {
						feedback?.invoke("Hotkey $keyText is already in use")
						return
					}
					
					field.text = keyText
					return
				}
				
				// Combo mode
				val modifiers = mutableListOf<String>()
				if (e.isControlDown) modifiers.add("Ctrl")
				if (e.isAltDown) modifiers.add("Alt")
				if (e.isShiftDown) modifiers.add("Shift")
				if (e.isMetaDown) modifiers.add("Meta")
				
				if (modifiers.isEmpty()) return
				
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
	
	private fun setupHotKeyListener() {
		val disallowed = listOf(
			KeyEvent.VK_ENTER, 	 KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_CONTROL, 		KeyEvent.VK_ALT,
			KeyEvent.VK_META, 	 KeyEvent.VK_SPACE, 	KeyEvent.VK_CONTEXT_MENU, 	KeyEvent.VK_ESCAPE,
			KeyEvent.VK_UP, 	 KeyEvent.VK_DOWN, 		KeyEvent.VK_LEFT, 			KeyEvent.VK_RIGHT,
			KeyEvent.VK_HOME, 	 KeyEvent.VK_INSERT, 	KeyEvent.VK_DELETE,
			KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_END,
			KeyEvent.VK_SHIFT
		)
		
		setupKeyListener(hotkeyField, false, disallowed)
	}
	
	override fun save(action: Action): String? {
		action.hotkey = hotkeyField.text
		action.globalKey = globalShortcutField.text
		
		return null
	}
	
	override fun setData(action: Action) {
		hotkeyField.text = action.hotkey
		globalShortcutField.text = action.globalKey
	}
	
	fun setFeedbacker(feedback: (String) -> Unit) {
		this.feedback = feedback
	}
	
}