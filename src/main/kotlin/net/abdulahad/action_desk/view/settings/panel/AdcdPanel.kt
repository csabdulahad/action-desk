package net.abdulahad.action_desk.view.settings.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import com.formdev.flatlaf.extras.components.FlatSpinner
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.view.settings.SettingsPanel
import org.jdesktop.swingx.VerticalLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Window
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.JTextArea
import javax.swing.SpinnerNumberModel
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

class AdcdPanel(private val frame: Window): JPanel(), SettingsPanel {
	
	private val adcdEnabledCheckbox = FlatCheckBox()
	private val adcdPortField = FlatSpinner()
	private val adcdAllowNetworkCheckbox = FlatCheckBox()
	private val adcdMuteSoundCheckbox = FlatCheckBox()
	private val adcdDisableDialogCheckbox = FlatCheckBox()
	
	init {
		layout = VerticalLayout(8)
		
		setupPlaceholders()
		addFields()
		addListeners()
	}
	
	private fun setupPlaceholders() {
		adcdEnabledCheckbox.text = "Enable ADCD"
		adcdEnabledCheckbox.toolTipText = "Allow local scripts and actions to open JSON-driven native dialogs."
		
		adcdAllowNetworkCheckbox.text = "Allow network access"
		adcdAllowNetworkCheckbox.toolTipText = "Allow devices on the same network to access ADCD using this computer's LAN IP."
		
		adcdMuteSoundCheckbox.text = "Mute ADCD sounds"
		adcdMuteSoundCheckbox.toolTipText = "Ignore sound playback requested by ADCD dialog JSON."
		
		adcdDisableDialogCheckbox.text = "Disable ADCD dialogs"
		adcdDisableDialogCheckbox.toolTipText = "Return a disabled response for ADCD dialog requests without showing a dialog."
		
		adcdPortField.apply {
			model = SpinnerNumberModel(AppConfig.DEFAULT_ADCD_PORT, 1024, 65535, 1)
			editor = JSpinner.NumberEditor(this, "#")
			preferredSize = Dimension(110, 28)
			putClientProperty("FlatLaf.style", "minimumWidth: 110")
		}
		
		selectAllOnSpinnerFocus(adcdPortField)
	}
	
	private fun addFields() {
		val infoField = wrappedHint(
			"ADCD\n\nListens on localhost at the selected port.\n" +
			"Enable network access to allow LAN devices to call it using your PC IP", 260)
		
		add(infoField)
		
		add(adcdEnabledCheckbox.apply { border = EmptyBorder(5, 0, 0, 0) }, BorderLayout.EAST)
		add(adcdAllowNetworkCheckbox)
		add(adcdMuteSoundCheckbox)
		add(adcdDisableDialogCheckbox)
		
		val portPanel = JPanel(VerticalLayout(4)).apply {
			val label = JLabel("Port").apply {
				border = EmptyBorder(5, 0, 0, 0)
			}
			
			val inputRow = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
				add(adcdPortField)
			}
			
			add(label)
			add(inputRow)
		}
		
		add(portPanel)
	}
	
	private fun addListeners() {
		adcdEnabledCheckbox.addActionListener {
			syncEnabledState()
		}
	}
	
	private fun syncEnabledState() {
		val enabled = adcdEnabledCheckbox.isSelected
		
		adcdPortField.isEnabled = enabled
		adcdAllowNetworkCheckbox.isEnabled = enabled
		adcdMuteSoundCheckbox.isEnabled = enabled
		adcdDisableDialogCheckbox.isEnabled = enabled
	}
	
	private fun selectAllOnSpinnerFocus(spinner: FlatSpinner) {
		val editor = spinner.editor as JSpinner.DefaultEditor
		val textField = editor.textField
		
		textField.addFocusListener(object : FocusAdapter() {
			override fun focusGained(e: FocusEvent) {
				SwingUtilities.invokeLater {
					textField.selectAll()
				}
			}
		})
	}
	
	override fun initUI() {
		adcdEnabledCheckbox.isSelected = AppConfig.getAdcdEnabled()
		adcdAllowNetworkCheckbox.isSelected = AppConfig.getAdcdAllowNetwork()
		adcdMuteSoundCheckbox.isSelected = AppConfig.getAdcdMuteSound()
		adcdDisableDialogCheckbox.isSelected = AppConfig.getAdcdDisableDialog()
		adcdPortField.value = AppConfig.getAdcdPort()
		
		syncEnabledState()
	}
	
	override fun saveConfig() {
		AppConfig.setAdcdEnabled(adcdEnabledCheckbox.isSelected)
		AppConfig.setAdcdAllowNetwork(adcdAllowNetworkCheckbox.isSelected)
		AppConfig.setAdcdMuteSound(adcdMuteSoundCheckbox.isSelected)
		AppConfig.setAdcdDisableDialog(adcdDisableDialogCheckbox.isSelected)
		AppConfig.setAdcdPort((adcdPortField.value as Number).toInt())
		
		AppConfig.applyAdcd()
	}
	
	private fun wrappedHint(text: String, width: Int = 320): JTextArea {
		return JTextArea(text).apply {
			lineWrap = true
			wrapStyleWord = true
			isEditable = false
			isFocusable = false
			isOpaque = false
			border = null
			
			font = UIManager.getFont("Label.font")
			foreground = UIManager.getColor("Label.disabledForeground")
			
			// Important: force width first so JTextArea can calculate wrapped height
			setSize(width, Short.MAX_VALUE.toInt())
			
			val preferred = preferredSize
			
			preferredSize = Dimension(width, preferred.height)
			minimumSize = Dimension(0, preferred.height)
			maximumSize = Dimension(width, preferred.height)
		}
	}
	
}