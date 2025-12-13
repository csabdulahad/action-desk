package net.abdulahad.action_desk.view.settings

import net.abdulahad.action_desk.config.ConfigService
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.helper.ViewHelper
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.view.settings.panel.GeneralPanel
import net.abdulahad.action_desk.view.settings.panel.StartupPanel
import java.awt.*
import javax.swing.*
import javax.swing.border.MatteBorder

class Settings() : JDialog(ActionDesk) {
	
	private val listItems = listOf("General", "Startup")
	private lateinit var rightPanel: JPanel
	private lateinit var leftScrollPane: JScrollPane
	
	private val cardLayout = CardLayout()
	private val jList = JList<String>()

	private lateinit var generalPanel: GeneralPanel
	private lateinit var startupPanel: StartupPanel
	
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
			dividerLocation = 120
			isEnabled = false
			dividerSize = 0
		}
		
		add(splitPane, BorderLayout.CENTER)
	}
	
	private fun setupDialog() {
		title = "Settings"
		modalityType = ModalityType.APPLICATION_MODAL
		minimumSize = Dimension(500, 360)
		isResizable = false
		layout = BorderLayout()
		
		val icon = "actionDesk".icon(32)
		setIconImage(icon.image)
	}
	
	private fun initPanels() {
		generalPanel = GeneralPanel(this)
		startupPanel = StartupPanel(this)
	}
	
	private fun setupRightPanel() {
		rightPanel = JPanel(cardLayout)
		
		
		panels["General"] = generalPanel
		panels["Startup"] = startupPanel
		
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
		
		// Layout
		val bottomPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
		bottomPanel.border = MatteBorder(Insets(1, 0, 0, 0), UIManager.getColor("Component.borderColor"))
		bottomPanel.isOpaque = false
		bottomPanel.add(buttonPanel)
		
		add(bottomPanel, BorderLayout.PAGE_END)
	}
	
}