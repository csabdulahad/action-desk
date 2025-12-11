package net.abdulahad.action_desk.dao

import net.abdulahad.action_desk.data.DB
import net.abdulahad.action_desk.jooq.tables.Action.ACTION
import net.abdulahad.action_desk.model.Action
import java.time.LocalDateTime

object ActionDao {
	
	fun fetchById(id: Int): Action? {
		val record = DB.DSL
			.selectFrom(ACTION)
			.where(ACTION.ID.eq(id))
			.fetchOne() ?: return null
		
		return Action(
			id = record.id,
			icon = record.icon ?: "",
			name = record.name ?: "",
			description = record.description ?: "",
			enabled = record.enabled == 1,
			
			startDirectory = record.directory ?: "",
			command = record.command ?: "",
			arguments = record.args ?: "",
			
			runAsAdmin = record.runAsAdmin == 1,
			singleton = record.singleton == 1,
			startWithAD = record.startWithAd == 1,
			
			showWindow = record.showWindow == 1,
			windowStyle = record.windowStyle,
			
			hotkey = record.hotKey ?: "",
			globalKey = record.globalKey ?: ""
		)
	}
	
	fun fetchAutoRunActions(): List<Action> {
		val query = DB.DSL.select(
			ACTION.ID, ACTION.ICON, ACTION.NAME, ACTION.DESCRIPTION, ACTION.ENABLED,
			ACTION.DIRECTORY, ACTION.COMMAND, ACTION.ARGS,
			
			ACTION.RUN_AS_ADMIN, ACTION.SINGLETON, ACTION.START_WITH_AD,
			
			ACTION.SHOW_WINDOW, ACTION.WINDOW_STYLE,
			
			ACTION.HOT_KEY, ACTION.GLOBAL_KEY
		)
		.from(ACTION)
		.where(ACTION.START_WITH_AD.eq(1), ACTION.ENABLED.eq(1))
		
		return query.fetch { record ->
			Action(
				id = record.get(ACTION.ID),
				icon = record.get(ACTION.ICON),
				name = record.get(ACTION.NAME),
				enabled = record.get(ACTION.ENABLED) == 1,
				
				description = record.get(ACTION.DESCRIPTION),
				startDirectory = record.get(ACTION.DIRECTORY),
				command = record.get(ACTION.COMMAND),
				arguments = record.get(ACTION.ARGS),
				
				runAsAdmin = record.get(ACTION.RUN_AS_ADMIN) == 1,
				singleton = record.get(ACTION.SINGLETON) == 1,
				startWithAD = record.get(ACTION.START_WITH_AD) == 1,
				
				showWindow = record.get(ACTION.SHOW_WINDOW) == 1,
				windowStyle = record.get(ACTION.WINDOW_STYLE),
				
				hotkey = record.get(ACTION.HOT_KEY),
				globalKey = record.get(ACTION.GLOBAL_KEY),
			)
		}
	}
	
	fun insert(action: Action) {
		DB.DSL.insertInto(ACTION)
		.set(ACTION.ICON, action.icon)
		.set(ACTION.NAME, action.name)
		.set(ACTION.DESCRIPTION, action.description)
		.set(ACTION.ENABLED, 1)
		
		.set(ACTION.DIRECTORY, action.startDirectory)
		.set(ACTION.COMMAND, action.command)
		.set(ACTION.ARGS, action.arguments)
		
		.set(ACTION.RUN_AS_ADMIN, if (action.runAsAdmin) 1 else 0)
		.set(ACTION.SINGLETON, if (action.singleton) 1 else 0)
		.set(ACTION.START_WITH_AD, if (action.startWithAD) 1 else 0)
		
		.set(ACTION.SHOW_WINDOW, if (action.showWindow) 1 else 0)
		.set(ACTION.WINDOW_STYLE, action.windowStyle)
		
		.set(ACTION.HOT_KEY, action.hotkey)
		.set(ACTION.GLOBAL_KEY, action.globalKey)
		
		.set(ACTION.CREATED, LocalDateTime.now().toString())
		.execute()
	}
	
	fun update(action: Action) {
		DB.DSL.update(ACTION)
		.set(ACTION.ICON, action.icon)
		.set(ACTION.NAME, action.name)
		.set(ACTION.DESCRIPTION, action.description)
		.set(ACTION.ENABLED, 1)
		
		.set(ACTION.DIRECTORY, action.startDirectory)
		.set(ACTION.COMMAND, action.command)
		.set(ACTION.ARGS, action.arguments)
		
		.set(ACTION.RUN_AS_ADMIN, if (action.runAsAdmin) 1 else 0)
		.set(ACTION.SINGLETON, if (action.singleton) 1 else 0)
		.set(ACTION.START_WITH_AD, if (action.startWithAD) 1 else 0)
		
		.set(ACTION.SHOW_WINDOW, if (action.showWindow) 1 else 0)
		.set(ACTION.WINDOW_STYLE, action.windowStyle)
		
		.set(ACTION.HOT_KEY, action.hotkey)
		.set(ACTION.GLOBAL_KEY, action.globalKey)
		
		.where(ACTION.ID.eq(action.id))
		.execute()
	}
	
	fun save(action: Action) {
		if (action.id == 0) {
			insert(action)
		} else {
			update(action)
		}
	}
	
	fun delete(id: Int) {
		DB.DSL
			.deleteFrom(ACTION)
			.where(ACTION.ID.eq(id))
			.execute()
	}
	
	fun list(filterByIds: List<Int>? = null): List<Action> {
		val query = DB.DSL.select(
			ACTION.ID, ACTION.ICON, ACTION.NAME, ACTION.DESCRIPTION, ACTION.ENABLED,
			ACTION.DIRECTORY, ACTION.COMMAND, ACTION.ARGS,
			
			ACTION.RUN_AS_ADMIN, ACTION.SINGLETON, ACTION.START_WITH_AD,
			
			ACTION.SHOW_WINDOW, ACTION.WINDOW_STYLE,
			
			ACTION.HOT_KEY, ACTION.GLOBAL_KEY
		).from(ACTION)
		
		// Conditionally filter by IDs
		if (!filterByIds.isNullOrEmpty()) {
			query.where(ACTION.ID.`in`(filterByIds))
		}
		
		return query
			.orderBy(ACTION.NAME.asc()).fetch { record ->
				Action(
					id = record.get(ACTION.ID),
					icon = record.get(ACTION.ICON),
					name = record.get(ACTION.NAME),
					enabled = record.get(ACTION.ENABLED) == 1,
					
					description = record.get(ACTION.DESCRIPTION),
					startDirectory = record.get(ACTION.DIRECTORY),
					command = record.get(ACTION.COMMAND),
					arguments = record.get(ACTION.ARGS),
					
					runAsAdmin = record.get(ACTION.RUN_AS_ADMIN) == 1,
					singleton = record.get(ACTION.SINGLETON) == 1,
					startWithAD = record.get(ACTION.START_WITH_AD) == 1,
					
					showWindow = record.get(ACTION.SHOW_WINDOW) == 1,
					windowStyle = record.get(ACTION.WINDOW_STYLE),
					
					hotkey = record.get(ACTION.HOT_KEY),
					globalKey = record.get(ACTION.GLOBAL_KEY),
				)
			}
	}
	
	fun listKeys(type: String? = null): List<String> {
		val col = if (type == "hotkey") ACTION.HOT_KEY else ACTION.GLOBAL_KEY
		
		return DB.DSL
			.select(col)
			.from(ACTION)
			.fetch { rec ->
				rec.get(col)
			}
	}
	
}