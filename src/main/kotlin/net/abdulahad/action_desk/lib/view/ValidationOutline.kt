package net.abdulahad.action_desk.lib.view

import javax.swing.JComponent

object ValidationOutline {
	
	fun error(component: JComponent, message: String?) {
		component.putClientProperty("JComponent.outline", "error")
		component.toolTipText = message
	}
	
	fun clear(component: JComponent) {
		component.putClientProperty("JComponent.outline", null)
		component.toolTipText = null
	}
	
}
