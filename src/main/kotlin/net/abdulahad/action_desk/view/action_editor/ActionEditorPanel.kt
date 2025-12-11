package net.abdulahad.action_desk.view.action_editor

import net.abdulahad.action_desk.model.Action

interface ActionEditorPanel {
	
	fun save(action: Action): String?
	
	fun setData(action: Action)
	
}