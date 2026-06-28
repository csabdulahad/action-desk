package net.abdulahad.action_desk.view.settings

import net.abdulahad.action_desk.config.ConfigService
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.helper.Icons.toImageIcon
import net.abdulahad.action_desk.helper.ViewHelper
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.view.settings.panel.WindowPanel
import net.abdulahad.action_desk.view.settings.panel.GeneralPanel
import net.abdulahad.action_desk.view.settings.panel.AdcdPanel
import net.abdulahad.action_desk.view.settings.panel.SafetySecurityPanel
import net.abdulahad.action_desk.view.settings.panel.StartupPanel
import java.awt.*
import javax.swing.*
import javax.swing.border.MatteBorder

class Settings : JDialog(ActionDesk) {
	
	private val listItems = listOf("General", "Startup", "Window", "ADCD", "Safety and Security")
	private lateinit var rightPanel: JPanel
	private lateinit var leftScrollPane: JScrollPane
	
	private val cardLayout = CardLayout()
	private val jList = JList<String>()

	private lateinit var generalPanel: GeneralPanel
	private lateinit var startupPanel: StartupPanel
	private lateinit var windowPanel: WindowPanel
	private lateinit var adcdPanel: AdcdPanel
	private lateinit var safetySecurityPanel: SafetySecurityPanel
	
	private val feedbackLabel = JLabel(" ")
	
	val panels = mutableMapOf<String, JPanel>()
	
	init {
		setupDialog()
		initPanels()
		
		setupRightPanel()
		
		setupLeftList()
		addLeftMenuListener()
		
		setupBottomPanel()
		
		// JSplitPane (non-resizable)
		val splitPane = JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			leftScrollPane,
			rightPanel).apply {
			putClientProperty("JSplitPane.style", "grip: none")
			border = MatteBorder(Insets(1, 0, 0, 0), UIManager.getColor("Component.borderColor"))
			dividerLocation = 155
			isEnabled = false
			dividerSize = 0
		}
		
		add(splitPane, BorderLayout.CENTER)
	}
	
	private fun setupDialog() {
		title = "Settings"
		modalityType = ModalityType.APPLICATION_MODAL
		minimumSize = Dimension(560, 380)
		isResizable = false
		layout = BorderLayout()
		
		val icon = "actionDesk".icon(32).toImageIcon()
		setIconImage(icon.image)
	}
	
	private fun initPanels() {
		generalPanel = GeneralPanel(this)
		startupPanel = StartupPanel(this)
		windowPanel = WindowPanel(this)
		adcdPanel = AdcdPanel(this)
		safetySecurityPanel = SafetySecurityPanel(this, ::showFeedback)
	}
	
	private fun setupRightPanel() {
		rightPanel = JPanel(cardLayout)
		
		panels["General"] 	 = generalPanel
		panels["Startup"] 	 = startupPanel
		panels["Window"] 	 = windowPanel
		panels["ADCD"] 		 = adcdPanel
		panels["Safety and Security"] = safetySecurityPanel
		
		panels.forEach { (key, panel) ->
			(panel as SettingsPanel).initUI()
			panel.border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
			
			val x = JScrollPane(panel)
			x.verticalScrollBar.unitIncrement = 12
			x.border = null
			x.preferredSize = Dimension(350, preferredSize.height)
			
			rightPanel.add(x, key)
		}
		
		// Show default
		cardLayout.show(rightPanel, "General")
	}
	
	private fun setupLeftList() {
		jList.setListData(listItems.toTypedArray())
		
		jList.isOpaque = true
		jList.selectionMode = ListSelectionModel.SINGLE_SELECTION
		jList.selectedIndex = 0
		jList.fixedCellHeight = 30
		
		leftScrollPane = JScrollPane(jList).apply {
			isOpaque = true
			putClientProperty("JComponent.focusWidth", 0)
			border = MatteBorder(Insets(0, 0, 0, 1), UIManager.getColor("Component.borderColor"))
		}
	}
	
	private fun addLeftMenuListener() {
		jList.addListSelectionListener {
			val selected = jList.selectedValue
			if (selected != null) {
				cardLayout.show(rightPanel, selected)
			}
		}
	}
	
	private fun setupBottomPanel() {
		// Bottom button panel
		val saveButton = JButton("Save")
		saveButton.addActionListener {
			panels.forEach { (_, panel) ->
				(panel as SettingsPanel).saveConfig()
			}
			
			ConfigService.flush()
			ViewHelper.closeDialogWithEvent(this)
		}
		
		val cancelButton = JButton("Cancel")
		cancelButton.addActionListener {
			ViewHelper.closeDialogWithEvent(this)
		}
		
		val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
		buttonPanel.add(saveButton)
		buttonPanel.add(cancelButton)
		
		rootPane.defaultButton = saveButton
		
		feedbackLabel.border = BorderFactory.createEmptyBorder(0, 12, 0, 0)
		feedbackLabel.foreground = UIManager.getColor("Label.disabledForeground")
		
		// Layout
		val bottomPanel = JPanel(BorderLayout())
		bottomPanel.border = MatteBorder(Insets(1, 0, 0, 0), UIManager.getColor("Component.borderColor"))
		bottomPanel.isOpaque = false
		bottomPanel.add(feedbackLabel, BorderLayout.CENTER)
		bottomPanel.add(buttonPanel, BorderLayout.LINE_END)
		
		add(bottomPanel, BorderLayout.PAGE_END)
	}
	
	private fun showFeedback(message: String, error: Boolean) {
		feedbackLabel.text = message.ifBlank { " " }
		feedbackLabel.foreground = if (error) {
			UIManager.getColor("Component.error.focusedBorderColor") ?: Color(180, 0, 0)
		} else {
			UIManager.getColor("Label.foreground")
		}
	}
	
}