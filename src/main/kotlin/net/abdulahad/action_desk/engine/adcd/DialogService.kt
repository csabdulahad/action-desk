package net.abdulahad.action_desk.engine.adcd

import net.abdulahad.action_desk.App
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities

object DialogService {
	
	fun show(json: String): DialogResult {
		val spec = DialogJson.parseDialogSpec(json)
		return show(spec)
	}
	
	fun show(spec: DialogSpec): DialogResult {
		return if (spec.awaitResult) {
			showAndAwaitResult(spec)
		} else {
			showAndReturnImmediately(spec)
		}
	}
	
	private fun showAndAwaitResult(spec: DialogSpec): DialogResult {
		val future = CompletableFuture<DialogResult>()
		
		SwingUtilities.invokeLater {
			try {
				DialogBuilder(spec) { result ->
					future.complete(result)
				}.show()
			} catch (e: Exception) {
				App.logErr("ADCD Dialog: failed to show dialog")
				App.logErr(e.stackTraceToString())
				future.completeExceptionally(e)
			}
		}
		
		return future.get()
	}
	
	private fun showAndReturnImmediately(spec: DialogSpec): DialogResult {
		val shownFuture = CompletableFuture<Unit>()
		
		SwingUtilities.invokeLater {
			try {
				DialogBuilder(spec) { result ->
					App.logInfo("ADCD Dialog: non-awaiting dialog completed with button: ${result.button}")
				}.show()
				shownFuture.complete(Unit)
			} catch (e: Exception) {
				App.logErr("ADCD Dialog: failed to show non-awaiting dialog")
				App.logErr(e.stackTraceToString())
				shownFuture.completeExceptionally(e)
			}
		}
		
		shownFuture.get()
		return DialogResult("shown")
	}
	
}
