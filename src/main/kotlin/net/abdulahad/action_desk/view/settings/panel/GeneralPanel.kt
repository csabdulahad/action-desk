package net.abdulahad.action_desk.view.settings.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import net.abdulahad.action_desk.capitalizeFirst
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.view.settings.SettingsPanel
import org.jdesktop.swingx.HorizontalLayout
import org.jdesktop.swingx.VerticalLayout
import java.awt.Window
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class GeneralPanel(private val frame: Window): JPanel(), SettingsPanel {

	private val focusSearchCheckbox = FlatCheckBox()
	private val alwaysOnTopCheckbox = FlatCheckBox()
	private val hideAfterActionCheckbox = FlatCheckBox()
	
	private val themeOptions = arrayOf("Light", "Dark")
	private val themeDropdown = JComboBox(themeOptions)

	init {
		layout = VerticalLayout(8)
		setupPlaceholders()
		addFields()
	}
	
	private fun setupPlaceholders() {
		focusSearchCheckbox.text = "Focus search on open"
		alwaysOnTopCheckbox.text = "Always on top"
		hideAfterActionCheckbox.text = "Close dialog after action"
	}
	
	private fun addFields() {
		add(focusSearchCheckbox)
		add(alwaysOnTopCheckbox)
		add(hideAfterActionCheckbox)
		
		themeDropdown()
	}
	
	private fun themeDropdown() {
		val panel = JPanel().apply {
			layout = HorizontalLayout(12)
			
			add(JLabel("Theme"))
			add(themeDropdown)
		}
		
		add(panel)
	}
	
	override fun initUI() {
		focusSearchCheckbox.isSelected = AppConfig.getSearchFocus()
		alwaysOnTopCheckbox.isSelected = AppConfig.getAlwaysOnTop()
		hideAfterActionCheckbox.isSelected = AppConfig.getHideAfterAction()
		
		val theme = AppConfig.getTheme().capitalizeFirst()
		themeDropdown.selectedItem = theme
	}
	
	override fun saveConfig() {
		AppConfig.setSearchFocus(focusSearchCheckbox.isSelected, true)
		AppConfig.setAlwaysOnTop(alwaysOnTopCheckbox.isSelected, true)
		AppConfig.setHideAfterAction(hideAfterActionCheckbox.isSelected, true)
		
		AppConfig.setTheme(themeDropdown.selectedItem as String, true)
	}
	
}