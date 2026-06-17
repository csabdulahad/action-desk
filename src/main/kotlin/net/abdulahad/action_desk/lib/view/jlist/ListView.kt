package net.abdulahad.action_desk.lib.view.jlist

import com.formdev.flatlaf.ui.FlatScrollBarUI
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.lib.view.JPanel2
import net.abdulahad.action_desk.onSwing
import org.jdesktop.swingx.VerticalLayout
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener

class ListView(
	private val width: Int? = null,
	private val height: Int? = null,
	private val hasLoadingView: Boolean = true,
	private val itemGap: Int = 0,
	private val gapTop: Int = 0,
	private val gapRight: Int = 0,
	private val gapBottom: Int = 0,
	private val gapLeft: Int = 0
) : JPanel2(VerticalLayout(itemGap)) {
	
	private lateinit var mouseEnterListener: (item: JPanel2) -> Unit
	private lateinit var mouseExitListener: (item: JPanel2) -> Unit
	private lateinit var doubleClickListener: (item: JPanel2) -> Unit
	private lateinit var rightClickListener: (item: JPanel2, e: MouseEvent) -> Unit
	private lateinit var contextMenuListener: (item: JPanel2) -> Unit
	
	private var filterFinishedListener: ((showingItem: Int) -> Unit)? = null
	
	private var modernUIBar = ModernScrollBarUI()
	
	private var popupMenu: JPopupMenu? = null
	private var contextMenuOn: Boolean = false
	
	private var searchDebounceTimer: Timer? = null
	
	private var emptyView: JPanel? = null
	
	private var loadingTextView = JLabel("Loading...", SwingConstants.CENTER)
	private lateinit var loadingView: JPanel
	
	private var itemBorder: ListItemBorder? = null
	
	private var highlightBackground: Boolean = true
	
	val scrollPane: JScrollPane = object : JScrollPane(this) {
		override fun isOptimizedDrawingEnabled() = false
	}
	
	private val hoverController: ListItemHoverAdapter = ListItemHoverAdapter(object : ListItemHoverListener {
		override fun onHoverEnter(item: JPanel2) {
			if (highlightBackground) {
				item.background = UIManager.getColor("List.selectionBackground")
			}
			
			if (!::mouseEnterListener.isInitialized) {
				item.repaint()
				return
			}
			
			mouseEnterListener.invoke(item)
			item.repaint()
		}
		
		override fun onHoverExit(item: JPanel2) {
			if (highlightBackground) {
				item.background = UIManager.getColor("AD.itemBG")
			}
			
			if (!::mouseExitListener.isInitialized) {
				item.repaint()
				return
			}
			
			mouseExitListener.invoke(item)
			item.repaint()
		}
		
		override fun onDoubleClick(item: JPanel2, e: MouseEvent?) {
			if (!::doubleClickListener.isInitialized) return
			
			doubleClickListener.invoke(item)
		}
		
		override fun onRightClick(item: JPanel2, e: MouseEvent) {
			if (!::rightClickListener.isInitialized) return
			
			if (item != hoverController.hoveredItem) {
				return
			}
			
			rightClickListener.invoke(item, e)
		}
		
		override fun onContextMenu(item: JPanel2) {
			if (!::contextMenuListener.isInitialized) return
			
			contextMenuListener.invoke(item)
		}
	})
	
	init {
		border = BorderFactory.createEmptyBorder(gapTop, gapLeft, gapBottom, gapRight)
		hoverController.install(this)
		
		scrollPane.apply {
			val width = this@ListView.width ?: preferredSize.width
			val height = this@ListView.height ?: preferredSize.height
			
			preferredSize = Dimension(width, height)
			
			border = BorderFactory.createEmptyBorder()
			viewportBorder = BorderFactory.createEmptyBorder()
			
			isOpaque = false
			viewport.isOpaque = false
			
			layout = OverlayScrollPaneLayout()
			
			/*
			 * Adjust z-index for the vertical scrollbar
			 * */
			onSwing {
				verticalScrollBar?.let { bar ->
					setComponentZOrder(bar, 0)
				}
			}
			
			// Set UI and adjust smooth, fast scrolling
			verticalScrollBar.ui = modernUIBar
			verticalScrollBar.unitIncrement = 20
			
			horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
			verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
			
			// Show thumb on mouse wheel (Even if mouse is not over the scrollbar)
			addMouseWheelListener {
				if (!canScroll()) return@addMouseWheelListener
				modernUIBar.showThumb()
			}
		}
		
		/*
		 * Stop on focus key up/down event on the scroll pane
		 * */
		val dummyAction = object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				// Do absolutely nothing
			}
		}
		
		// These are the internal strings JScrollPane uses for keyboard actions
		val am = scrollPane.actionMap
		
		arrayOf(
			"unitScrollDown", "unitScrollUp",
			"scrollDown", "scrollUp",
			"pageDown", "pageUp",
			"scrollHome", "scrollEnd"
		).forEach { actionKey ->
			am.put(actionKey, dummyAction)
		}
		
		addMouseMotionListener(object : MouseAdapter() {
			override fun mouseMoved(e: MouseEvent?) {
				if (contextMenuOn) return
				if (!canScroll()) return
				
				modernUIBar.showThumb()
			}
		})
		
		scrollPane.verticalScrollBar.apply {
			ui = this@ListView.modernUIBar
			isOpaque = false
			
			addMouseListener(object : MouseAdapter() {
				override fun mouseEntered(e: MouseEvent) {
					if (contextMenuOn) return
					
					modernUIBar.showThumb(false)
					
					hoverController.hoveredItem?.let {
						if (::mouseExitListener.isInitialized) {
							mouseExitListener.invoke(it)
						}
						
						if (highlightBackground) {
							hoverController.hoveredItem?.background = UIManager.getColor("AD.itemBG")
							hoverController.hoveredItem?.repaint()
						}
						
						hoverController.hoveredItem = null
					}
				}
				
				override fun mouseExited(e: MouseEvent) {
					if (contextMenuOn) return
					modernUIBar.startTimer()
				}
			})
			
			// Show on drag (Adjustment events handle both clicking and dragging)
			addAdjustmentListener { e ->
				if (
					e.valueIsAdjusting ||
					!scrollPane.verticalScrollBar.isVisible ||
					scrollPane.verticalScrollBar.maximum <= scrollPane.verticalScrollBar.visibleAmount
				) {
					return@addAdjustmentListener
				}
				
				if (contextMenuOn) return@addAdjustmentListener
				
				modernUIBar.showThumb(false)
			}
		}
		
		if (hasLoadingView) {
			setupLoadingView()
		}
		
		App.listenThemeChange {
			setScrollBarUI()
		}
	}
	
	fun setOnItemHover(callback: (item: JPanel2) -> Unit) {
		mouseEnterListener = callback
	}
	
	fun setOnItemMouseExit(callback: (item: JPanel2) -> Unit) {
		mouseExitListener = callback
	}
	
	fun setOnItemDoubleClick(callback: (item: JPanel2) -> Unit) {
		doubleClickListener = callback
	}
	
	fun setOnItemRightClick(callback: (item: JPanel2, e: MouseEvent) -> Unit) {
		rightClickListener = callback
	}
	
	fun setOnContextMenu(callback: (item: JPanel2) -> Unit) {
		contextMenuListener = callback
	}
	
	fun setOnFilterFinished(callback: (showingItem: Int) -> Unit) {
		filterFinishedListener = callback
	}
	
	fun setItemBorder(top: Int, right: Int, bottom: Int, left: Int, thickness: Float = 1f, color: Color? = null) {
		itemBorder = ListItemBorder(top, right, bottom, left, thickness, color)
	}
	
	fun registerPopupMenu(menu: JPopupMenu) {
		popupMenu = menu
		
		popupMenu!!.addPopupMenuListener(object : PopupMenuListener {
			override fun popupMenuWillBecomeVisible(e: PopupMenuEvent?) {
				contextMenuOn = true
				hoverController.lockHover()
			}
			
			override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent?) {
				contextMenuOn = false
				hoverController.resumeHover()
			}
			
			override fun popupMenuCanceled(e: PopupMenuEvent?) {
				contextMenuOn = false
				hoverController.resumeHover()
			}
		})
	}
	
	fun isPopupMenuShown() = contextMenuOn
	
	fun addItem(view: JPanel2, immediateUpdate: Boolean = true) {
		// Hide loading view
		if (hasLoadingView) {
			loadingView.isVisible = false
		}
		
		// Assign the automatic border
		if (itemBorder != null) {
			view.border = itemBorder
		}
		
		add(view)
		
		emptyView?.isVisible = false
		
		if (immediateUpdate) {
			revalidate()
			repaint()
		}
	}
	
	fun filterView(filterCallback: (item: JPanel2) -> Boolean) {
		// 1. Stop any existing timer so we "restart" the clock on every keystroke
		searchDebounceTimer?.stop()
		
		
		// 2. Create a new timer with a 150ms delay
		searchDebounceTimer = Timer(150) {
			var count = 0
			
			// This code runs only after the user stops typing for 300ms
			components
				.filterIsInstance<JPanel2>().forEach { panel ->
					
					val visible = filterCallback(panel)
					panel.isVisible = visible
					
					if (visible) count++
				}
			
			/*
			 * Build empty view and show/hide based on count
			 * */
			validateEmptyView(count == 0)
			
			if (filterFinishedListener != null) {
				filterFinishedListener!!(count)
			}
			
			// Ensure the list updates its layout and shows/hides the empty view
			revalidate()
			repaint()
		}.apply {
			isRepeats = false // Crucial: Execute only once per typing pause
			start()
		}
	}
	
	fun setEmptyView(view: JPanel) {
		emptyView = view
		
		view.name = "list_empty_view"
		view.isVisible = false
		
		add(emptyView)
	}
	
	fun clearFilteredList() {
		components.forEach { panel ->
			if (panel.name == "list_empty_view") return
			if (panel.name == "list_loading_view") return
			
			panel.isVisible = true
		}
		
		revalidate()
		repaint()
	}
	
	fun clearList(showLoadingView: Boolean = true) {
		// 1. Identify what needs to be removed first
		val toRemove = components.filter {
			it.name != "list_empty_view" &&
			it.name != "list_loading_view"
		}
		
		// 2. Hide the empty  view if it exists
		emptyView?.isVisible = false
		
		// 3. Toggle loading view accordingly
		if (hasLoadingView) {
			loadingView.isVisible = showLoadingView
		}
		
		// 4. Remove the identified items
		toRemove.forEach { remove(it) }
	}
	
	fun setLoadingText(text: String) {
		if (!hasLoadingView) return
		
		loadingTextView.text = text
	}
	
	fun itemListChanged() {
		val toRemove = components.filter {
			it.name != "list_empty_view" &&
					it.name != "list_loading_view"
		}
		
		if (toRemove.isEmpty()) {
			loadingView.isVisible = false
			validateEmptyView(true)
		} else {
			loadingView.isVisible = false
			emptyView?.isVisible = false
		}
		
		revalidate()
		repaint()
	}
	
	fun disableHighlightingBG() {
		highlightBackground = false
	}
	
	fun disableTrack() {
		modernUIBar.disableTrackBar = true
	}
	
	private fun validateEmptyView(visibility: Boolean) {
		if (emptyView == null) {
			setupEmptyView()
		}
		
		emptyView!!.preferredSize = Dimension(this@ListView.scrollPane.width, this@ListView.scrollPane.height)
		emptyView!!.isVisible = visibility
	}
	
	private fun setupEmptyView() {
		emptyView = JPanel2(BorderLayout()).apply {
			name = "list_empty_view"
			isVisible = false
			
			val label = JLabel("No result found", SwingConstants.CENTER)
			add(label, BorderLayout.CENTER)
		}
		
		add(emptyView)
	}
	
	private fun setupLoadingView() {
		loadingView = object : JPanel(GridBagLayout()) {
			override fun getPreferredSize(): Dimension {
				// If we have a parent (the ScrollPane viewport), match its size
				// Otherwise, fall back to a reasonable default
				// return parent?.size ?: super.getPreferredSize()
				val vp = SwingUtilities.getAncestorOfClass(JViewport::class.java, this)
				return vp?.size ?: super.getPreferredSize()
			}
		}.apply {
			name = "list_loading_view"
			isVisible = true
			
			// GridBagLayout will now center the label within the
			// "Force-Expanded" height we provided above.
			val label = loadingTextView.apply {
				putClientProperty("FlatLaf.style", "font: 130% bold; foreground: @disabledForeground;")
			}
			
			add(label, GridBagConstraints())
		}
		
		loadingView.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
		loadingView.layout = GridBagLayout()
		
		add(loadingView)
	}
	
	private fun canScroll(): Boolean {
		val bar = scrollPane.verticalScrollBar
		return bar.isVisible && bar.maximum > bar.visibleAmount
	}
	
	private fun setScrollBarUI() {
		modernUIBar = ModernScrollBarUI()
		scrollPane.verticalScrollBar.ui = modernUIBar
	}
	
	override fun updateUI() {
		super.updateUI()
		
		if (itemBorder == null) return
		
		// When the theme changes, tell all children borders to refresh their colors
		components.forEach { comp ->
			if (comp is JComponent && comp.border is ListItemBorder) {
				(comp.border as ListItemBorder).updateColor()
			}
		}
	}
	
	class OverlayScrollPaneLayout : ScrollPaneLayout() {
		override fun layoutContainer(parent: Container?) {
			val container = parent as? JScrollPane ?: return
			val availR = container.bounds
			val insets = container.insets
			
			
			val w = availR.width - insets.left - insets.right
			val h = availR.height - insets.top - insets.bottom
			
			viewport?.setBounds(0, 0, w, h)
			
			if (vsb != null && vsb.isVisible) {
				val barWidth = vsb.preferredSize.width.coerceAtLeast(10)
				vsb.setBounds(w - barWidth, 0, barWidth, h)
			}
		}
	}
	
	class ModernScrollBarUI : FlatScrollBarUI() {
		private var thumbAlpha = 0
		private var fadeTimer: Timer? = null
		private var hideDelayTimer: Timer? = null
		
		private var currentThumbColor: Color = Color.GRAY
		private var currentTrackColor: Color = Color.LIGHT_GRAY
		
		var disableTrackBar: Boolean = false
		
		override fun configureScrollBarColors() {
			super.configureScrollBarColors()
			
			// Pull exact colors from UIManager (your .properties file)
			currentThumbColor = UIManager.getColor("AD.thumb") ?: Color(100, 100, 100)
			currentTrackColor = UIManager.getColor("AD.track") ?: Color(200, 200, 200)
		}
		
		private fun fade(fadeIn: Boolean) {
			fadeTimer?.stop()
			
			fadeTimer = Timer(15) {
				if (fadeIn) {
					if (thumbAlpha < 255) thumbAlpha += 25 else (it.source as Timer).stop()
				} else {
					if (thumbAlpha > 0) thumbAlpha -= 15 else (it.source as Timer).stop()
				}
				thumbAlpha = thumbAlpha.coerceIn(0, 255)
				scrollbar?.repaint()
			}
			
			fadeTimer?.start()
		}
		
		override fun paintTrack(g: Graphics, c: JComponent?, trackBounds: Rectangle) {// ADD THIS CHECK HERE:
			if (disableTrackBar) return
			
			if (thumbAlpha <= 0 || thumbBounds?.isEmpty != false) return
			
			// Pass a 'maxAlpha' of 150 (out of 255) to keep it see-through
			drawRect(g, trackBounds, currentTrackColor, maxAlpha = 150)
		}
		
		override fun paintThumb(g: Graphics, c: JComponent?, thumbBounds: Rectangle) {
			if (thumbBounds.isEmpty || !scrollbar.isEnabled || thumbAlpha == 0) return
			
			// Thumb stays solid (maxAlpha 255)
			drawRect(g, thumbBounds, currentThumbColor, maxAlpha = 255)
		}
		
		private fun drawRect(g: Graphics, bounds: Rectangle, color: Color, maxAlpha: Int) {
			val g2 = g.create() as Graphics2D
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			
			// Calculate the current alpha based on the fade state
			// and the maximum allowed transparency for this specific element
			val alphaFactor = thumbAlpha / 255f
			val finalAlpha = (maxAlpha * alphaFactor).toInt()
			
			g2.color = Color(
				color.red,
				color.green,
				color.blue,
				finalAlpha
			)
			
			// Using fillRoundRect looks much "softer" for modern overlays
			g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
			g2.dispose()
		}
		
		// Standard helpers
		fun showThumb(startTimer: Boolean = true) {
			hideDelayTimer?.stop()
			fade(true)
			if (startTimer) startTimer()
		}
		
		fun startTimer() {
			hideDelayTimer = Timer(1200) { fade(false) }.apply {
				isRepeats = false
				start()
			}
		}
		
		override fun createDecreaseButton(orientation: Int) = createZeroButton()
		
		override fun createIncreaseButton(orientation: Int) = createZeroButton()
		
		private fun createZeroButton() = JButton().apply {
			preferredSize = Dimension(0, 0)
		}
	}
	
}