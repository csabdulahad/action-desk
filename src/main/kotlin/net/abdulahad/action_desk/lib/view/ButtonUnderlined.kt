package net.abdulahad.action_desk.lib.view

import com.formdev.flatlaf.extras.components.FlatButton
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.border.EmptyBorder

class ButtonUnderlined(private val txt: String) : FlatButton() {
	init {
		text = html(false)
		horizontalAlignment = LEFT
		verticalAlignment = TOP
		isContentAreaFilled = false
		isBorderPainted = false
		isFocusable = false
		margin = Insets(0, 0, 0, 0)
		border = EmptyBorder(0, 8, 0, 8)
		maximumSize = Dimension(preferredSize.width, Int.MAX_VALUE)
		
		addMouseListener(object : MouseAdapter() {
			override fun mouseEntered(e: MouseEvent) {
				text = html(true)
				cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
			}
			
			override fun mouseExited(e: MouseEvent) {
				text = html(false)
				cursor = Cursor.getDefaultCursor()
			}
		})
	}
	
	private fun html(underlined: Boolean): String {
		return if (underlined) "<html><u>$txt</u></html>" else "<html>$txt</html>"
	}
}
