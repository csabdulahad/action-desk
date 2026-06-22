package net.abdulahad.action_desk.engine.adcd

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import net.abdulahad.action_desk.App
import java.nio.charset.StandardCharsets

class AdcdDialogHttpHandler : HttpHandler {
	
	override fun handle(exchange: HttpExchange) {
		try {
			if (exchange.requestMethod.uppercase() != "POST") {
				sendJson(exchange, 405, DialogJson.errorToJson("Method not allowed"))
				return
			}
			
			val body = exchange.requestBody
				.bufferedReader(StandardCharsets.UTF_8)
				.use { it.readText() }
			
			App.logInfo("ADCD: /adcd/v1/dialog request received")
			
			val result = DialogService.show(body)
			sendJson(exchange, 200, DialogJson.resultToJson(result))
		} catch (e: IllegalArgumentException) {
			App.logErr("ADCD: bad request")
			App.logErr(e.message)
			
			sendJson(exchange, 400, DialogJson.errorToJson(e.message ?: "Bad request"))
		} catch (e: Exception) {
			App.logErr("ADCD: request failed")
			App.logErr(e.stackTraceToString())
			
			sendJson(exchange, 500, DialogJson.errorToJson("Internal ADCD error"))
		}
	}
	
	private fun sendJson(exchange: HttpExchange, statusCode: Int, body: String) {
		val bytes = body.toByteArray(StandardCharsets.UTF_8)
		
		exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
		exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
		
		exchange.responseBody.use {
			it.write(bytes)
		}
	}
	
}