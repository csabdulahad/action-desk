package net.abdulahad.action_desk.lib.view.jlist

import net.abdulahad.action_desk.lib.view.JPanel2
import java.awt.Point
import java.awt.event.*
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.SwingUtilities

class ListItemHoverAdapter (
	private val listener: ListItemHoverListener
) {
	
	private val itemClass = JPanel2::class.java
	
	private lateinit var listPanel: JPanel2
	var hoveredItem: JPanel2? = null
	private var lock: Boolean = false
	
	fun install(panel: JPanel2) {
		listPanel = panel
		installPanelListeners()
		installKeyboard()
	}
	
	fun lockHover() {
		lock = true
	}
	
	fun resumeHover() {
		lock = false
	}
	
	fun clearHover() {
		hoveredItem?.let { listener.onHoverExit(it) }
		hoveredItem = null
	}
	
	private fun installPanelListeners() {
		listPanel.addMouseMotionListener(object : MouseMotionAdapter() {
			override fun mouseMoved(e: MouseEvent) {
				handleMove(e)
			}
		})
		
		listPanel.addMouseListener(object : MouseAdapter() {
			override fun mouseExited(e: MouseEvent) {
				handleExit(e)
			}
			
			override fun mouseClicked(e: MouseEvent) {
				handleClick(e)
			}
		})
	}
	
	private fun installKeyboard() {
		listPanel.isFocusable = true
		
		val im = listPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
		val am = listPanel.actionMap
		
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "hoverDown")
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "hoverUp")
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "activate")
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "contextMenu")
		
		am.put("hoverDown", object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				moveHover(+1)
			}
		})
		
		am.put("hoverUp", object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				moveHover(-1)
			}
		})
		
		am.put("activate", object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				if (hoveredItem == null) return
				listener.onDoubleClick(hoveredItem!!, null)
			}
		})
		
		am.put("contextMenu", object : AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				openContextMenuFromKeyboard()
			}
		})
	}
	
	private fun handleMove(e: MouseEvent) {
		if (lock) return
		
		val p = e.point
		
		if (!listPanel.visibleRect.contains(p)) {
			clearHover()
			return
		}
		
		val item = findVisibleItemAt(p)
		
		if (item == null && hoveredItem != null) {
			listener.onHoverExit(hoveredItem as JPanel2)
			hoveredItem = null
		}
		
		if (item == null) return
		
		if (item !== hoveredItem) {
			hoveredItem?.let { listener.onHoverExit(it) }
			hoveredItem = item
			listener.onHoverEnter(item)
		}
	}
	
	private fun handleExit(e: MouseEvent) {
		if (lock) return
		
		val p = SwingUtilities.convertPoint(e.component, e.point, listPanel)
		if (listPanel.visibleRect.contains(p)) return
		
		clearHover()
	}
	
	private fun handleClick(e: MouseEvent) {
		val p = e.point
		val item = findVisibleItemAt(p) ?: return
		
		// Don't interfere with child controls
		if (e.isConsumed) return
		
		when {
			SwingUtilities.isRightMouseButton(e) ->
				listener.onRightClick(item, e)
			
			e.clickCount == 2 ->
				listener.onDoubleClick(item, e)
			
			else ->
				listener.onClick(item, e)
		}
	}
	
	private fun moveHover(delta: Int) {
		val items = getVisibleItems()
		if (items.isEmpty()) return
		
		val currentIndex = hoveredItem?.let { items.indexOf(it) } ?: -1
		
		val nextIndex = when {
			currentIndex == -1 -> if (delta > 0) 0 else items.lastIndex
			else -> (currentIndex + delta + items.size) % items.size
		}
		
		val nextItem = items[nextIndex]
		
		if (nextItem !== hoveredItem) {
			hoveredItem?.let { listener.onHoverExit(it) }
			hoveredItem = nextItem
			listener.onHoverEnter(nextItem)
			
			ensureVisible(nextItem)
		}
	}
	
	private fun ensureVisible(item: JPanel2) {
		val r = item.bounds
		listPanel.scrollRectToVisible(r)
	}
	
	private fun getVisibleItems(): List<JPanel2> {
		val result = ArrayList<JPanel2>()
		
		for (comp in listPanel.components) {
			if (!itemClass.isInstance(comp)) continue
			val item = itemClass.cast(comp)
			
			if (!item.isVisible) continue
			
			result.add(item as JPanel2)
		}
		
		return result
	}
	
	private fun openContextMenuFromKeyboard() {
		val item = hoveredItem ?: return
		lock = true
		
		// Ensure item is visible before popup
		ensureVisible(item)
		
		listener.onContextMenu(item)
	}
	
	private fun findVisibleItemAt(p: Point): JPanel2? {
		val visible = listPanel.visibleRect
		
		for (comp in listPanel.components) {
			if (!itemClass.isInstance(comp)) continue
			
			if (comp.name == "list_empty_view") continue
			if (comp.name == "list_loading_view") continue
			
			if (!comp.isVisible) continue
			
			val item = itemClass.cast(comp)
			
			if (!item.bounds.intersects(visible)) continue
			if (!item.bounds.contains(p)) continue
			
			return item as JPanel2
		}
		
		return null
	}
	
}