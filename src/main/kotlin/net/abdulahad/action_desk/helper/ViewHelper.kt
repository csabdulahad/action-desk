package net.abdulahad.action_desk.helper

import java.awt.Cursor
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.URI
import javax.swing.JLabel
import javax.swing.JTextArea
import javax.swing.UIManager

object ViewHelper {
	
	fun getLinkLabel(name: String, link: String, tooltip: String? = null): JLabel {
		val label = JLabel("<html><a style='text-decoration: none' href=\"$link\">$name</a></html>")
		label.toolTipText = tooltip
		label.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
		
		label.addMouseListener(object : MouseAdapter() {
			override fun mouseClicked(e: MouseEvent) {
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(URI(link))
				}
			}
		})
		
		return label
	}
	
	fun closeDialogWithEvent(window: Window) {
		val event = WindowEvent(window, WindowEvent.WINDOW_CLOSING)
		window.dispatchEvent(event)
	}
	
	fun hideAndSeekWindow(child: Window, parent: Window? = null, showParent: Boolean = true) {
		child.addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent?) {
				child.dispose()
				
				if (showParent && parent != null) {
					parent.isVisible = true
				}
			}
		})
		
		child.pack()
		child.setLocationRelativeTo(parent)
		
		if (parent != null) {
			parent.isVisible = false
		}
		
		child.isVisible = true
	}
	
	fun wrappedHint(text: String, width: Int = 320): JTextArea {
		return JTextArea(text).apply {
			lineWrap = true
			wrapStyleWord = true
			isEditable = false
			isFocusable = false
			isOpaque = false
			border = null
			
			font = UIManager.getFont("Label.font")
			foreground = UIManager.getColor("Label.disabledForeground")
			
			// Force width first so JTextArea can calculate wrapped height.
			setSize(width, Short.MAX_VALUE.toInt())
			
			val preferred = preferredSize
			
			preferredSize = Dimension(width, preferred.height)
			minimumSize = Dimension(0, preferred.height)
			maximumSize = Dimension(width, preferred.height)
		}
	}
	
}