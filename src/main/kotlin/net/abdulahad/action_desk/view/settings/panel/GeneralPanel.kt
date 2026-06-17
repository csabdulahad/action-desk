package net.abdulahad.action_desk.view.settings.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import com.formdev.flatlaf.extras.components.FlatTextField
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.helper.Icons
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.lib.view.ShortcutFieldAdapter
import net.abdulahad.action_desk.view.settings.SettingsPanel
import org.jdesktop.swingx.HorizontalLayout
import org.jdesktop.swingx.VerticalLayout
import java.awt.Window
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class GeneralPanel(private val frame: Window): JPanel(), SettingsPanel {

	private lateinit var shortcutAdapter: ShortcutFieldAdapter
	
	private val alwaysOnTopCheckbox = FlatCheckBox()
	private val hideAfterActionCheckbox = FlatCheckBox()
	
	private val adGlobalShortcutField = FlatTextField()
	private val winCheckBox = FlatCheckBox()
	
	private val psBinOptions  = arrayOf("PowerShell 5", "PowerShell 7")
	private val psBinDropdown = JComboBox(psBinOptions)

	init {
		layout = VerticalLayout(8)
		setupPlaceholders()
		addFields()
	}
	
	private fun setupPlaceholders() {
		alwaysOnTopCheckbox.text = "Always on top"
		hideAfterActionCheckbox.text = "Close dialog after action"
		
		adGlobalShortcutField.toolTipText = "Action Desk global shortcut"
	}
	
	private fun addFields() {
		setupPSBinPanel()
		
		add(alwaysOnTopCheckbox)
		add(hideAfterActionCheckbox)
		
		setupGlobalShortcutPanel()
	}
	
	private fun setupPSBinPanel() {
		val panel = JPanel(VerticalLayout()).apply {
			val label = JLabel("PowerShell binary").apply {
				border = EmptyBorder(0, 0, 0, 0)
			}
			
			add(label)
			add(psBinDropdown)
		}
		
		add(panel)
	}
	
	private fun setupGlobalShortcutPanel() {
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
			add(adGlobalShortcutField)
			add(windowKeyCheckbox)
		}
		
		shortcutAdapter = ShortcutFieldAdapter(adGlobalShortcutField, { winCheckBox.isSelected }, ::checkKey).apply {
			bindWindowsKey(winCheckBox)
		}
		
		add(panel)
	}
	
	private fun checkKey(keyText: String): Boolean {
		return true
	}
	
	override fun initUI() {
		alwaysOnTopCheckbox.isSelected = AppConfig.getAlwaysOnTop()
		hideAfterActionCheckbox.isSelected = AppConfig.getHideAfterAction()
		shortcutAdapter.setValue(AppConfig.getADHotkey())
		
		psBinDropdown.selectedItem = translatePSBinName(AppConfig.getPSBin())
	}
	
	override fun saveConfig() {
		AppConfig.setAlwaysOnTop(alwaysOnTopCheckbox.isSelected, true)
		AppConfig.setHideAfterAction(hideAfterActionCheckbox.isSelected, true)
		AppConfig.setADHotkey(shortcutAdapter.getValue())
		
		val psBin = translatePSBin(psBinDropdown.selectedItem as String)
		AppConfig.setPSBin(psBin)
	}
	
	private fun translatePSBin(value: String): String {
		return if (value == "PowerShell 7") "pwsh" else "powershell"
	}
	
	private fun translatePSBinName(value: String): String {
		return if (value == "pwsh") "PowerShell 7" else "PowerShell 5"
	}
	
}