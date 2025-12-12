package net.abdulahad.action_desk.view

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatPopupMenu
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.data.AppConfig
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.helper.Icons
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.helper.ViewHelper
import net.abdulahad.action_desk.lib.view.IconTextField
import net.abdulahad.action_desk.lib.view.ScrollableTextField
import net.abdulahad.action_desk.view.dashboard.ActionListPanel
import net.abdulahad.action_desk.view.dashboard.ActionProcessPanel
import net.abdulahad.action_desk.view.action_editor.ActionEditor
import net.abdulahad.action_desk.view.settings.Settings
import org.jdesktop.swingx.HorizontalLayout
import org.jdesktop.swingx.VerticalLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Insets
import java.awt.event.*
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import kotlin.system.exitProcess

object ActionDesk : JDialog() {
	
	private fun readResolve(): Any = ActionDesk
	
	private var windowListenerAttached: Boolean = false
	
	private val cardLayout = CardLayout()
	
	private var activePanelName: String? = null
	
	private lateinit var actionListPanel: ActionListPanel
	private lateinit var actionProcessPanel: ActionProcessPanel
	private lateinit var headerPanel: JPanel
	private lateinit var contentPanel: JPanel
	
	private lateinit var searchField: ScrollableTextField
	private lateinit var morePopupMenu: JPopupMenu
	
	private lateinit var instanceBtn: FlatButton
	
	private val instanceIcon = FlatSVGIcon("icon/groups.svg", 20, 20)
	private val actionIcon = FlatSVGIcon("icon/spark.svg", 20, 20)
	
	private var searchFocus = AppConfig.getSearchFocus()
	
	init {
		setupFrame()
		setTitleBar()
		setHeader()
		setupMoreMenu()
		
		setupContentPanels()
		pack()
		
		App.listenThemeChange {
			App.applyIconifyIcon()
			applyTopPanelBottomBorder()
			setupMoreMenu()
			App.applyCloseIcon()
		}
	}
	
	private fun setupContentPanels() {
		contentPanel = JPanel(cardLayout)
		add(contentPanel)
		
		actionListPanel = ActionListPanel()
		actionProcessPanel = ActionProcessPanel()
		
		/*
		 * Add to the content panel
		 * */
		contentPanel.add(actionListPanel, "action_panel")
		contentPanel.add(actionProcessPanel, "action_process_panel")
		
		/*
		 * Show default
		 * */
		activePanelName = "action_panel"
		cardLayout.show(contentPanel, "action_panel")
	}
	
	private fun showPanel(panelName: String) {
		activePanelName = panelName
		cardLayout.show(contentPanel, activePanelName)
		
		val isActionPanel = activePanelName == "action_panel"
		
		val iconX 	= if (isActionPanel) instanceIcon else actionIcon
		val tooltip = if (isActionPanel) "Running Actions" else "Action List"
		
		instanceBtn.apply {
			icon = iconX
			toolTipText = tooltip
		}
	}
	
	private fun isActionPanelActive(): Boolean {
		return activePanelName == "action_panel"
	}
	
	private fun setTitleBar() {
		val menuBar = JMenuBar()
		
		val label = JLabel()
		label.text = App.getName()
		label.border = BorderFactory.createEmptyBorder(0, 5, 0, 48)
		
		menuBar.add(label)
		
		jMenuBar = menuBar
	}
	
	private fun setupSearchFieldListener() {
		searchField.addKeyListener(object : KeyAdapter() {
			override fun keyReleased(e: KeyEvent) {
				
				if (e.keyCode == KeyEvent.VK_ESCAPE) {
					hideFrame()
					return
				}
				
				if (isActionPanelActive()) {
					// Shift focus to action list
					if (e.keyCode == KeyEvent.VK_DOWN) {
						actionListPanel.focusList()
						return
					}
					
					val text = searchField.text.trim().lowercase()
					actionListPanel.filterList(text)
				}
			}
		})
		
		searchField.addFocusListener(object : FocusAdapter() {
			override fun focusGained(e: FocusEvent) {
				if (isActionPanelActive()) {
					actionListPanel.clearSelection()
				}
			}
		})
	}
	
	private fun setupMoreMenu() {
		morePopupMenu = FlatPopupMenu().apply {
			val logFolder  = JMenuItem("Logs Folder").apply {
				icon = Icons.FOLDER.icon(20)
				addActionListener {
					CommonActions.openFolder("${Env.APP_FOLDER}/logs")
				}
			}
			
			val marketPlace  = JMenuItem("Action Library")
			marketPlace.toolTipText = "Download & install common\nactions from online library"
			marketPlace.icon = Icons.INSTALL.icon()
			
			val settings  = JMenuItem("Settings").apply {
				icon = Icons.SETTINGS.icon()
				
				addActionListener {
					ViewHelper.hideAndSeekWindow(Settings(), this@ActionDesk)
				}
			}
			
			val about  = JMenuItem("About")
			about.icon = Icons.INFO_OUTLINE.icon()
			about.addActionListener {
				About(ActionDesk)
			}

			val restart  = JMenuItem("Restart").apply {
				addActionListener {
					CommonActions.restartActionDesk()
				}
			}

			val exit  = JMenuItem("Exit").apply {
				icon = Icons.CLOSE.icon()
				addActionListener {
					exitProcess(0)
				}
			}
			
			add(marketPlace)
			add(logFolder)
			add(JSeparator())
			add(settings)
			add(about)
			add(JSeparator())
			add(restart)
			add(exit)
		}
	}
	
	private fun setupFrame() {
		layout = VerticalLayout()
		minimumSize = Dimension(320, 300)
		isResizable = false
		isAlwaysOnTop = AppConfig.getAlwaysOnTop()
		
		/*
		 * Set up default close up operation
		 * */
		defaultCloseOperation = DO_NOTHING_ON_CLOSE
		
		addWindowListener(object: WindowAdapter() {
			override fun windowClosing(e: WindowEvent?) {
				isVisible = false
			}
		})
	}
	
	private fun watchWindowMovement() {
		addComponentListener(object: ComponentAdapter() {
			override fun componentMoved(e: ComponentEvent?) {
				Env.CONFIG.set("win_pos_x", locationOnScreen.x)
				Env.CONFIG.set("win_pos_y", locationOnScreen.y)
				Env.CONFIG.save()
			}
		})
	}
	
	private fun setHeader() {
		val searchIcon = Icons.SEARCH.icon(21)
		val search = IconTextField(searchIcon, 11)
		search.border = EmptyBorder(2, 0, 0, 0)
		
		searchField = search.textField.apply {
			isOpaque = false
			background = Color(0, 0, 0, 0)
			border = EmptyBorder(0, 0, 0, 0)
			placeholderText = "Search Actions"
			preferredSize = Dimension(170, 30)
		}
		
		setupSearchFieldListener()

		val addBtn = FlatButton().apply {
			toolTipText = "New Action"
			icon = Icons.ADD.icon(20)
			
			addActionListener {
				val x = ActionEditor(this@ActionDesk)
				x.setUploadCallback(actionListPanel::reload)
				x.showIt()
			}
		}
		
		instanceBtn = FlatButton().apply {
			toolTipText = "Running Actions"
			icon = instanceIcon
			
			addActionListener {
				val togglePanel = if (isActionPanelActive()) "action_process_panel" else "action_panel"
				showPanel(togglePanel)
				
				if (togglePanel == "action_process_panel") {
					actionProcessPanel.refresh()
				}
			}
		}
		
		val moreBtn = FlatButton().apply {
			toolTipText = "More Options"
			icon = Icons.MORE_HORIZONTAL.icon(20)
			
			addActionListener {
				morePopupMenu.show(this, 0, this.height)
			}
		}
		
		// Make but tons flat, no borders/background
		listOf(addBtn, instanceBtn, moreBtn).forEach { btn ->
			btn.isOpaque = false
			btn.isFocusable = false

			// optional: makes them tighter
			btn.putClientProperty("JButton.buttonType", "toolBarButton")
		}
		
		headerPanel = JPanel(HorizontalLayout())
		applyTopPanelBottomBorder()
		
		headerPanel.add(search)
		headerPanel.add(instanceBtn)
		headerPanel.add(addBtn)
		headerPanel.add(moreBtn)
		
		add(headerPanel)
	}

	private fun applyTopPanelBottomBorder() {
		val line = MatteBorder(Insets(0, 0, 1, 0), UIManager.getColor("Component.borderColor"))
		val padding = EmptyBorder(0, 8, 4, 8)
		
		headerPanel.border = CompoundBorder(line, padding)
	}
	
	private fun frameVisibility(show: Boolean) {
		if (isShowing && show) {
			toFront()
			return
		}
		
		if (show) {
			val x = Env.CONFIG.path("win_pos_x").int(-1)
			val y = Env.CONFIG.path("win_pos_y").int(-1)
			
			if (x == -1 && y == -1) {
				setLocationRelativeTo(null)
			} else {
				setLocation(x, y)
			}
			
			isVisible = true
			toFront()
			
			if (searchFocus) {
				searchField.requestFocus()
			} else {
				actionListPanel.focusList()
			}
			
			if (!windowListenerAttached) {
				App.applyCloseIcon()
				windowListenerAttached = true
				watchWindowMovement()
			}
		} else {
			isVisible = false
		}
	}
	
	fun showFrame() {
		frameVisibility(true)
	}
	
	fun hideFrame() {
		searchField.text = ""
		frameVisibility(false)
	}
	
	fun focusSearchField() {
		searchField.requestFocusInWindow()
	}
	
	fun applyFocusSearch(enable: Boolean) {
		searchFocus = enable
	}
	
	fun applyAlwaysOnTop(enable: Boolean) {
		isAlwaysOnTop = enable
	}
	
}
