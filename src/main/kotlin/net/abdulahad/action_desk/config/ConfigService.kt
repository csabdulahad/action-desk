package net.abdulahad.action_desk.config

import net.abdulahad.action_desk.data.DB
import net.abdulahad.action_desk.jooq.Tables

object ConfigService {
	
	val config: MutableMap<String, String> = mutableMapOf()
	
	fun loadConfig() {
		config.clear()
		
		config.putAll(
			DB.DSL.select(Tables.CONFIG.NAME, Tables.CONFIG.VALUE)
				.from(Tables.CONFIG)
				.fetch()
				.intoMap(Tables.CONFIG.NAME, Tables.CONFIG.VALUE)
		)
	}
	
	fun flush() {
		config.forEach { (key, value) ->
			DB.DSL
				.insertInto(Tables.CONFIG)
				.set(Tables.CONFIG.NAME, key)
				.set(Tables.CONFIG.VALUE, value)
				.onConflict(Tables.CONFIG.NAME) // if config name already exists
				.doUpdate()
				.set(Tables.CONFIG.VALUE, value) // update value
				.execute()
		}
	}
	
	private fun commitToDB(key: String, value: Any) {
		DB.DSL
			.insertInto(Tables.CONFIG)
			.set(Tables.CONFIG.NAME, key)
			.set(Tables.CONFIG.VALUE, value.toString())
			.onConflict(Tables.CONFIG.NAME)
			.doUpdate()
			.set(Tables.CONFIG.VALUE, value.toString())
			.execute()
	}
	
	fun commit(key: String , value: Boolean) {
		commitToDB(key, value)
		config[key] = value.toString()
	}
	
	fun commit(key: String , value: Int) {
		commitToDB(key, value)
		config[key] = "$value"
	}
	
	fun commit(key: String , value: String) {
		commitToDB(key, value)
		config[key] = value
	}
	
	fun getBool(key: String, defValue: Boolean = false): Boolean = config[key]?.toBoolean() ?: defValue
	
	fun getInt(key: String, defValue: Int = 0): Int = config[key]?.toInt() ?: defValue
	
	fun getString(key: String, defValue: String = ""): String = config[key] ?: defValue
	
}