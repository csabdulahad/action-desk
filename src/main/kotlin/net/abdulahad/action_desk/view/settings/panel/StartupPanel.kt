package net.abdulahad.action_desk.view.settings.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.view.settings.SettingsPanel
import org.jdesktop.swingx.VerticalLayout
import java.awt.Window
import javax.swing.JPanel

class StartupPanel(private val frame: Window): JPanel(), SettingsPanel {
	
	private val autoRunCheckbox = FlatCheckBox()
	private val startMiniCheckbox = FlatCheckBox()
	
	init {
		layout = VerticalLayout(8)
		setupPlaceholders()
		addFields()
	}
	
	private fun setupPlaceholders() {
		autoRunCheckbox.text = "Launch with Windows"
		startMiniCheckbox.text = "Start minimized"
	}
	
	private fun addFields() {
		add(autoRunCheckbox)
		add(startMiniCheckbox)
	}
	
	override fun initUI() {
		autoRunCheckbox.isSelected = AppConfig.getAutoRun()
		startMiniCheckbox.isSelected = AppConfig.getStartMinimized()
	}
	
	override fun saveConfig() {
		AppConfig.setAutoRun(autoRunCheckbox.isSelected, true)
		AppConfig.setStartMinimized(startMiniCheckbox.isSelected, true)
	}
	
}