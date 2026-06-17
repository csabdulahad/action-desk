package net.abdulahad.action_desk.repo.shortcut

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.repo.action.ActionRepo
import net.abdulahad.action_desk.repo.action.ActionRepoListener

object ShortcutRepo : ActionRepoListener {

	init {
		ActionRepo.addListener(this)
	}
	
	override fun onActionRepoLoaded() {
		App.logInfo("Registering shortcuts for loaded actions")
	}
	
	override fun onActionDeleted(action: Action) {
		App.logInfo("Validating registered shortcut on action ${action.id} delete")
	}
	
	override fun onActionAdded(action: Action) {
		App.logInfo("Adding shortcut to action ${action.id}")
	}
	
	override fun onActionUpdated(action: Action) {
		App.logInfo("Validating registered shortcut on action ${action.id} update")
	}
	
}