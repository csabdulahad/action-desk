package net.abdulahad.action_desk.lib.view.jlist

import net.abdulahad.action_desk.lib.view.JPanel2
import java.awt.event.MouseEvent

interface ListItemHoverListener {
	fun onHoverEnter(item: JPanel2) {}
	fun onHoverExit(item: JPanel2) {}
	fun onClick(item: JPanel2, e: MouseEvent) {}
	fun onDoubleClick(item: JPanel2, e: MouseEvent?) {}
	fun onRightClick(item: JPanel2, e: MouseEvent) {}
	fun onContextMenu(item: JPanel2) {}
}