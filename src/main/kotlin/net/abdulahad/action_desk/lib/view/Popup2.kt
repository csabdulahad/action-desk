package net.abdulahad.action_desk.lib.view

import java.awt.Point
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.SwingUtilities

class Popup2 {
	private var dialog: JDialog? = null
	private lateinit var content: JPanel
	private var decorateDialogCallback: ((JDialog) -> Unit)? = null
	
	private fun isInitialized(): Boolean {
		return dialog != null
	}
	
	fun setContent(panel: JPanel) {
		content = panel
	}
	
	fun show(onView: JComponent) {
		val existing = dialog
		if (existing != null && existing.isVisible) {
			hide()
			return
		}
		
		if (!isInitialized()) {
			val owner = SwingUtilities.getWindowAncestor(onView)
			dialog = JDialog(owner).apply {
				isResizable = false
				decorateDialogCallback?.invoke(this)
				contentPane = content
				pack()
				addWindowFocusListener(object : WindowAdapter() {
					override fun windowLostFocus(e: WindowEvent) {
						hide()
					}
				})
				rootPane.registerKeyboardAction(
					{ hide() },
					KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
					JComponent.WHEN_IN_FOCUSED_WINDOW
				)
			}
		}
		
		positionDialog(onView)
		dialog?.isVisible = true
	}
	
	fun hide() {
		dialog?.isVisible = false
	}
	
	fun setDialogDecoratorCallback(callback: (JDialog) -> Unit) {
		decorateDialogCallback = callback
	}
	
	private fun positionDialog(onView: JComponent) {
		val gc = onView.graphicsConfiguration
		val bounds = gc.bounds
		val insets = Toolkit.getDefaultToolkit().getScreenInsets(gc)
		
		val screenX = bounds.x + insets.left
		val screenY = bounds.y + insets.top
		val screenW = bounds.width - (insets.left + insets.right)
		val screenH = bounds.height - (insets.top + insets.bottom)
		
		val invokerPos = Point(0, 0)
		SwingUtilities.convertPointToScreen(invokerPos, onView)
		
		val prefSize = content.preferredSize
		var x = invokerPos.x
		var y = invokerPos.y + onView.height
		
		if (y + prefSize.height > screenY + screenH) {
			y = invokerPos.y - prefSize.height
		}
		
		if (x + prefSize.width > screenX + screenW) {
			x = screenX + screenW - prefSize.width
		}
		
		dialog?.location = Point(x.coerceAtLeast(screenX), y.coerceAtLeast(screenY))
	}
}
