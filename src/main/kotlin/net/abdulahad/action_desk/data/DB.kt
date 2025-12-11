package net.abdulahad.action_desk.data

import org.jooq.DSLContext
import org.jooq.SQLDialect
import java.sql.Connection
import java.sql.DriverManager
import org.jooq.impl.DSL as DSLJQ

object DB {
	
	private lateinit var path: String
	val PATH: String
		get() = path
		
	private lateinit var conn: Connection
	val CONN: Connection
		get() = conn
	
	private lateinit var dsl: DSLContext
	val DSL: DSLContext
		get() = dsl
	
	@Throws(Exception::class)
	fun init(path: String, initDB: ((conn: Connection, dsl: DSLContext) -> Unit)? = null) {
		this.path = path
		
		conn = DriverManager.getConnection("jdbc:sqlite:$path")
		dsl = DSLJQ.using(conn, SQLDialect.SQLITE)
		
		if (initDB == null) return
		
		initDB(conn, DSL)
	}
	
}