package net.abdulahad.action_desk.view.action_editor

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.dao.ActionDao
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.view.action_editor.panel.CommandPanel
import net.abdulahad.action_desk.view.action_editor.panel.GeneralPanel
import net.abdulahad.action_desk.view.action_editor.panel.ProcessPanel
import net.abdulahad.action_desk.view.action_editor.panel.ShortcutPanel
import net.abdulahad.action_desk.view.action_editor.panel.WindowPanel
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.MatteBorder

class ActionEditor(parentFrame: Window, private var action: Action? = null) : JDialog(parentFrame) {
	
	private val listItems = listOf("General", "Command", "Process", "Window", "Shortcuts")
	
	private lateinit var rightPanel: JPanel
	private lateinit var leftScrollPane: JScrollPane
	
	private val cardLayout = CardLayout()
	private val jList = JList<String>()
	
	private val feedbackField = JLabel("")
	
	private lateinit var generalPanel: GeneralPanel
	private lateinit var commandPanel: CommandPanel
	private lateinit var processPanel: ProcessPanel
	private lateinit var windowPanel: WindowPanel
	private lateinit var shortcutPanel: ShortcutPanel
	
	private var updateCallback: (() -> Unit)? = null
	
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
			dividerLocation = 120
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
		windowPanel.setData(action!!)
		shortcutPanel.setData(action!!)
	}
	
	private fun initPanels() {
		generalPanel = GeneralPanel(this)
		commandPanel = CommandPanel()
		processPanel = ProcessPanel()
		windowPanel = WindowPanel()
		
		shortcutPanel = ShortcutPanel()
		shortcutPanel.setFeedbacker(::setFeedback)
	}
	
	private fun setupDialog() {
		App.applyCloseIcon()
		
		title = "New Action"
		modalityType = ModalityType.APPLICATION_MODAL
		defaultCloseOperation = DO_NOTHING_ON_CLOSE
		minimumSize = Dimension(375, 375)
		isResizable = false
		layout = BorderLayout()
		
		addWindowListener(object: WindowAdapter() {
			override fun windowClosing(e: WindowEvent?) {
				dispose()
				this@ActionEditor.parent.isVisible = true
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
			putClientProperty("JComponent.focusWidth", 0)
			border = MatteBorder(Insets(0, 0, 0, 1), UIManager.getColor("Component.borderColor"))
		}
	}
	
	private fun setupRightPanel() {
		rightPanel = JPanel(cardLayout)
		
		mapOf (
			"General"   to generalPanel,
			"Command"   to commandPanel,
			"Process"   to processPanel,
			"Window"   to windowPanel,
			"Shortcuts" to shortcutPanel,
		).forEach { (key, panel) ->
			val x = JScrollPane(panel)
			x.verticalScrollBar.unitIncrement = 12
			x.border = null
			panel.border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
			x.preferredSize = Dimension(350, preferredSize.height)
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
			dispose()
			this@ActionEditor.parent.isVisible = true
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
		
		feedbackField.foreground = UIManager.getColor("error_color")
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
			windowPanel,
			shortcutPanel,
		)
		
		for (panel in panels) {
			val reply = panel.save(action!!) ?: continue
			
			val (panelName, msg) = reply.split(":", limit = 2)
			
			selectLeftMenu(panelName)
			setFeedback(msg)
			
			return
		}
		
		ActionDao.save(action!!)
		
		if (updateCallback != null) updateCallback!!.invoke()
		
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