package net.abdulahad.action_desk.view.learning

import com.formdev.flatlaf.util.UIScale
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.Rectangle
import java.awt.Window
import java.awt.image.BufferedImage
import kotlin.math.ceil
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JViewport
import javax.swing.Scrollable
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

class LearningImageViewer(
	owner: Window?,
	private val image: BufferedImage,
	caption: String? = null,
) : JDialog(owner) {

	init {
		title = caption?.takeIf { it.isNotBlank() } ?: "Image Preview"
		layout = BorderLayout()
		modalityType = ModalityType.MODELESS
		isResizable = true
		preferredSize = Dimension(UIScale.scale(900), UIScale.scale(650))
		minimumSize = Dimension(UIScale.scale(420), UIScale.scale(320))

		add(createScrollPane(), BorderLayout.CENTER)
		add(createBottomPanel(), BorderLayout.SOUTH)

		pack()
		setLocationRelativeTo(owner)
	}

	fun showIt() {
		isVisible = true
		toFront()
	}

	private fun createScrollPane(): JScrollPane {
		return JScrollPane(OriginalImagePanel(image)).apply {
			border = null
			viewport.background = UIManager.getColor("Panel.background") ?: Color.WHITE
			verticalScrollBar.unitIncrement = UIScale.scale(16)
			horizontalScrollBar.unitIncrement = UIScale.scale(16)
		}
	}

	private fun createBottomPanel(): JPanel {
		return JPanel(BorderLayout()).apply {
			border = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")),
				EmptyBorder(8, 12, 8, 12)
			)

			add(JButton("Close").apply {
				addActionListener { dispose() }
			}, BorderLayout.EAST)
		}
	}

	private class OriginalImagePanel(private val image: BufferedImage) : JPanel(), Scrollable {
		private val padding = UIScale.scale(16)

		init {
			background = UIManager.getColor("Panel.background") ?: Color.WHITE
			border = EmptyBorder(padding, padding, padding, padding)
		}

		override fun getPreferredSize(): Dimension {
			val insets = insets
			val imageSize = getOriginalDisplaySize()
			return Dimension(
				imageSize.width + insets.left + insets.right,
				imageSize.height + insets.top + insets.bottom
			)
		}

		override fun paintComponent(g: Graphics) {
			super.paintComponent(g)

			val insets = insets
			val imageSize = getOriginalDisplaySize()
			val availableWidth = width - insets.left - insets.right
			val availableHeight = height - insets.top - insets.bottom
			val x = insets.left + ((availableWidth - imageSize.width) / 2).coerceAtLeast(0)
			val y = insets.top + ((availableHeight - imageSize.height) / 2).coerceAtLeast(0)

			val g2 = g.create() as Graphics2D
			try {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)
				g2.drawImage(image, x, y, imageSize.width, imageSize.height, null)
			} finally {
				g2.dispose()
			}
		}

		private fun getOriginalDisplaySize(): Dimension {
			val scaleX = getDeviceScaleX()
			val scaleY = getDeviceScaleY()
			return Dimension(
				ceil(image.width / scaleX).toInt().coerceAtLeast(1),
				ceil(image.height / scaleY).toInt().coerceAtLeast(1)
			)
		}

		private fun getDeviceScaleX(): Double {
			return getDeviceScale().first
		}

		private fun getDeviceScaleY(): Double {
			return getDeviceScale().second
		}

		private fun getDeviceScale(): Pair<Double, Double> {
			val configuration = graphicsConfiguration
				?: GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
			val transform = configuration.defaultTransform
			return Pair(
				transform.scaleX.takeIf { it > 0.0 } ?: 1.0,
				transform.scaleY.takeIf { it > 0.0 } ?: 1.0
			)
		}

		override fun getPreferredScrollableViewportSize(): Dimension {
			return preferredSize
		}

		override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
			return UIScale.scale(16)
		}

		override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
			return UIScale.scale(96)
		}

		override fun getScrollableTracksViewportWidth(): Boolean {
			val viewport = parent as? JViewport ?: return false
			return viewport.width > preferredSize.width
		}

		override fun getScrollableTracksViewportHeight(): Boolean {
			val viewport = parent as? JViewport ?: return false
			return viewport.height > preferredSize.height
		}
	}

}
