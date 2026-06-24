package net.abdulahad.action_desk.repo.action

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.engine.action.ActionRunner
import net.abdulahad.action_desk.engine.shortcut.ShortcutManager
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.onIO
import net.abdulahad.action_desk.onUI
import net.abdulahad.action_desk.engine.notification.NotificationManager

object ActionRepo {
	
	private val listeners = mutableListOf<ActionRepoListener>()
	
	private val actions = mutableListOf<Action>()
	
	fun init() {
		onIO {
			// TODO - comment this out
			// sleep(500)
			
			actions.addAll(ActionDao.list())
			
			onUI {
				listeners.forEach { l -> l.onActionRepoLoaded() }
			}
		}
	}
	
	fun addListener(listener: ActionRepoListener) {
		listeners.add(listener)
	}
	
	fun findById(id: Int): Action? {
		return actions.find { it.id == id }
	}
	
	fun findActionById(id: Int): Action? {
		return findById(id)
	}
	
	fun save(action: Action?, afterSaved: ((Action) -> Unit)? = null) {
		if (action == null) return
		
		onIO {
			val updated = action.id != 0
			val id = ActionDao.save(action)
			
			if (!updated) {
				App.logInfo("Action ${action.name} created")
				action.id = id
				actions.add(action)
			} else {
				App.logInfo("Action ${action.name} updated")
			}
			
			ShortcutManager.registerOrUpdate(action.id, action.globalKey) {
				onIO {
					action.byShortcut = true
					ActionRunner.runAction(action, false)
				}
			}
			
			val postSaveError = try {
				afterSaved?.invoke(action)
				null
			} catch (e: Exception) {
				App.logErr("Action ${action.name} saved, but post-save work failed: ${e.message}")
				e
			}
			
			onUI {
				if (postSaveError == null) {
					NotificationManager.success("Action ${action.name} saved")
				} else {
					NotificationManager.error("Action ${action.name} saved, but schedule could not be saved")
				}
				listeners.forEach { l ->
					if (updated) l.onActionUpdated(action) else l.onActionAdded(action)
				}
			}
		}
		
	}
	
	fun delete(action: Action?) {
		if (action == null) return
		
		onIO {
			if (!actions.remove(action)) return@onIO
			
			ActionDao.delete(action.id)
			App.logInfo("Action ${action.name} deleted")
			
			onUI {
				NotificationManager.warn("Action ${action.name} deleted")
				listeners.forEach { l -> l.onActionDeleted(action) }
			}
		}
	}
	
	fun list(): List<Action> {
		return actions
	}
	
	fun getActions(): List<Action> {
		return list()
	}
	
}