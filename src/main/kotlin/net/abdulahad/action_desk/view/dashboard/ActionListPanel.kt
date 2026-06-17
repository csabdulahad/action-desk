package net.abdulahad.action_desk.view.dashboard

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatPopupMenu
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.engine.action.ActionRunner
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.helper.Icons
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.lib.util.Alert
import net.abdulahad.action_desk.lib.findByName
import net.abdulahad.action_desk.lib.view.JPanel2
import net.abdulahad.action_desk.lib.view.jlist.ListView
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.model.Action.Companion.logFolder
import net.abdulahad.action_desk.onUI
import net.abdulahad.action_desk.repo.action.ActionRepo
import net.abdulahad.action_desk.repo.action.ActionRepoListener
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.view.ActionDeskPanel
import net.abdulahad.action_desk.engine.notification.NotificationManager
import net.abdulahad.action_desk.view.action_editor.ActionEditor
import org.jdesktop.swingx.HorizontalLayout
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.random.Random

class ActionListPanel: JPanel(BorderLayout()), ActionDeskPanel, ActionRepoListener {
	
	private var listView: ListView = ListView()
	private val allItems: MutableList<Action> = mutableListOf()
	
	private lateinit var popupMenu: JPopupMenu
	
	private var selectedAction: Action? = null
	
	init {
		ActionRepo.addListener(this)
		
		setEmptyView()
		listView.setItemBorder(10, 12, 10, 12)
		listView.setOnFilterFinished { count ->
			val word = if (count > 1) "actions" else "action"
			ActionDesk.setMessage("Showing $count $word")
		}
		setupListHoverCallbacks()
		
		add(listView.scrollPane, BorderLayout.CENTER)
		
		App.applyThemedUI {
			// Apply theme changes to context menu!
			buildPopupMenu()
		}
	}
	
	private fun setupListHoverCallbacks() {
		listView.setOnItemHover { item ->
			val	tag = item.tag1
			handleMouseEnter(tag, item)
		}
		
		listView.setOnItemMouseExit { item ->
			handleMouseExit(item)
		}
		
		listView.setOnItemDoubleClick { item ->
			val	tag = item.tag1
			selectAction(tag)
			
			ActionRunner.runAction(selectedAction!!, false)
		}
		
		listView.setOnItemRightClick { item, e ->
			showPopup(
				item,
				SwingUtilities.convertPoint(listView, e.point, item)
			)
		}
		
		listView.setOnContextMenu { item ->
			val button = item.findByName<JButton>("more_btn") ?: return@setOnContextMenu
			val point  = SwingUtilities.convertPoint(button, button.width / 2, button.height / 2, item)
			showPopup(item, point)
		}
	}
	
	private fun setEmptyView() {
		// 1. Create the container using GridBagLayout (the "Centering King")
		val emptyView = JPanel(GridBagLayout()).apply {
			// Optional: match your background
			isOpaque = false
		}
		
		// 2. Define standard constraints for a vertical stack
		val gbc = GridBagConstraints().apply {
			// Takes full width (next component goes below)
			gridwidth = GridBagConstraints.REMAINDER
			
			// Stay in the middle
			anchor = GridBagConstraints.CENTER
			
			// Margin/Padding
			insets = Insets(5, 5, 5, 5)
		}
		
		// 3. Add components directly to the same panel
		emptyView.add(JLabel("No action found", SwingConstants.CENTER).apply {
			putClientProperty("FlatLaf.style", "font: 130% bold; foreground: @disabledForeground")
		}, gbc)
		
		emptyView.add(FlatButton().apply {
			icon = Icons.ACTION_DESK.icon(16)
			text = "Create new action"
			
			addActionListener {
				ActionDesk.createNewAction()
				onUI { ActionDesk.clearSearchFilter() }
			}
		}, gbc)
		
		listView.setEmptyView(emptyView)
	}
	
	override fun onActionRepoLoaded() {
		allItems.clear()
		allItems.addAll(ActionRepo.list())
		
		listView.clearList()
		
		allItems.forEach  { action ->
			listView.addItem(createListItem(action), false)
		}
		
		listView.itemListChanged()
	}
	
	override fun onActionUpdated(action: Action) {
		onActionRepoLoaded()
	}
	
	override fun onActionDeleted(action: Action) {
		// TODO - find the view representing the action
		//  and remove it from the UI
		onActionRepoLoaded()
	}
	
	override fun onActionAdded(action: Action) {
		listView.addItem(createListItem(action))
	}
	
	private fun showPopup(com: JComponent, e: Point) {
		popupMenu.show(com, e.x, e.y)
	}
	
	private fun buildPopupMenu() {
		popupMenu = FlatPopupMenu().apply {
			val diagnose = JMenuItem("Run Diagnostic")
			diagnose.icon = FlatSVGIcon("icon/debug.svg", 20, 20)
			
			diagnose.addActionListener {
				ActionRunner.runAction(selectedAction!!, true)
			}
			
			val run = JMenuItem("Run")
			run.icon = Icons.RUN.icon(20)
			run.addActionListener {
				ActionRunner.runAction(selectedAction!!, false)
			}
			
			val edit = JMenuItem("Edit")
			edit.icon = Icons.EDIT.icon(20)
			
			edit.addActionListener {
				val x = ActionEditor(ActionDesk, selectedAction!!)
				x.setUploadCallback(this@ActionListPanel::refresh)
				x.showIt()
			}
			
			val delete = JMenuItem("Delete")
			delete.icon = Icons.DELETE.icon(20)
			
			delete.addActionListener {
				onUI {
					Alert
						.confirm("Are you sure to delete the following action?\nOnce deleted, it can't be undone!\n\n${selectedAction!!.name}")
						.title("Confirm deletion")
						.onAck {
							ActionRepo.delete(selectedAction)
						}
						.show(ActionDesk)
				}
			}
			
			val logFolder  = JMenuItem("Log folder").apply {
				icon = Icons.FOLDER.icon(20)
				
				addActionListener {
					val action = selectedAction!!
					val folderPath = "${Env.APP_FOLDER}/logs/${action.logFolder()}"
					
					if (!File(folderPath).exists()) {
						val msg = "Folder doesn't exist: $folderPath"
						App.logWarn(msg)
						NotificationManager.warn(msg, isSilent = false)
						Alert.confirm(msg).title("Not found").show(ActionDesk)
						return@addActionListener
					}
					
					CommonActions.openFolder(folderPath)
				}
			}
			
			add(run)
			add(edit)
			add(delete)
			add(JSeparator())
			add(diagnose)
			add(logFolder)
		}
		
		listView.registerPopupMenu(popupMenu)
	}

	private fun createListItem(action: Action): JPanel2 {
		val panel = JPanel2(BorderLayout(12, 8))
		
		/*
		 * Right side icons panel
		 * */
		val statusIconPanel = JPanel(HorizontalLayout(5))
		statusIconPanel.isOpaque = false
		statusIconPanel.border = EmptyBorder(4, 0, 0, 0)
		
		val lockStatus = Random.nextBoolean()
		
		val lockIcon =
			if (lockStatus) Icons.LOCKED.icon(18)
			else Icons.UNLOCKED.icon(18)
		
		val lockIconTooltip =
			if (lockStatus) "Password protected"
			else "Not protected"
		
		val lockedIcon = JLabel(lockIcon)
		lockedIcon.toolTipText = lockIconTooltip
		statusIconPanel.add(lockedIcon)
		
		if (action.singleton) {
			val singleton = JLabel(Icons.SINGLE_STOPPED_CONTAINER.icon(16))
			singleton.toolTipText = "Single instance"
			statusIconPanel.add(singleton)
		}
		
		if (action.runAsAdmin) {
			val adminIcon = JLabel(Icons.SHIELD.icon(15))
			adminIcon.toolTipText = "Runs with Admin privileges"
			statusIconPanel.add(adminIcon)
		}
		
		if (action.startWithAD) {
			val autoRestartIcon = JLabel(Icons.SUBSCRIPTION.icon(18))
			autoRestartIcon.toolTipText = "Auto start with Action Desk"
			statusIconPanel.add(autoRestartIcon)
		}
		
		val runIcon = FlatButton().apply {
			icon = Icons.RUN.icon(20)
			isOpaque = false
			isFocusable = false
			toolTipText = "Run"
			putClientProperty("JButton.buttonType", "toolBarButton")
			addActionListener {
				ActionRunner.runAction(action, false)
			}
			border = EmptyBorder(4, 4, 4, 4)
		}
		
		
		val moreIcon = FlatButton().apply {
			name = "more_btn"
			toolTipText = "More options"
			icon = "more".icon(20)
			isOpaque = false
			isFocusable = false
			border = EmptyBorder(4, 4, 4, 4)
			putClientProperty("JButton.buttonType", "toolBarButton")
			
			addMouseListener(object : MouseAdapter() {
				override fun mouseClicked(e: MouseEvent) {
					selectedAction = action
					
					showPopup(
						this@apply,
						e.point
					)
				}
			})
		}
		
		val hoverActionsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 0, 0)).apply {
			name = "hover_actions_panel"
			layout = BoxLayout(this, BoxLayout.X_AXIS)
			isVisible = false
			isOpaque = false
			
			add(Box.createVerticalGlue())
			add(runIcon)
			add(Box.createHorizontalStrut(0))
			add(moreIcon)
			add(Box.createVerticalGlue())
		}
		
		panel.tag1 = action.id.toString()
		panel.tag2 = action.name
		
		/*
		 * icon + name
		 * */
		val actionIcon = JLabel(action.icon.icon(32)).apply {
			preferredSize = Dimension(32, 32)
			isFocusable = false
		}
		
		panel.add(actionIcon, BorderLayout.WEST)
		
		val actionName = JLabel(action.name).apply {
			preferredSize = Dimension(0, preferredSize.height)
		}
		
		val p = JPanel(BorderLayout()).apply {
			isFocusable = false
			isOpaque = false
			add(actionName, BorderLayout.CENTER)
			add(statusIconPanel, BorderLayout.SOUTH)
		}
		
		panel.add(p, BorderLayout.CENTER)
		
		val shortcutLabel = JLabel(action.globalKey).apply {
			name = "shortcut_label"
			horizontalAlignment = JLabel.RIGHT
			preferredSize = Dimension(120, preferredSize.height)
			
			foreground = UIManager.getColor("Label.disabledForeground")
			font = font.deriveFont(font.size2D - 1f)
		}
		
		val x = JPanel(HorizontalLayout(0)).apply {
			isOpaque = false
			
			add(shortcutLabel)
			add(hoverActionsPanel)
		}
		
		panel.add(x, BorderLayout.EAST)
		
		//panel.border = EmptyBorder(8, 12, 8, 12)
		
		panel.putClientProperty("FlatLaf.style", "background: @itemBG")
		// panel.putClientProperty("FlatLaf.style", "border: net.abdulahad.action_desk.view.MehBorder")
		
		panel.isFocusable = true
		
		return panel
	}
	
	private fun selectAction(id: String) {
		for (item in allItems) {
			if (item.id == id.toInt()) {
				selectedAction = item
				break
			}
		}
	}
	
	private fun handleMouseEnter(tag: String, panel: JPanel) {
		selectAction(tag)
		
		val shortcut = panel.findByName<JLabel>("shortcut_label")
		val actions = panel.findByName<JPanel>("hover_actions_panel")
		
		shortcut?.isVisible = false
		actions?.isVisible = true
	}
	
	private fun handleMouseExit(item: JPanel) {
		item.findByName<JPanel>("hover_actions_panel")?.let { it.isVisible = false }
		item.findByName<JLabel>("shortcut_label")?.let { it.isVisible = true }
	}
	
	override fun onSearch(term: String) {
		listView.filterView { item ->
			return@filterView item.tag2.lowercase().contains(term)
		}
	}
	
	override fun refresh() {
		onActionRepoLoaded()
	}
	
	override fun repaintList() {
		listView.clearFilteredList()
	}
	
	fun isPopupMenuShown() = listView.isPopupMenuShown()
	
}