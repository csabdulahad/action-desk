package net.abdulahad.action_desk.view

import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatPopupMenu
import com.formdev.flatlaf.util.UIScale
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.config.ConfigKeys
import net.abdulahad.action_desk.config.ConfigService
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.helper.CommonActions
import net.abdulahad.action_desk.helper.Icons
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.helper.ViewHelper
import net.abdulahad.action_desk.engine.notification.NotificationListener
import net.abdulahad.action_desk.engine.notification.NotificationManager
import net.abdulahad.action_desk.lib.view.ButtonBadge
import net.abdulahad.action_desk.lib.view.IconTextField
import net.abdulahad.action_desk.lib.view.ScrollableTextField
import net.abdulahad.action_desk.model.Notification
import net.abdulahad.action_desk.onUIDelayed
import net.abdulahad.action_desk.view.action_editor.ActionEditor
import net.abdulahad.action_desk.view.dashboard.ActionListPanel
import net.abdulahad.action_desk.view.dashboard.ActionProcessPanel
import net.abdulahad.action_desk.view.settings.Settings
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.MatteBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.system.exitProcess


object ActionDesk : JDialog(), NotificationListener {
	
	private fun readResolve(): Any = ActionDesk
	
	private var showWindowInCenter = false
	
	private lateinit var wrapperPanel: JPanel
	
	private var windowListenerAttached: Boolean = false
	
	private val cardLayout = CardLayout()
	
	private val statusTextField = JLabel("v1.0")
	
	private var activePanelName: String? = null
	
	private lateinit var actionListPanel: ActionListPanel
	private lateinit var actionProcessPanel: ActionProcessPanel
	private lateinit var contentPanel: JPanel
	
	private lateinit var searchField: ScrollableTextField
	private lateinit var morePopupMenu: JPopupMenu
	private lateinit var notificationPopup: NotificationView
	
	private lateinit var instanceBtn: FlatButton
	private lateinit var notificationIcon: ButtonBadge
	
	private val instanceIcon = Icons.GROUPS.icon(20)
	private val actionIcon = Icons.LIGHTNING.icon(20)
	
	private var resizeDebounceTimer: Timer? = null
	
	init {
		setupFrame()
		setWindowResizeListener()
		setTitleBar()
		setHeader()
		
		setupContentPanels()
		notificationPopup = NotificationView()
		setFooter()
		pack()
		
		App.applyThemedUI {
			val titlePaneColor =  UIManager.getColor("AD.secondaryColor")
			rootPane.background = titlePaneColor
			wrapperPanel.background = titlePaneColor
			
			setupMoreMenu()
		}
		
		setupSearchFocusListener()
		
		onUIDelayed(1f) {
			App.setMessage("Using folder: " + Env.APP_FOLDER, false)
		}
		
		NotificationManager.listen(this)
	}
	
	private fun setupSearchFocusListener() {
		// 1. Get the Root Pane's InputMap
		val inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
		
		// 2. Map the KeyStroke (forward slash) to an Action Key (a simple string)
		inputMap.put(KeyStroke.getKeyStroke('/'), "searchFocusListener")
		
		// 3. Map the Action Key to an actual AbstractAction
		getRootPane().actionMap.put("searchFocusListener", object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				searchField.requestFocus()
			}
		})
	}
	
	private fun setupContentPanels() {
		contentPanel = JPanel(cardLayout)
		add(contentPanel, BorderLayout.CENTER)
		
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
			override fun keyPressed(e: KeyEvent) {
				if (
					e.keyCode != KeyEvent.VK_ESCAPE ||
					actionListPanel.isPopupMenuShown()) return
				
				hideFrame()
				searchField.text = ""
				
				actionListPanel.repaintList()
			}
		})
		
		searchField.document.addDocumentListener(object : DocumentListener {
			private fun updateSearch() {
				val text = searchField.text.trim().lowercase()
				
				if (isActionPanelActive()) {
					actionListPanel.onSearch(text)
				}
			}
			
			override fun insertUpdate(e: DocumentEvent) = updateSearch()
			override fun removeUpdate(e: DocumentEvent) = updateSearch()
			override fun changedUpdate(e: DocumentEvent) = updateSearch()
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
			
			val marketPlace  = JMenuItem("Action Store")
			marketPlace.toolTipText = "Download & install common\nactions from online library"
			marketPlace.icon = Icons.INSTALL.icon()
			
			val settings  = JMenuItem("Settings").apply {
				addActionListener {
					ViewHelper.hideAndSeekWindow(Settings(), this@ActionDesk)
				}
			}
			
			val about  = JMenuItem("About")
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
			
			add(settings)
			add(logFolder)
			add(marketPlace)
			add(about)
			add(JSeparator())
			add(restart)
			add(exit)
		}
	}
	
	private fun setupFrame() {
		layout = BorderLayout()
		// isResizable = false
		isAlwaysOnTop = AppConfig.getAlwaysOnTop()
		
		/*
		 * Set frame size
		 * */
		val point = AppConfig.getWindowSize()
		preferredSize = Dimension(UIScale.scale(point.x), UIScale.scale(point.y))
		
		/*
		 * Show window position setting
		 * */
		showWindowInCenter = AppConfig.getShowWindowInCenter()
		
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
	
	private fun setWindowResizeListener() {
		addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent?) {
				// Cancel previous pending save
				resizeDebounceTimer?.stop()
				
				// Debounce: execute after user stops resizing
				resizeDebounceTimer = Timer(500) {
					// Get the new dimensions
					val width = getWidth()
					val height = getHeight()
					
					AppConfig.setWindowSize(Point(width, height), false)
					App.setMessage("Windows resized to $width x $height")
				}.apply {
					isRepeats = false
					start()
				}
			}
		})
	}
	
	private fun watchWindowMovement() {
		addComponentListener(object: ComponentAdapter() {
			override fun componentMoved(e: ComponentEvent?) {
				ConfigService.commit(ConfigKeys.WIN_POS_X, location.x)
				ConfigService.commit(ConfigKeys.WIN_POS_Y, location.y)
			}
		})
	}
	
	private fun setHeader() {
		val searchIcon = Icons.SEARCH.icon(21)
		val search = IconTextField(searchIcon, 11)
		search.preferredSize = Dimension(24, 24)
		
		searchField = search.textField.apply {
			font = font.deriveFont(font.size2D + 1f)
			isOpaque = false
			background = Color(0, 0, 0, 0)
			border = EmptyBorder(0, 4, 0, 4)
			placeholderText = "Search actions..."
		}
		
		setupSearchFieldListener()
		
		instanceBtn = FlatButton().apply {
			toolTipText = "Running Actions"
			icon = instanceIcon
			
			/*
			 * Handle Tab event on instance buton to focus back to search field
			 * */
			
			// Remove TAB from the Forward Traversal list
			setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet<AWTKeyStroke>())

			// Add TAB to the Backward Traversal list
			val backwardKeys = getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS)
			val newBackwardKeys = HashSet<AWTKeyStroke>(backwardKeys)
			newBackwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))
			setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, newBackwardKeys)
			
			addActionListener {
				val togglePanel = if (isActionPanelActive()) "action_process_panel" else "action_panel"
				showPanel(togglePanel)
				
				if (togglePanel == "action_process_panel") {
					actionProcessPanel.refresh()
				}
			}
			
			addKeyListener(object : KeyAdapter() {
				override fun keyReleased(e: KeyEvent) {
					if (e.keyCode == KeyEvent.VK_ESCAPE) {
						hideFrame()
					}
				}
			})
		}
		
		// Make but tons flat, no borders/background
		listOf(instanceBtn).forEach { btn ->
			btn.isOpaque = false

			// optional: makes them tighter
			btn.putClientProperty("JButton.buttonType", "toolBarButton")
		}
		
		val panel = JPanel(BorderLayout()).apply {
			border = EmptyBorder(6, 6, 6, 6)
			
			add(search, BorderLayout.WEST)
			add(searchField, BorderLayout.CENTER)
			add(instanceBtn, BorderLayout.EAST)
		}
		
		wrapperPanel = JPanel(BorderLayout()).apply {
			isOpaque = true
			
			App.applyThemedUI {
				border = BorderFactory.createCompoundBorder(
					EmptyBorder(3, 12, 12, 12),
					LineBorder(UIManager.getColor("Component.borderColor"), 1),
				)
				
				background = UIManager.getColor("AD.secondaryColor")
			}
			
			add(panel, BorderLayout.CENTER)
		}
		
		val outerPanel = JPanel(BorderLayout()).apply {
			App.applyThemedUI {
				border = MatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor"))
			}
			
			add(wrapperPanel)
		}
		
		add(outerPanel, BorderLayout.NORTH)
	}
	
	private fun setFooter() {
		val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0)).apply {
			isOpaque = false
			
			val newActionIcon = FlatButton().apply {
				toolTipText = "Create new action"
				icon = Icons.ADD.icon(20)
				isOpaque = false
				isFocusable = false
				putClientProperty("JButton.buttonType", "toolBarButton")
				addActionListener {}
				border = EmptyBorder(4, 4, 4, 4)
				
				addActionListener {
					createNewAction()
				}
			}
			
			val settingsIcon = FlatButton().apply {
				toolTipText = "More options"
				icon = Icons.SETTINGS.icon(20)
				isOpaque = false
				//isFocusable = false
				border = EmptyBorder(4, 4, 4, 4)
				putClientProperty("JButton.buttonType", "toolBarButton")
				
				addMouseListener(object : MouseAdapter() {
					override fun mouseClicked(e: MouseEvent) {
						morePopupMenu.show(e.component, e.x, e.y)
					}
				})
			}
			
			notificationIcon = ButtonBadge().apply {
				toolTipText = "Notifications"
				icon = Icons.NOTIFICATIONS.icon(20)
				isOpaque = false
				border = EmptyBorder(4, 4, 4, 4)
				putClientProperty("JButton.buttonType", "toolBarButton")
				badgeIcon = "circle".icon(8)
				badgeTopPadding = 1
				badgeRightPadding = 1
			}
			
			notificationIcon.addMouseListener(object : MouseAdapter() {
				override fun mouseClicked(e: MouseEvent) {
					notificationIcon.isBadgeVisible = false
					notificationPopup.show(notificationIcon)
				}
			})
			
			this.add(newActionIcon)
			this.add(settingsIcon)
			this.add(notificationIcon)
		}
		
		val panel = JPanel(BorderLayout()).apply {
			
			App.applyThemedUI {
				background = UIManager.getColor("AD.secondaryColor")
				
				border = BorderFactory.createCompoundBorder(
					MatteBorder(Insets(1, 0, 0, 0), UIManager.getColor("Component.borderColor")),
					EmptyBorder(4, 12, 4, 12)
				)
				
				statusTextField.foreground = UIManager.getColor("Label.disabledForeground")
			}
			
			this.add(statusTextField.apply {
				font = font.deriveFont(font.size2D - 1f)
				foreground = UIManager.getColor("Label.disabledForeground")
				preferredSize = Dimension(1, preferredSize.height)
			}, BorderLayout.CENTER)
			
			this.add(rightPanel, BorderLayout.EAST)
		}
		
		add(panel, BorderLayout.SOUTH)
	}
	
	private fun frameVisibility(show: Boolean) {
		if (isShowing && show) {
			toFront()
			return
		}
		
		if (show) {
			if (!showWindowInCenter) {
				val x = ConfigService.getInt(ConfigKeys.WIN_POS_X, -1)
				val y = ConfigService.getInt(ConfigKeys.WIN_POS_Y, -1)
				
				if (x == -1 && y == -1) {
					setLocationRelativeTo(null)
				} else {
					setLocation(x, y)
				}
			} else {
				setLocationRelativeTo(null)
			}
			
			isVisible = true
			toFront()
			
			if (!windowListenerAttached) {
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
	
	fun clearSearchFilter() {
		searchField.text = ""
	}
	
	fun applyAlwaysOnTop(enable: Boolean) {
		isAlwaysOnTop = enable
	}
	
	fun applyWindowSize(value: Point) {
		// 1. Set the preferred size of the content pane to match
		// This prevents the Layout Manager from "snapping back"
		contentPane.preferredSize = Dimension(value.x, value.y)
		
		// 2. Use ONLY setSize (unless you want to move the window too)
		setSize(value.x, value.y)
		
		// 3. Force the layout to respect the new boundaries
		rootPane.revalidate()
		rootPane.repaint()
	}
	
	fun applyShowWindowInCenter(value: Boolean) {
		showWindowInCenter = value
	}
	
	
	/* =========================================
	 * Common actions from this ActionDesk class
	 * =========================================
	 */
	fun createNewAction() {
		val x = ActionEditor(this@ActionDesk)
		x.setUploadCallback(actionListPanel::refresh)
		x.showIt()
	}
	
	fun setMessage(text: String, animate: Boolean = true) {
		statusTextField.text = text
		statusTextField.toolTipText = text
		
		if (!animate) return
		
		// 1. Get the current theme's text color dynamically
		val targetColor = UIManager.getColor("Label.disabledForeground") ?: Color.BLACK
		
		val duration = 400 // Total milliseconds
		val frames = 16    // Number of steps
		val interval = duration / frames
		var currentFrame = 0
		
		val timer = Timer(interval, null)
		timer.addActionListener {
			currentFrame++
			
			// Calculate the percentage of the fade (0.0 to 1.0)
			val alphaPercent = currentFrame.toFloat() / frames
			
			// 2. Create the new color with the calculated Alpha
			val animatedColor = Color(
				targetColor.red,
				targetColor.green,
				targetColor.blue,
				(alphaPercent * 255).toInt().coerceIn(0, 255)
			)
			
			statusTextField.foreground = animatedColor
			
			if (currentFrame >= frames) {
				(it.source as Timer).stop()
			}
		}
		
		// Start fully transparent
		statusTextField.foreground = Color(targetColor.red, targetColor.green, targetColor.blue, 0)
		timer.start()
	}
	
	override fun onNewNotification(notification: Notification) {
		if (notification.isSilent) return
		if (::notificationIcon.isInitialized) {
			notificationIcon.isBadgeVisible = true
		}
	}
	
	override fun onNotificationClear() {
		if (::notificationIcon.isInitialized) {
			notificationIcon.isBadgeVisible = false
		}
	}
	
	override fun onNotificationsAcknowledged() {
		if (::notificationIcon.isInitialized) {
			notificationIcon.isBadgeVisible = false
		}
	}

}
