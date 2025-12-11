package net.abdulahad.action_desk.view.dashboard

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGUtils
import com.formdev.flatlaf.extras.components.FlatPopupMenu
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.LogViewerPanel
import net.abdulahad.action_desk.dao.ActionDao
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.engine.ActionRunner
import net.abdulahad.action_desk.helper.Icons
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.lib.util.Alert
import net.abdulahad.action_desk.lib.util.Poth
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.model.Action.Companion.logFolder
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.view.ActionDesk
import net.abdulahad.action_desk.view.action_editor.ActionEditor
import net.abdulahad.action_desk.view.list_model.ActionEntry
import org.jdesktop.swingx.VerticalLayout
import java.awt.Dimension
import java.awt.event.*
import java.io.File
import javax.swing.*

class ActionListPanel: JPanel() {
	
	private lateinit var scrollPane: JScrollPane
	
	private lateinit var allItems: List<Action>
	private lateinit var jList: JList<Action>
	private lateinit var listModel: DefaultListModel<Action>
	
	private lateinit var popupMenu: JPopupMenu
	
	init {
		layout = VerticalLayout()
		
		loadActions()
		setActionListPanel()
		reloadPopMenu()
		
		setupScrollPane()
		
		// Apply theme change to context menu!
		App.listenThemeChange {
			reloadPopMenu()
		}
	}
	
	private fun setupScrollPane() {
		scrollPane = JScrollPane(jList)
		scrollPane.preferredSize = Dimension(300, 300)
		scrollPane.verticalScrollBar.unitIncrement = 8
		
		add(scrollPane)
	}
	
	private fun setActionListPanel() {
		listModel = DefaultListModel<Action>().apply {
			allItems.forEach { addElement(it) }
		}
		
		jList = JList(listModel)
		jList.selectionMode = ListSelectionModel.SINGLE_SELECTION
		jList.cellRenderer = ActionEntry()
		
		jList.addKeyListener(object : KeyAdapter() {
			
			override fun keyReleased(e: KeyEvent) {
				e.consume()
				
				if (e.keyCode == KeyEvent.VK_UP || e.keyCode == KeyEvent.VK_DOWN) {
					return
				}
				
				if (e.keyCode == KeyEvent.VK_ESCAPE) {
					jList.clearSelection()
					handleEscape()
					return
				}
				
				if (e.keyCode == KeyEvent.VK_SHIFT) {
					jList.clearSelection()
					ActionDesk.focusSearchField()
					return
				}
				
				if (e.keyCode == KeyEvent.VK_ENTER) {
					handleAction()
					return
				}
				
				println("Searching the action for hotkey: ${e.keyCode}")
			}
			
			override fun keyPressed(e: KeyEvent) {
				handleActionListNavigation(e)
			}
		})
		
		jList.addMouseListener(object : MouseAdapter() {
			override fun mousePressed(e: MouseEvent) {
				maybeShowPopup(e)
			}
			
			override fun mouseReleased(e: MouseEvent) {
				maybeShowPopup(e)
			}
			
			override fun mouseClicked(e: MouseEvent) {
				if (e.clickCount == 2 && e.button == MouseEvent.BUTTON1) {
					val index = jList.locationToIndex(e.point)
					if (index != -1) {
						val item = jList.model.getElementAt(index)
						if (item is Action) {
							handleAction()
						}
					}
				}
			}
			
			private fun maybeShowPopup(e: MouseEvent) {
				if (e.isPopupTrigger) {
					val index = jList.locationToIndex(e.point)
					if (index != -1) {
						jList.selectedIndex = index // highlight the clicked item
						popupMenu.show(jList, e.x, e.y)
					}
				}
			}
		})
	}
	
	private fun handleActionListNavigation(e: KeyEvent) {
		if (e.keyCode == KeyEvent.VK_UP || e.keyCode == KeyEvent.VK_DOWN) {
			
			val polarity = if (e.keyCode == KeyEvent.VK_DOWN) 1 else -1
			var index = jList.selectedIndex + polarity
			
			if (index < 0) {
				index = jList.model.size - 1
			} else if (index >= jList.model.size) {
				index = 0
			}
			
			SwingUtilities.invokeLater {
				jList.selectedIndex = index
			}
		}
	}
	
	private fun handleEscape() {
		jList.clearSelection()
		
		listModel.clear()
		allItems.forEach { listModel.addElement(it) }
		
		ActionDesk.hideFrame()
	}
	
	private fun handleAction(diagnose: Boolean = false) {
		val action = jList.selectedValue
		ActionRunner.runAction(action, diagnose)
	}
	
	private fun reloadPopMenu() {
		popupMenu = FlatPopupMenu().apply {
			val run  = JMenuItem("Run")
			run.icon = Icons.RUN.icon(20)
			
			run.addActionListener {
				handleAction()
			}
			
			val log  = JMenuItem("See logs")
			log.icon = Icons.TOGGLE_VISIBILITY.icon(20)
			
			log.addActionListener {
				JFrame("Log Viewer").apply {
					
					val action = jList.selectedValue
					iconImage = FlatSVGUtils.svg2image(Poth.getURL("intel_icon/${action.icon}.svg"), 64, 64)
					title = "Logs - ${action.name}"
					
					//defaultCloseOperation = JFrame.EXIT_ON_CLOSE
					contentPane = LogViewerPanel()
					preferredSize = Dimension(800, 480)
					pack()
					setLocationRelativeTo(null)
					this@ActionListPanel.isVisible = false
					isVisible = true
					
					addWindowListener(object : WindowAdapter() {
						override fun windowClosing(e: WindowEvent) {
							SwingUtilities.invokeLater {
								this@ActionListPanel.isVisible = true
							}
						}
					})
				}
			}
			
			val diagnose = JMenuItem("Run Diagnostic")
			diagnose.icon = FlatSVGIcon("icon/debug.svg", 20, 20)
			
			diagnose.addActionListener {
				handleAction(true)
			}
			
			val edit = JMenuItem("Edit")
			edit.icon = Icons.EDIT.icon(20)
			
			edit.addActionListener {
				val x = ActionEditor(ActionDesk, jList.selectedValue)
				x.showIt()
			}
			
			val delete = JMenuItem("Delete")
			delete.icon = Icons.DELETE.icon(20)
			
			delete.addActionListener {
				val id = jList.selectedValue.id
				SwingUtilities.invokeLater {
					Alert
						.confirm("Are you sure to delete this action?\nIt can't be undone!")
						.title("Confirm deletion")
						.onAck {
							ActionDao.delete(id)
							reload()
						}
						.show(ActionDesk)
				}
			}
			
			val logFolder  = JMenuItem("Log folder").apply {
				icon = Icons.FOLDER.icon(20)
				
				addActionListener {
					val action = jList.selectedValue
					val folderPath = "${Env.APP_FOLDER}/logs/${action.logFolder()}"
					
					if (!File(folderPath).exists()) {
						val msg = "Folder doesn't exist: $folderPath"
						App.logWarn(msg)
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
			add(JSeparator())
			add(log)
			add(logFolder)
		}
	}
	
	private fun loadActions() {
		allItems = ActionDao.list()
	}
	
	fun filterList(text: String) {
		listModel.clear()
		
		if (text.isEmpty()) {
			// no filter, show all
			allItems.forEach { listModel.addElement(it) }
		} else {
			// filter by name or mnemonic
			allItems
				.filter {
					it.name.lowercase().contains(text)
				}
				.forEach {
					listModel.addElement(it)
				}
		}
	}
	
	fun focusList() {
		if (jList.model.size == 0) return
		jList.requestFocus()
		jList.selectedIndex = 0
	}
	
	fun clearSelection() {
		jList.clearSelection()
	}
	
	fun reload() {
		loadActions()
		listModel.clear()
		allItems.forEach { listModel.addElement(it) }
	}
	
}