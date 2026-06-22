package net.abdulahad.action_desk.lib.view

import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Insets
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.Window

object DialogPositioner {
	
	fun place(
		window: Window,
		position: String?,
		targetGraphicsConfiguration: GraphicsConfiguration? = null,
		margin: Int = 20
	) {
		val graphicsConfig = targetGraphicsConfiguration
			?: window.graphicsConfiguration
			?: defaultGraphicsConfiguration()
		
		val usableBounds = usableBounds(graphicsConfig)
		val tokens = positionTokens(position)
		
		val x = when {
			"left" in tokens -> usableBounds.x + margin
			"right" in tokens -> usableBounds.x + usableBounds.width - window.width - margin
			else -> usableBounds.x + ((usableBounds.width - window.width) / 2)
		}
		
		val y = when {
			"top" in tokens -> usableBounds.y + margin
			"bottom" in tokens -> usableBounds.y + usableBounds.height - window.height - margin
			else -> usableBounds.y + ((usableBounds.height - window.height) / 2)
		}
		
		window.setLocation(
			clampX(x, usableBounds, window.width),
			clampY(y, usableBounds, window.height)
		)
	}
	
	fun usableBounds(graphicsConfig: GraphicsConfiguration = defaultGraphicsConfiguration()): Rectangle {
		return usableBounds(
			graphicsConfig.bounds,
			Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfig)
		)
	}
	
	fun positionTokens(position: String?): Set<String> {
		val cleanPosition = position
			?.trim()
			?.lowercase()
			?.replace("_", " ")
			?.replace("-", " ")
			?.replace(",", " ")
			?: ""
		
		if (cleanPosition.isBlank() || cleanPosition == "center") {
			return setOf("center")
		}
		
		return cleanPosition
			.split(Regex("\\s+"))
			.filter { it.isNotBlank() }
			.toSet()
	}
	
	private fun usableBounds(bounds: Rectangle, insets: Insets): Rectangle {
		return Rectangle(
			bounds.x + insets.left,
			bounds.y + insets.top,
			bounds.width - insets.left - insets.right,
			bounds.height - insets.top - insets.bottom
		)
	}
	
	private fun defaultGraphicsConfiguration(): GraphicsConfiguration {
		return GraphicsEnvironment
			.getLocalGraphicsEnvironment()
			.defaultScreenDevice
			.defaultConfiguration
	}
	
	private fun clampX(x: Int, bounds: Rectangle, width: Int): Int {
		val min = bounds.x
		val max = (bounds.x + bounds.width - width).coerceAtLeast(min)
		
		return x.coerceIn(min, max)
	}
	
	private fun clampY(y: Int, bounds: Rectangle, height: Int): Int {
		val min = bounds.y
		val max = (bounds.y + bounds.height - height).coerceAtLeast(min)
		
		return y.coerceIn(min, max)
	}
	
}
