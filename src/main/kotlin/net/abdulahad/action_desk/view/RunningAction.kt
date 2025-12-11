package net.abdulahad.action_desk.view

import net.abdulahad.action_desk.model.Action
import javax.swing.JPanel

object RunningAction: JPanel() {
	
	private fun readResolve(): Any = RunningAction
	
	fun refresh() {
	
	}
	
}