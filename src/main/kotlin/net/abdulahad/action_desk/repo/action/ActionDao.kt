package net.abdulahad.action_desk.repo.action

import net.abdulahad.action_desk.data.DB
import net.abdulahad.action_desk.jooq.Tables.ACTION
import net.abdulahad.action_desk.model.Action
import org.jooq.Field
import org.jooq.Record
import org.jooq.impl.DSL
import java.time.LocalDateTime

object ActionDao {
	
	private val CONFIRMATION_BEFORE_RUN: Field<Int> =
		DSL.field(DSL.name("confirmation_before_run"), Int::class.java)
	
	private val PASSWORD_PROTECTED: Field<Int> =
		DSL.field(DSL.name("password_protected"), Int::class.java)
	
	private val ENCRYPTED_PAYLOAD: Field<String> = DSL.field(DSL.name("encrypted_payload"), String::class.java)
	
	private val selectFields = listOf(
		ACTION.ID, ACTION.ICON, ACTION.NAME, ACTION.DESCRIPTION, ACTION.ENABLED,
		ACTION.DIRECTORY, ACTION.COMMAND, ACTION.ARGS,
		
		ACTION.RUN_AS_ADMIN, ACTION.SINGLETON, ACTION.START_WITH_AD,
		
		ACTION.SHOW_WINDOW, ACTION.WINDOW_STYLE, ACTION.BRING_WINDOW,
		
		ACTION.GLOBAL_KEY,
		CONFIRMATION_BEFORE_RUN, PASSWORD_PROTECTED, ENCRYPTED_PAYLOAD
	)
	
	private fun readAction(record: Record): Action {
		return Action(
			id = record.get(ACTION.ID),
			icon = record.get(ACTION.ICON) ?: "",
			name = record.get(ACTION.NAME) ?: "",
			description = record.get(ACTION.DESCRIPTION) ?: "",
			enabled = record.get(ACTION.ENABLED) == 1,
			
			startDirectory = record.get(ACTION.DIRECTORY) ?: "",
			command = record.get(ACTION.COMMAND) ?: "",
			arguments = record.get(ACTION.ARGS) ?: "",
			
			runAsAdmin = record.get(ACTION.RUN_AS_ADMIN) == 1,
			singleton = record.get(ACTION.SINGLETON) == 1,
			startWithAD = record.get(ACTION.START_WITH_AD) == 1,
			
			confirmationBeforeRun = record.get(CONFIRMATION_BEFORE_RUN) == 1,
			passwordProtected = record.get(PASSWORD_PROTECTED) == 1,
			encryptedPayload = record.get(ENCRYPTED_PAYLOAD) ?: "",
			
			showWindow = record.get(ACTION.SHOW_WINDOW) == 1,
			windowStyle = record.get(ACTION.WINDOW_STYLE) ?: "Normal",
			bringWindow = record.get(ACTION.BRING_WINDOW) == 1,
			
			globalKey = record.get(ACTION.GLOBAL_KEY) ?: ""
		)
	}
	
	fun fetchById(id: Int): Action? {
		val record = DB.DSL
			.select(selectFields)
			.from(ACTION)
			.where(ACTION.ID.eq(id))
			.fetchOne() ?: return null
		
		return readAction(record)
	}
	
	fun fetchAutoRunActions(): List<Action> {
		val query = DB.DSL.select(selectFields)
			.from(ACTION)
			.where(ACTION.START_WITH_AD.eq(1), ACTION.ENABLED.eq(1))
		
		return query.fetch { record -> readAction(record) }
	}
	
	fun insert(action: Action): Int {
		val result = DB.DSL.insertInto(ACTION)
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
		
		.set(CONFIRMATION_BEFORE_RUN, if (action.confirmationBeforeRun) 1 else 0)
		.set(PASSWORD_PROTECTED, if (action.passwordProtected) 1 else 0)
		.set(ENCRYPTED_PAYLOAD, action.encryptedPayload.ifBlank { null })
		
		.set(ACTION.SHOW_WINDOW, if (action.showWindow) 1 else 0)
		.set(ACTION.WINDOW_STYLE, action.windowStyle)
		.set(ACTION.BRING_WINDOW, if (action.bringWindow) 1 else 0)
		
		.set(ACTION.GLOBAL_KEY, action.globalKey)
		
		.set(ACTION.CREATED, LocalDateTime.now().toString())
		.returning(ACTION.ID)
		.fetchOne()
		
		return result?.get(ACTION.ID) ?: -1
	}
	
	fun update(action: Action): Int {
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
		
		.set(CONFIRMATION_BEFORE_RUN, if (action.confirmationBeforeRun) 1 else 0)
		.set(PASSWORD_PROTECTED, if (action.passwordProtected) 1 else 0)
		.set(ENCRYPTED_PAYLOAD, action.encryptedPayload.ifBlank { null })
		
		.set(ACTION.SHOW_WINDOW, if (action.showWindow) 1 else 0)
		.set(ACTION.WINDOW_STYLE, action.windowStyle)
		.set(ACTION.BRING_WINDOW, if (action.bringWindow) 1 else 0)
		
		.set(ACTION.GLOBAL_KEY, action.globalKey)
		
		.where(ACTION.ID.eq(action.id))
		.execute()
		
		return action.id
	}
	
	fun save(action: Action ): Int {
		return if (action.id == 0) {
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
		val query = DB.DSL.select(selectFields).from(ACTION)
		
		// Conditionally filter by IDs
		if (!filterByIds.isNullOrEmpty()) {
			query.where(ACTION.ID.`in`(filterByIds))
		}
		
		return query
			.orderBy(ACTION.NAME.asc()).fetch { record -> readAction(record) }
	}
	
	fun listKeys(): List<String> {
		return DB.DSL
			.select(ACTION.GLOBAL_KEY)
			.from(ACTION)
			.fetch { rec ->
				rec.get(ACTION.GLOBAL_KEY)
			}
	}
	
}