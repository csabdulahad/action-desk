package net.abdulahad.action_desk.lib.view

import com.formdev.flatlaf.extras.components.FlatButton
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import javax.swing.Icon
import javax.swing.SwingUtilities

class ButtonBadge : FlatButton() {
	var badgeIcon: Icon? = null
		set(value) {
			field = value
			repaint()
		}
	
	var isBadgeVisible: Boolean = false
		set(value) {
			field = value
			repaint()
		}
	
	var badgeTopPadding: Int = 0
		set(value) {
			field = value
			repaint()
		}
	
	var badgeRightPadding: Int = 0
		set(value) {
			field = value
			repaint()
		}
	
	override fun paintComponent(g: Graphics) {
		super.paintComponent(g)
		
		val badge = badgeIcon
		if (!isBadgeVisible || badge == null || icon == null) return
		
		val g2 = g.create() as Graphics2D
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			
			val iconRect = Rectangle()
			val textRect = Rectangle()
			val viewRect = Rectangle(width, height)
			val insets = insets
			
			viewRect.x += insets.left
			viewRect.y += insets.top
			viewRect.width -= insets.left + insets.right
			viewRect.height -= insets.top + insets.bottom
			
			SwingUtilities.layoutCompoundLabel(
				this,
				g2.fontMetrics,
				text,
				icon,
				verticalAlignment,
				horizontalAlignment,
				verticalTextPosition,
				horizontalTextPosition,
				viewRect,
				iconRect,
				textRect,
				iconTextGap
			)
			
			val x = iconRect.x + iconRect.width - badge.iconWidth - badgeRightPadding
			val y = iconRect.y + badgeTopPadding
			badge.paintIcon(this, g2, x, y)
		} finally {
			g2.dispose()
		}
	}
}
