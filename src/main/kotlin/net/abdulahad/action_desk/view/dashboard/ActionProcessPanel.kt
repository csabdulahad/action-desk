package net.abdulahad.action_desk.view.dashboard

import com.formdev.flatlaf.ui.FlatEmptyBorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.dao.ActionDao
import net.abdulahad.action_desk.engine.ActionManager
import net.abdulahad.action_desk.helper.Icons
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.helper.ProcessHelper
import net.abdulahad.action_desk.lib.util.Alert
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.onUI
import net.abdulahad.action_desk.view.ActionDesk
import org.jdesktop.swingx.VerticalLayout
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ActionProcessPanel : JPanel() {
	
	private var fetching = false
	
	private val fetchingMsg = "Fetching process details..."
	private val noActionMsg = "No action running"
	
	private val loadingPanel = JPanel(GridBagLayout())
	private val loadingLabel = JLabel(fetchingMsg)
	
	private val listPanel = JPanel(VerticalLayout())
	private val scrollPane = JScrollPane(listPanel)
	
	init {
		val constraints = GridBagConstraints().apply {
			gridx = 0
			gridy = 0
			anchor = GridBagConstraints.CENTER
		}
		
		layout = BorderLayout()
		loadingPanel.add(loadingLabel, constraints)
		isOpaque = true
		
		scrollPane.isOpaque = true
		
		App.listenThemeChange {
			loadingLabel.foreground = UIManager.getColor("Label.foreground")
			loadingPanel.background = UIManager.getColor("Panel.background")
		}
	}
	
	private fun swapPanel(panel: JPanel) {
		removeAll()
		
		if (panel == listPanel) {
			add(scrollPane)
		} else {
			add(loadingPanel)
		}
		
		revalidate()
		repaint()
	}
	
	fun installHoverEffect(panel: JPanel, btn: JButton) {
		val enterAction = {
			panel.background = UIManager.getColor("List.selectionBackground")
			panel.repaint()
		}
		val exitAction = {
			panel.background = UIManager.getColor("Panel.background")
			panel.repaint()
		}
		
		val listener = object : MouseAdapter() {
			override fun mouseEntered(e: MouseEvent) {
				btn.isVisible = true
				enterAction()
			}
			
			override fun mouseExited(e: MouseEvent) {
				val mousePos = panel.mousePosition
				
				if (mousePos == null || !panel.contains(mousePos)) {
					btn.isVisible = false
					exitAction()
				}
			}
		}
		
		// add to panel + all children
		panel.addMouseListener(listener)
		for (comp in panel.components) {
			comp.addMouseListener(listener)
		}
	}
	
	private fun createListItem(action: Action, parentProcess: Long, numOfChild: Int): JPanel {
		val panel = JPanel().apply {
			isOpaque = true
			setLayout(BorderLayout(8, 8))
			setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8))
		}
		
		/*
		 * Action icon
		 * */
		val icon = JLabel(action.icon.icon(22))
		panel.add(icon, BorderLayout.WEST)
		
		
		/*
		 * Name + num of process
		 * */
		val name = JLabel(action.name)
		val processWord = if (numOfChild > 1) "processes" else "process"
		
		val numOfProcess = JLabel("$numOfChild $processWord")
		numOfProcess.font = numOfProcess.getFont().deriveFont(11.5f)
		
		JPanel().apply {
			layout = VerticalLayout()
			isOpaque = false
			border = FlatEmptyBorder(0, 0, 0, 8)
			
			add(name)
			add(numOfProcess)
			
			panel.add(this, BorderLayout.CENTER)
		}
		
		val btn = JButton(Icons.CLOSE.icon(16)).apply {
			isVisible = false
			isOpaque = false
			isFocusable = false
			
			// optional: makes them tighter
			putClientProperty("JButton.buttonType", "toolBarButton")
			
			addActionListener {
				Alert
					.confirm("${action.name} will be terminated. Are you sure you want to close this action?")
					.title("Warning")
					.onAck {
						ProcessHelper.killProcess(parentProcess) { result ->
							if (result) {
								listPanel.remove(panel)
								
								if (listPanel.componentCount == 0) {
									loadingLabel.text = noActionMsg
									swapPanel(loadingPanel)
								} else {
									listPanel.revalidate()
									listPanel.repaint()
								}
							}
						}
					}
					.show(ActionDesk)
			}
		}
		
		panel.add(btn, BorderLayout.EAST)
		
		installHoverEffect(panel, btn)
		
		return panel
	}
	
	fun refresh() {
		CoroutineScope(Dispatchers.Default).launch {
			if (fetching) return@launch
			
			onUI {
				fetching = true
				loadingLabel.text = fetchingMsg
				swapPanel(loadingPanel)
			}
			
			val list = ActionManager.readPIDFromLockFiles()
			
			if (list.isEmpty()) {
				onUI {
					loadingLabel.text = noActionMsg
					fetching = false
				}
				
				return@launch
			}
			
			val itemUIList = mutableListOf<JPanel>()
			
			list.forEach {
				val id = it.substringAfter(';').toInt()
				val pid = it.substringBefore(';').toLong()
				
				val action = ActionDao.fetchById(id) ?: return@forEach
				
				val childPIDs = ActionManager.collectChildPIDs(pid)
				
				val processCount = if (childPIDs.isEmpty()) 1 else childPIDs.size + 1
				
				val itemUI = createListItem(action, pid, processCount)
				itemUIList.add(itemUI)
			}
			
			if (itemUIList.isEmpty()) {
				onUI {
					loadingLabel.text = noActionMsg
					fetching = false
				}
				
				return@launch
			}
			
			onUI {
				fetching = false
				
				listPanel.removeAll()
				itemUIList.forEach { listPanel.add(it) }
				listPanel.revalidate()
				
				swapPanel(listPanel)
			}
		}
	}
	
}