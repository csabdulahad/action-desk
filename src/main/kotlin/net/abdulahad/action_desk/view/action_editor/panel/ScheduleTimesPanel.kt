package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatTextField
import org.jdesktop.swingx.HorizontalLayout
import org.jdesktop.swingx.VerticalLayout
import org.jdesktop.swingx.prompt.PromptSupport
import java.awt.Component
import java.awt.Container
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class ScheduleTimesPanel(
	private val defaultTime: String = "",
	addButtonText: String = "+ Add time"
) : JPanel() {
	
	private val rowsPanel = JPanel(VerticalLayout(6))
	private val addButton = JButton(addButtonText)
	private val fields = mutableListOf<FlatTextField>()
	
	private val looseFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
	private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
	
	init {
		layout = VerticalLayout(6)
		add(rowsPanel)
		
		addButton.addActionListener {
			addTime(defaultTime)
		}
		
		setTimes(listOf(defaultTime))
	}
	
	fun setTimes(times: List<String>) {
		rowsPanel.removeAll()
		fields.clear()
		
		val cleanTimes = times
			.map { it.trim() }
			.ifEmpty { listOf(defaultTime) }
		
		cleanTimes.forEach { addTime(it, refresh = false) }
		refresh()
	}
	
	fun getNormalizedTimes(): List<String> {
		return fields
			.mapNotNull { parseTime(it.text) }
			.map { formatTime(it) }
			.distinct()
			.sorted()
	}
	
	fun hasEnteredTimes(): Boolean {
		return fields.any { it.text?.trim()?.isNotEmpty() == true }
	}
	
	fun validateTimes(label: String = "Times", required: Boolean = true): String? {
		val seen = mutableSetOf<String>()
		
		if (fields.isEmpty()) {
			return if (required) "$label must contain at least one time" else null
		}
		
		if (!required && !hasEnteredTimes()) {
			return null
		}
		
		for (field in fields) {
			val raw = field.text?.trim().orEmpty()
			
			if (raw.isEmpty()) {
				return "$label cannot contain an empty time"
			}
			
			val time = parseTime(raw) ?: return "$label must be in HH:mm format"
			val formatted = formatTime(time)
			
			if (!seen.add(formatted)) {
				return "$label contains duplicate time $formatted"
			}
		}
		
		normalizeFields()
		return null
	}
	
	override fun setEnabled(enabled: Boolean) {
		super.setEnabled(enabled)
		setEnabledRecursive(rowsPanel, enabled)
		addButton.isEnabled = enabled
	}
	
	private fun addTime(value: String = defaultTime, refresh: Boolean = true) {
		val field = FlatTextField()
		PromptSupport.setPrompt("HH:mm", field)
		field.columns = 6
		installTimeInputFilter(field)
		field.text = value
		
		val removeButton = JButton("Remove")
		val row = JPanel(HorizontalLayout(8))
		row.add(field)
		row.add(removeButton)
		
		fields.add(field)
		rowsPanel.add(row)
		
		removeButton.addActionListener {
			fields.remove(field)
			rowsPanel.remove(row)
			
			if (fields.isEmpty()) {
				addTime("", refresh = false)
			}
			
			refresh()
		}
		
		if (refresh) {
			refresh()
			SwingUtilities.invokeLater { field.requestFocusInWindow() }
		}
	}
	
	private fun installTimeInputFilter(field: FlatTextField) {
		val document = field.document
		
		if (document is AbstractDocument) {
			document.documentFilter = object : DocumentFilter() {
				override fun insertString(fb: FilterBypass, offset: Int, string: String?, attr: AttributeSet?) {
					replace(fb, offset, 0, string, attr)
				}
				
				override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String?, attrs: AttributeSet?) {
					val current = fb.document.getText(0, fb.document.length)
					val proposed = current.substring(0, offset) + (text ?: "") + current.substring(offset + length)
					val formatted = formatTypingText(proposed)
					fb.replace(0, fb.document.length, formatted, attrs)
				}
				
				override fun remove(fb: FilterBypass, offset: Int, length: Int) {
					val current = fb.document.getText(0, fb.document.length)
					val proposed = current.removeRange(offset, offset + length)
					val formatted = formatTypingText(proposed)
					fb.replace(0, fb.document.length, formatted, null)
				}
			}
		}
	}
	
	private fun formatTypingText(value: String): String {
		val text = value.trim()
		
		if (text.contains(":")) {
			val parts = text.split(":", limit = 2)
			val hour = parts.getOrNull(0).orEmpty().filter { it.isDigit() }.take(2)
			val minute = parts.getOrNull(1).orEmpty().filter { it.isDigit() }.take(2)
			return "$hour:$minute"
		}
		
		val digits = text.filter { it.isDigit() }.take(4)
		
		return when {
			digits.length <= 2 -> digits
			else -> digits.substring(0, 2) + ":" + digits.substring(2)
		}
	}
	
	private fun normalizeFields() {
		val normalized = getNormalizedTimes()
		setTimes(normalized)
	}
	
	private fun parseTime(value: String?): LocalTime? {
		if (value.isNullOrBlank()) return null
		
		val text = value.trim()
		
		return runCatching { LocalTime.parse(text) }.getOrNull()
			?: runCatching { LocalTime.parse(text, looseFormatter) }.getOrNull()
	}
	
	private fun formatTime(time: LocalTime): String {
		return time.format(displayFormatter)
	}
	
	private fun refresh() {
		moveAddButtonToLastRow()
		revalidate()
		repaint()
	}
	
	private fun moveAddButtonToLastRow() {
		addButton.parent?.remove(addButton)
		
		val lastRow = rowsPanel.components.lastOrNull()
		
		if (lastRow is JPanel) {
			lastRow.add(addButton)
		}
	}
	
	private fun setEnabledRecursive(component: Component, enabled: Boolean) {
		component.isEnabled = enabled
		
		if (component is Container) {
			component.components.forEach { child ->
				setEnabledRecursive(child, enabled)
			}
		}
	}
	
}
