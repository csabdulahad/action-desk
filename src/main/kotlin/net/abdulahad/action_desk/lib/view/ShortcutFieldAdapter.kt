package net.abdulahad.action_desk.lib.view

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JCheckBox
import javax.swing.JTextField

class ShortcutFieldAdapter (
	private val field: JTextField,
	private val isolatedShortcut: () -> Boolean,
	private var checkKey: ((String) -> Boolean)? = null,
) {
	
	private var windowsKey: JCheckBox? = null
	
	init {
		field.isEditable = false
		
		field.addKeyListener(object : KeyAdapter() {
			override fun keyPressed(e: KeyEvent) {
				e.consume()
				
				val code = e.keyCode
				
				// 1. Handle Clear
				if (code == KeyEvent.VK_BACK_SPACE) {
					field.text = ""
					return
				}
				
				// 2. Capture Modifiers
				val modifiers = mutableListOf<String>()
				
				if (e.isControlDown) modifiers.add("Ctrl")
				if (e.isAltDown)     modifiers.add("Alt")
				if (e.isShiftDown)   modifiers.add("Shift")
				
				// 3. Get the actual key name
				val keyText = KeyEvent.getKeyText(code)
				
				// 4. Validation: Ignore if it's just a modifier key being pressed alone
				val isModifierOnly =
					code == KeyEvent.VK_CONTROL ||
					code == KeyEvent.VK_ALT ||
					code == KeyEvent.VK_SHIFT ||
					code == KeyEvent.VK_META
				
				if (isModifierOnly) return
				
				// 5. Validation: Isolated shortcut check (if no modifiers are held)
				if (!isolatedShortcut() && modifiers.isEmpty()) return
				
				// 6. Build final string
				// We add the keyText to the list and join them all at once!
				val fullCombo = (modifiers + keyText).joinToString("+")
				
				// 7. Check and Update
				if (checkKey == null || checkKey!!(fullCombo)) {
					field.text = fullCombo
				}
			}
		})
	}
	
	fun bindWindowsKey(checkbox: JCheckBox) {
		windowsKey = checkbox
		checkbox.addActionListener {
			if (!checkbox.isSelected) {
				field.text = ""
			}
		}
	}
	
	fun getValue(): String {
		return if (windowsKey == null) {
			field.text
		} else {
			if (windowsKey!!.isSelected) "Win+${field.text}" else field.text
		}
	}
	
	fun setValue(value: String) {
		val windowsShortcut = value.startsWith("Win+")
		
		field.text = if (windowsShortcut) {
			value.replaceFirst("Win+", "")
		} else {
			value
		}
		
		windowsKey?.isSelected = windowsShortcut
	}
	
}