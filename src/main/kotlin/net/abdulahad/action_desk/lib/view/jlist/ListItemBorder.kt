package net.abdulahad.action_desk.lib.view.jlist

import com.formdev.flatlaf.extras.components.FlatSeparator
import com.formdev.flatlaf.util.HiDPIUtils
import com.formdev.flatlaf.util.UIScale
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.UIManager
import javax.swing.border.AbstractBorder
import kotlin.math.roundToInt

class ListItemBorder (
	private val top: Int = 1,
	private val right: Int = 1,
	private val bottom: Int = 1,
	private val left: Int = 1,
	private val thickness: Float = 1f,
	private var color: Color? = null
) : AbstractBorder() {
	
	private var themedColor = true
	
	init {
		themedColor = color == null
		
		if (color == null) {
			color = UIManager.getColor("Component.borderColor")
		}
	}
	
	fun updateColor() {
		if (!themedColor) return
		color = UIManager.getColor("Component.borderColor")
	}
	
	override fun getBorderInsets(c: Component): Insets {
		// We always reserve space for a bottom border (+1px line)
		// to prevent the layout from "jumping" when the last item changes.
		return Insets(
			UIScale.scale(top),
			UIScale.scale(left),
			UIScale.scale(bottom + 1),
			UIScale.scale(right))
	}
	
	override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
		val parent = c.parent as? JComponent ?: return
		
		// AUTOMATIC DETECTION:
		// Find all visible components that are actually list items
		val visibleItems = parent.components.filter {
			it.isVisible && it !is FlatSeparator &&
					it.name != "list_empty_view" &&
					it.name != "list_loading_view"
		}
		
		// If this component is the last visible item, DON'T draw the line
		if (visibleItems.lastOrNull() == c) return
		
		val g2 = g.create() as Graphics2D
		try {
			g2.color = color
			
			// Add the 6th parameter (scale) to the lambda signature
			HiDPIUtils.paintAtScale1x(g2, x, y, width, height) {
					g2d, x1x, y1x, w1x, h1x, scale ->
				
				// Now we are in 1x device pixels
				val scaledThickness = UIScale.scale(thickness)
				val deviceThickness = scaledThickness.roundToInt().coerceAtLeast(1)
				
				g2d.fillRect(x1x, y1x + h1x - deviceThickness, w1x, deviceThickness)
			}
		} finally {
			g2.dispose()
		}
	}
	
	 fun paintBorderx(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
		val parent = c.parent as? JComponent ?: return
		
		// AUTOMATIC DETECTION:
		// Find all visible components that are actually list items
		val visibleItems = parent.components.filter {
			it.isVisible && it !is FlatSeparator &&
			it.name != "list_empty_view" &&
			it.name != "list_loading_view"
		}
		
		// If this component is the last visible item, DON'T draw the line
		if (visibleItems.lastOrNull() == c) return
		
		val g2 = g.create() as Graphics2D
		
		try {
			g2.color = color
			val thickness = UIScale.scale(thickness).toInt()
			
			// Draw line at the bottom
			g2.fillRect(x, y + height - thickness, width, thickness)
		} finally {
			g2.dispose()
		}
	}
	
}