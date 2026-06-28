package net.abdulahad.action_desk.view.settings.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import com.formdev.flatlaf.extras.components.FlatSpinner
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.engine.theme.ThemeManager
import net.abdulahad.action_desk.view.settings.SettingsPanel
import org.jdesktop.swingx.HorizontalLayout
import org.jdesktop.swingx.VerticalLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Point
import java.awt.Window
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.SwingUtilities

class WindowPanel(private val frame: Window): JPanel(), SettingsPanel {
	
	private val themeOptions = arrayOf("Light", "Dark", "System default")
	private val themeDropdown = JComboBox(themeOptions)
	
	private val widthField  = FlatSpinner()
	private val heightField = FlatSpinner()
	
	private val showADWindowInCenter = FlatCheckBox()

	init {
		layout = VerticalLayout(6)
		setupPlaceholders()
		addFields()
	}
	
	private fun setupPlaceholders() {
		showADWindowInCenter.text = "Show ActionDesk window in center"
	}
	
	private fun addFields() {
		themeDropdown()
		windowSize()
		
		add(showADWindowInCenter)
	}
	
	private fun themeDropdown() {
		themeDropdown.preferredSize = Dimension(150, 28)
		
		val panel = JPanel().apply {
			layout = VerticalLayout(6)
			
			add(JLabel("Theme"))
			
			add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
				border = null
				add(themeDropdown)
			})
		}
		
		add(panel)
	}
	
	private fun selectAllOnSpinnerFocus(spinner: FlatSpinner) {
		val editor = spinner.editor as JSpinner.DefaultEditor
		val textField = editor.textField
		
		textField.addFocusListener(object : FocusAdapter() {
			override fun focusGained(e: FocusEvent) {
				// Use invokeLater to ensure the selection happens
				// after the focus event has finished processing
				SwingUtilities.invokeLater {
					textField.selectAll()
				}
			}
		})
	}
	
	private fun windowSize() {
		widthField.apply {
			val modelW = SpinnerNumberModel(400, 300, 800, 5)
			model = modelW
			putClientProperty("FlatLaf.style", "minimumWidth: 90")
			
			selectAllOnSpinnerFocus(this)
		}
		
		heightField.apply {
			val modelH = SpinnerNumberModel(320, 300, 800, 5)
			model = modelH
			putClientProperty("FlatLaf.style", "minimumWidth: 90")
			
			selectAllOnSpinnerFocus(this)
		}
		
		val panel = JPanel(VerticalLayout(6)).apply {
			add(JLabel("Window Size"))
			
			val sizeInputPanel = JPanel(HorizontalLayout(6)).apply {
				add(this@WindowPanel.widthField)
				add(JLabel("X"))
				add(this@WindowPanel.heightField)
			}
			
			add(sizeInputPanel)
		}
		
		add(panel)
	}
	
	override fun initUI() {
		/*
		 * Theme
		 * */
		val theme = AppConfig.getTheme()
		themeDropdown.selectedItem = ThemeManager.displayName(theme)
		
		/*
		 * Window size
		 * */
		val point = AppConfig.getWindowSize()
		widthField.value  = point.x
		heightField.value = point.y
		
		/*
		 * Window in center
		 * */
		showADWindowInCenter.isSelected = AppConfig.getShowWindowInCenter()
	}
	
	override fun saveConfig() {
		AppConfig.setTheme(themeDropdown.selectedItem as String, true)
		AppConfig.setWindowSize(Point(widthField.value as Int, heightField.value as Int), true)
		AppConfig.setShowWindowInCenter(showADWindowInCenter.isSelected)
	}
	
}