package net.abdulahad.action_desk.repo.action

import net.abdulahad.action_desk.model.Action

interface ActionRepoListener {
	
	fun onActionRepoLoaded() {}
	
	fun onActionDeleted(action: Action) {}
	
	fun onActionAdded(action: Action) {}
	
	fun onActionUpdated(action: Action) {}
	
}