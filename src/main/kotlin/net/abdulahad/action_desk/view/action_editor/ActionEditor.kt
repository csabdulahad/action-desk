package net.abdulahad.action_desk.view.action_editor

import net.abdulahad.action_desk.engine.security.ProtectedActionCrypto
import net.abdulahad.action_desk.engine.security.SecurityService
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.repo.action.ActionRepo
import net.abdulahad.action_desk.view.action_editor.panel.*
import net.abdulahad.action_desk.view.settings.dialog.SecurityPasswordDialog
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.MatteBorder

class ActionEditor(
	parentFrame: Window,
	private var action: Action? = null,
	unlockedPassword: String? = null
) : JDialog(parentFrame) {
	
	companion object {
		private const val LEFT_MENU_WIDTH = 140
		private const val CONTENT_WIDTH   = 420
		private const val CONTENT_HEIGHT  = 380
	}
	
	private val listItems = listOf("General", "Command", "Process", "Safety & Security", "Window", "Schedule")
	
	private lateinit var rightPanel: JPanel
	private lateinit var leftScrollPane: JScrollPane
	
	private val cardLayout = CardLayout()
	private val jList = JList<String>()
	
	private val feedbackField = JLabel("")
	
	private lateinit var generalPanel: GeneralPanel
	private lateinit var commandPanel: CommandPanel
	private lateinit var processPanel: ProcessPanel
	private lateinit var safetySecurityPanel: SafetySecurityPanel
	private lateinit var windowPanel: WindowPanel
	private lateinit var schedulePanel: SchedulePanel
	
	private var updateCallback: (() -> Unit)? = null
	private var editSessionPassword: String? = unlockedPassword
	
	fun setUploadCallback(callback: () -> Unit) {
		updateCallback = callback
	}
	
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
			dividerLocation = LEFT_MENU_WIDTH
			isEnabled = false
			dividerSize = 0
		}
		
		if (action != null) {
			loadActionIntoView()
		}
		
		add(splitPane, BorderLayout.CENTER)
	}
	
	private fun loadActionIntoView() {
		title = "Update Action"
		generalPanel.setData(action!!)
		commandPanel.setData(action!!)
		processPanel.setData(action!!)
		safetySecurityPanel.setData(action!!)
		windowPanel.setData(action!!)
		schedulePanel.setData(action!!)
	}
	
	private fun initPanels() {
		generalPanel = GeneralPanel(this)
		generalPanel.setFeedbacker(::setFeedback)
		
		commandPanel = CommandPanel()
		processPanel = ProcessPanel()
		safetySecurityPanel = SafetySecurityPanel()
		windowPanel = WindowPanel()
		schedulePanel = SchedulePanel()
	}
	
	private fun setupDialog() {
		title = "New Action"
		modalityType = ModalityType.APPLICATION_MODAL
		defaultCloseOperation = DO_NOTHING_ON_CLOSE
		minimumSize = Dimension(LEFT_MENU_WIDTH + CONTENT_WIDTH, CONTENT_HEIGHT)
		isResizable = false
		layout = BorderLayout()
		
		addWindowListener(object: WindowAdapter() {
			override fun windowClosing(e: WindowEvent?) {
				closeEditor()
			}
		})
	}
	
	private fun setupLeftList() {
		jList.setListData(listItems.toTypedArray())
		jList.isOpaque = true
		jList.selectionMode = ListSelectionModel.SINGLE_SELECTION
		jList.selectedIndex = 0
		jList.fixedCellHeight = 30
		
		leftScrollPane = JScrollPane(jList).apply {
			isOpaque = true
			preferredSize = Dimension(LEFT_MENU_WIDTH, CONTENT_HEIGHT)
			putClientProperty("JComponent.focusWidth", 0)
			border = MatteBorder(Insets(0, 0, 0, 1), UIManager.getColor("Component.borderColor"))
		}
	}
	
	private fun setupRightPanel() {
		rightPanel = JPanel(cardLayout)
		
		mapOf (
			"General"             to generalPanel,
			"Command"             to commandPanel,
			"Process"             to processPanel,
			"Safety & Security"   to safetySecurityPanel,
			"Schedule"            to schedulePanel,
			"Window"              to windowPanel,
		).forEach { (key, panel) ->
			val x = JScrollPane(panel)
			x.verticalScrollBar.unitIncrement = 12
			x.border = null
			panel.border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
			x.preferredSize = Dimension(CONTENT_WIDTH, CONTENT_HEIGHT)
			rightPanel.add(x, key)
		}
		
		// Show default
		cardLayout.show(rightPanel, "General")
	}
	
	private fun setupBottomPanel() {
		// Bottom button panel
		val saveButton = JButton("Save")
		saveButton.addActionListener {
			startSaving()
		}
		
		val cancelButton = JButton("Cancel")
		cancelButton.addActionListener {
			closeEditor()
		}
		
		val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
		buttonPanel.add(saveButton)
		buttonPanel.add(cancelButton)
		
		rootPane.defaultButton = saveButton
		
		
		// Layout
		val bottomPanel = JPanel(BorderLayout())
		bottomPanel.border = MatteBorder(Insets(1, 0, 0, 0), UIManager.getColor("Component.borderColor"))
		bottomPanel.isOpaque = false
		bottomPanel.add(buttonPanel, BorderLayout.LINE_END)
		
		feedbackField.foreground = UIManager.getColor("AD.errorColor")
		feedbackField.border = BorderFactory.createEmptyBorder(0, 6, 0, 0)
		bottomPanel.add(feedbackField, BorderLayout.LINE_START)
		
		add(bottomPanel, BorderLayout.PAGE_END)
	}
	
	private fun setFeedback(msg: String) {
		feedbackField.text = msg
	}
	
	private fun addLeftMenuListener() {
		jList.addListSelectionListener {
			val selected = jList.selectedValue
			if (selected != null) {
				cardLayout.show(rightPanel, selected)
			}
		}
	}
	
	private fun selectLeftMenu(name: String? = null) {
		val index = listItems.indexOf(name)
		
		if (index != -1) {
			jList.selectedIndex = index
		}
		
		cardLayout.show(rightPanel, name)
	}
	
	private fun startSaving() {
		if (action == null) {
			action = Action()
		}
		
		val panels = listOf<ActionEditorPanel>(
			generalPanel,
			commandPanel,
			processPanel,
			safetySecurityPanel,
			windowPanel,
			schedulePanel,
		)
		
		for (panel in panels) {
			val reply = panel.save(action!!) ?: continue
			
			val (panelName, msg) = reply.split(":", limit = 2)
			
			selectLeftMenu(panelName)
			setFeedback(msg)
			
			return
		}
		
		if (!prepareActionSecurityForSave(action!!)) {
			return
		}
		
		ActionRepo.save(action) { savedAction ->
			schedulePanel.persistForAction(savedAction)
		}
		
		if (updateCallback != null) updateCallback!!.invoke()
		
		closeEditor()
	}
	
	private fun prepareActionSecurityForSave(action: Action): Boolean {
		if (!action.passwordProtected) {
			action.encryptedPayload = ""
			return true
		}
		
		if (!SecurityService.hasPassword()) {
			selectLeftMenu("Safety & Security")
			setFeedback("Set security password in Settings first")
			return false
		}
		
		val password = editSessionPassword ?: askPasswordForProtectedSave() ?: run {
			selectLeftMenu("Safety & Security")
			setFeedback("Password is required to protect this action")
			return false
		}
		
		return try {
			ProtectedActionCrypto.encryptActionPayload(action, password)
			true
		} catch (e: Exception) {
			selectLeftMenu("Safety & Security")
			setFeedback(e.message ?: "Protected action could not be encrypted")
			false
		}
	}
	
	private fun askPasswordForProtectedSave(): String? {
		var password: String? = null
		
		val verified = SecurityPasswordDialog.showVerifiedPassword(
			parent = this,
			title = "Protect Action",
			invalidPasswordMessage = "Password did not work.",
			verifier = { input ->
				val success = SecurityService.verifyPassword(input)
				if (success) {
					password = input
				}
				success
			}
		)
		
		return if (verified) password else null
	}
	
	private fun closeEditor() {
		editSessionPassword = null
		dispose()
		this@ActionEditor.parent.isVisible = true
	}
	
	fun showIt() {
		pack()
		setLocationRelativeTo(owner)
		this@ActionEditor.parent.isVisible = false
		isVisible = true
	}
	
}
