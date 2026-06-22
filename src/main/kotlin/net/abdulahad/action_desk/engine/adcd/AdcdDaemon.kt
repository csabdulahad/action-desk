package net.abdulahad.action_desk.engine.adcd

import com.sun.net.httpserver.HttpServer
import net.abdulahad.action_desk.App
import java.net.InetSocketAddress
import java.util.concurrent.Executors

object AdcdDaemon {
	
	private var server: HttpServer? = null
	private var host: String? = null
	private var port: Int? = null
	
	fun start(host: String, port: Int) {
		if (isRunning()) {
			if (this.host == host && this.port == port) {
				App.logInfo("ADCD: already running on $host:$port")
				return
			}
			
			restart(host, port)
			return
		}
		
		try {
			val address = InetSocketAddress(host, port)
			
			val httpServer = HttpServer.create(address, 0).apply {
				createContext("/adcd/v1/dialog", AdcdDialogHttpHandler())
				executor = Executors.newCachedThreadPool()
				start()
			}
			
			server = httpServer
			this.host = host
			this.port = port
			
			App.logInfo("ADCD: started on $host:$port")
		} catch (e: Exception) {
			server = null
			this.host = null
			this.port = null
			
			App.logErr("ADCD: failed to start on $host:$port")
			App.logErr(e.stackTraceToString())
		}
	}
	
	fun stop() {
		val currentServer = server ?: return
		
		try {
			currentServer.stop(0)
			App.logInfo("ADCD: stopped")
		} catch (e: Exception) {
			App.logErr("ADCD: failed to stop")
			App.logErr(e.stackTraceToString())
		} finally {
			server = null
			host = null
			port = null
		}
	}
	
	fun restart(host: String, port: Int) {
		stop()
		start(host, port)
	}
	
	fun isRunning(): Boolean {
		return server != null
	}
	
}