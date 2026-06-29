package net.abdulahad.action_desk.view.learning

import com.formdev.flatlaf.util.UIScale
import net.abdulahad.action_desk.config.AppConfig
import net.abdulahad.action_desk.lib.util.Poth
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.Scrollable
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument
import javax.swing.JTextPane

class LearningWindow(private val ownerWindow: Window?) : JDialog(ownerWindow) {
	
	companion object {
		private const val MAX_LESSONS = 99
		private const val TEXT_ROOT = "lessons/text"
		private const val IMAGE_ROOT = "lessons/images"
		
		fun hasLessons(): Boolean {
			return lessonExists(1)
		}
		
		private fun lessonPath(index: Int): String {
			return "$TEXT_ROOT/${index.toString().padStart(2, '0')}.txt"
		}
		
		private fun lessonExists(index: Int): Boolean {
			if (index !in 1..MAX_LESSONS) return false
			return Poth.resourceExists(lessonPath(index))
		}
	}
	
	private var lessonIndex = 1
	
	private val contentPanel = LessonContentPanel().apply {
		layout = BoxLayout(this, BoxLayout.Y_AXIS)
		border = EmptyBorder(16, 18, 16, 18)
	}
	
	private val scrollPane = JScrollPane(contentPanel).apply {
		border = null
		verticalScrollBar.unitIncrement = UIScale.scale(16)
		horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		preferredSize = Dimension(UIScale.scale(560), UIScale.scale(430))
	}
	
	private val previousButton = JButton("Previous").apply {
		addActionListener {
			if (lessonExists(lessonIndex - 1)) {
				lessonIndex--
				renderLesson()
			}
		}
	}
	
	private val nextButton = JButton("Next").apply {
		addActionListener {
			if (lessonExists(lessonIndex + 1)) {
				lessonIndex++
				renderLesson()
			}
		}
	}
	
	private val closeButton = JButton("Close").apply {
		addActionListener { dispose() }
	}
	
	private val dontShowAgainCheckbox = JCheckBox("Don't show on next startup").apply {
		isSelected = !AppConfig.getLearningShowOnStartup()
		addActionListener {
			AppConfig.setLearningShowOnStartup(!isSelected)
		}
	}
	
	init {
		setupDialog()
		renderLesson()
	}
	
	fun showIt() {
		pack()
		setLocationRelativeTo(ownerWindow)
		isVisible = true
		toFront()
	}
	
	private fun setupDialog() {
		title = "Getting Started"
		layout = BorderLayout()
		modalityType = ModalityType.MODELESS
		isResizable = true
		minimumSize = Dimension(UIScale.scale(460), UIScale.scale(360))
		
		add(scrollPane, BorderLayout.CENTER)
		add(createBottomPanel(), BorderLayout.SOUTH)
	}
	
	private fun createBottomPanel(): JPanel {
		val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, UIScale.scale(6), 0)).apply {
			add(previousButton)
			add(nextButton)
			add(closeButton)
		}
		
		return JPanel(BorderLayout()).apply {
			border = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")),
				EmptyBorder(8, 12, 8, 12)
			)
			
			add(dontShowAgainCheckbox, BorderLayout.WEST)
			add(buttonPanel, BorderLayout.EAST)
		}
	}
	
	private fun renderLesson() {
		contentPanel.removeAll()
		
		val text = readLesson(lessonIndex)
		if (text == null) {
			addParagraph("No lesson found.")
		} else {
			renderText(text)
		}
		
		previousButton.isEnabled = lessonExists(lessonIndex - 1)
		nextButton.isEnabled = lessonExists(lessonIndex + 1)
		
		contentPanel.revalidate()
		contentPanel.repaint()
		
		SwingUtilities.invokeLater {
			scrollPane.verticalScrollBar.value = 0
		}
	}
	
	private fun readLesson(index: Int): String? {
		return Poth.getAsStream(lessonPath(index))?.bufferedReader()?.use { it.readText() }
	}
	
	private fun renderText(text: String) {
		val paragraph = StringBuilder()
		
		fun flushParagraph() {
			val value = paragraph.toString().trim()
			if (value.isNotEmpty()) {
				addParagraph(value)
			}
			paragraph.clear()
		}
		
		text.lines().forEach { rawLine ->
			val line = rawLine.trim()
			
			when {
				line.isBlank() -> flushParagraph()
				isGapLine(line) -> {
					flushParagraph()
					addGap()
				}
				isImageLine(line) -> {
					flushParagraph()
					addImage(line)
				}
				isBulletLine(line) -> {
					flushParagraph()
					addListItem("•", line.removePrefix("- ").trim())
				}
				isOrderedLine(line) -> {
					flushParagraph()
					val marker = line.substringBefore(".").trim() + "."
					val value = line.substringAfter(".").trim()
					addListItem(marker, value)
				}
				else -> {
					if (paragraph.isNotEmpty()) paragraph.append(' ')
					paragraph.append(line)
				}
			}
		}
		
		flushParagraph()
	}
	
	private fun isImageLine(line: String): Boolean {
		return line.startsWith("[image:", ignoreCase = true) && line.endsWith("]")
	}
	
	private fun isGapLine(line: String): Boolean {
		return line == "-"
	}
	
	private fun isBulletLine(line: String): Boolean {
		return line.startsWith("- ")
	}
	
	private fun isOrderedLine(line: String): Boolean {
		return Regex("^\\d+\\.\\s+.+").matches(line)
	}
	
	private fun parseImageLine(line: String): ImageSpec {
		val body = line
			.substringAfter(":", "")
			.removeSuffix("]")
			.trim()
		
		val parts = body.split("|", limit = 2)
		var imagePart = parts.getOrNull(0)?.trim().orEmpty()
		val caption = parts.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
		var alignment = ImageAlignment.CENTER
		
		val firstColon = imagePart.indexOf(':')
		if (firstColon > 0) {
			val maybeAlignment = imagePart.substring(0, firstColon).trim().lowercase()
			when (maybeAlignment) {
				"left" -> {
					alignment = ImageAlignment.LEFT
					imagePart = imagePart.substring(firstColon + 1).trim()
				}
				"center", "centre" -> {
					alignment = ImageAlignment.CENTER
					imagePart = imagePart.substring(firstColon + 1).trim()
				}
				"right" -> {
					alignment = ImageAlignment.RIGHT
					imagePart = imagePart.substring(firstColon + 1).trim()
				}
			}
		}
		
		return ImageSpec(imagePart, caption, alignment)
	}
	
	private fun addParagraph(text: String) {
		contentPanel.add(createTextPane(text, UIManager.getColor("Label.foreground"), 10))
	}
	
	private fun addGap() {
		contentPanel.add(Box.createVerticalStrut(UIScale.scale(12)))
	}
	
	private fun addListItem(marker: String, text: String) {
		val pane = WrappingTextPane().apply {
			isEditable = false
			isFocusable = false
			isOpaque = false
			border = EmptyBorder(0, UIScale.scale(8), UIScale.scale(6), 0)
			alignmentX = LEFT_ALIGNMENT
			font = UIManager.getFont("Label.font")
		}
		
		val foreground = UIManager.getColor("Label.foreground")
		val markerStyle = createStyle(foreground, bold = false, italic = false)
		runCatching {
			pane.styledDocument.insertString(0, "$marker ", markerStyle)
		}
		writeInlineText(pane.styledDocument, text, foreground, forceItalic = false)
		
		contentPanel.add(pane)
	}
	
	private fun addCaption(text: String, alignment: ImageAlignment) {
		val captionPane = createTextPane(
			text = text,
			foreground = UIManager.getColor("Label.disabledForeground"),
			bottomPadding = 12,
			forceItalic = true,
			maxWidth = UIScale.scale(420)
		)
		
		val flowAlignment = when (alignment) {
			ImageAlignment.LEFT -> FlowLayout.LEFT
			ImageAlignment.CENTER -> FlowLayout.CENTER
			ImageAlignment.RIGHT -> FlowLayout.RIGHT
		}
		
		val captionRow = object : JPanel(FlowLayout(flowAlignment, 0, 0)) {
			override fun getMaximumSize(): Dimension {
				return Dimension(Int.MAX_VALUE, preferredSize.height)
			}
		}.apply {
			isOpaque = false
			alignmentX = LEFT_ALIGNMENT
		}
		
		captionRow.add(captionPane)
		contentPanel.add(captionRow)
	}
	
	private fun addImage(line: String) {
		val spec = parseImageLine(line)
		val image = loadImage(spec.name)
		
		if (image == null) {
			addParagraph("[red: Image not found:] ${spec.name}")
			return
		}
		
		val imagePanel = LessonImagePanel(image, spec.alignment).apply {
			alignmentX = LEFT_ALIGNMENT
			cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
			toolTipText = "Click to open image preview"
			addMouseListener(object : MouseAdapter() {
				override fun mouseClicked(e: MouseEvent) {
					LearningImageViewer(this@LearningWindow, image, spec.caption).showIt()
				}
			})
		}
		
		contentPanel.add(imagePanel)
		
		if (!spec.caption.isNullOrBlank()) {
			addCaption(spec.caption, spec.alignment)
		} else {
			contentPanel.add(Box.createVerticalStrut(UIScale.scale(8)))
		}
	}
	
	private fun loadImage(imageName: String): BufferedImage? {
		val cleanName = imageName.trim().replace('\\', '/')
		if (cleanName.isBlank() || cleanName.contains("..")) return null
		
		val path = if (cleanName.startsWith("lessons/")) cleanName else "$IMAGE_ROOT/$cleanName"
		return Poth.getAsStream(path)?.use { stream -> ImageIO.read(stream) }
	}
	
	private fun createTextPane(
		text: String,
		foreground: Color,
		bottomPadding: Int,
		forceItalic: Boolean = false,
		maxWidth: Int? = null
	): JTextPane {
		val pane = WrappingTextPane(maxWidth).apply {
			isEditable = false
			isFocusable = false
			isOpaque = false
			border = EmptyBorder(0, 0, UIScale.scale(bottomPadding), 0)
			alignmentX = LEFT_ALIGNMENT
			font = UIManager.getFont("Label.font")
		}
		
		writeInlineText(pane.styledDocument, text, foreground, forceItalic)
		return pane
	}
	
	private fun writeInlineText(
		document: StyledDocument,
		text: String,
		foreground: Color,
		forceItalic: Boolean
	) {
		val normal = createStyle(foreground, bold = false, italic = forceItalic)
		val bold = createStyle(foreground, bold = true, italic = forceItalic)
		val italic = createStyle(foreground, bold = false, italic = true)
		val red = createStyle(Color(0xD32F2F), bold = false, italic = forceItalic)
		val highlight = createStyle(
			foreground = UIManager.getColor("Label.foreground") ?: foreground,
			bold = true,
			italic = forceItalic,
			background = UIManager.getColor("TextField.selectionBackground") ?: Color(0xFFE8A3)
		)
		
		fun append(value: String, style: SimpleAttributeSet) {
			if (value.isEmpty()) return
			runCatching {
				document.insertString(document.length, value, style)
			}
		}
		
		var i = 0
		while (i < text.length) {
			when {
				text.startsWith("[highlight:", i, ignoreCase = true) -> {
					val end = text.indexOf(']', i + 11)
					if (end > i) {
						append(" " + text.substring(i + 11, end).trim() + " ", highlight)
						i = end + 1
					} else {
						append(text[i].toString(), normal)
						i++
					}
				}
				text.startsWith("[red:", i, ignoreCase = true) -> {
					val end = text.indexOf(']', i + 5)
					if (end > i) {
						append(text.substring(i + 5, end).trim(), red)
						i = end + 1
					} else {
						append(text[i].toString(), normal)
						i++
					}
				}
				text.startsWith("**", i) -> {
					val end = text.indexOf("**", i + 2)
					if (end > i) {
						append(text.substring(i + 2, end), bold)
						i = end + 2
					} else {
						append(text[i].toString(), normal)
						i++
					}
				}
				text[i] == '*' -> {
					val end = text.indexOf('*', i + 1)
					if (end > i) {
						append(text.substring(i + 1, end), italic)
						i = end + 1
					} else {
						append(text[i].toString(), normal)
						i++
					}
				}
				else -> {
					append(text[i].toString(), normal)
					i++
				}
			}
		}
	}
	
	private fun createStyle(
		foreground: Color,
		bold: Boolean,
		italic: Boolean,
		background: Color? = null,
	): SimpleAttributeSet {
		val labelFont = UIManager.getFont("Label.font")
		return SimpleAttributeSet().apply {
			StyleConstants.setFontFamily(this, labelFont.family)
			StyleConstants.setFontSize(this, labelFont.size)
			StyleConstants.setForeground(this, foreground)
			StyleConstants.setBold(this, bold)
			StyleConstants.setItalic(this, italic)
			if (background != null) {
				StyleConstants.setBackground(this, background)
			}
		}
	}
	
	private data class ImageSpec(
		val name: String,
		val caption: String?,
		val alignment: ImageAlignment,
	)
	
	private enum class ImageAlignment {
		LEFT,
		CENTER,
		RIGHT,
	}
	
	private class LessonImagePanel(
		private val image: BufferedImage,
		private val alignment: ImageAlignment,
	) : JPanel() {
		private val maxDisplayWidth = UIScale.scale(420)
		private val maxDisplayHeight = UIScale.scale(260)
		
		init {
			isOpaque = false
			border = EmptyBorder(UIScale.scale(4), 0, UIScale.scale(8), 0)
		}
		
		override fun getPreferredSize(): Dimension {
			val insets = insets
			val displaySize = calculateDisplaySize(getAvailableDisplayWidth())
			return Dimension(
				displaySize.width + insets.left + insets.right,
				displaySize.height + insets.top + insets.bottom
			)
		}
		
		override fun getMaximumSize(): Dimension {
			return Dimension(Int.MAX_VALUE, preferredSize.height)
		}
		
		override fun paintComponent(g: Graphics) {
			super.paintComponent(g)
			
			val insets = insets
			val availableWidth = (width - insets.left - insets.right).coerceAtLeast(1)
			val displaySize = calculateDisplaySize(availableWidth)
			val x = when (alignment) {
				ImageAlignment.LEFT -> insets.left
				ImageAlignment.CENTER -> insets.left + ((availableWidth - displaySize.width) / 2).coerceAtLeast(0)
				ImageAlignment.RIGHT -> insets.left + (availableWidth - displaySize.width).coerceAtLeast(0)
			}
			val y = insets.top
			
			val g2 = g.create() as Graphics2D
			try {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
				g2.drawImage(image, x, y, displaySize.width, displaySize.height, null)
			} finally {
				g2.dispose()
			}
		}
		
		private fun getAvailableDisplayWidth(): Int {
			val parentWidth = parent?.width?.takeIf { it > 0 } ?: maxDisplayWidth
			val parentInsets = parent?.insets
			val parentContentWidth = if (parentInsets == null) {
				parentWidth
			} else {
				parentWidth - parentInsets.left - parentInsets.right
			}
			val insets = insets
			return (parentContentWidth - insets.left - insets.right)
				.coerceAtMost(maxDisplayWidth)
				.coerceAtLeast(1)
		}
		
		private fun calculateDisplaySize(availableWidth: Int): Dimension {
			val targetWidth = availableWidth.coerceAtMost(maxDisplayWidth).coerceAtLeast(1)
			val targetHeight = maxDisplayHeight.coerceAtLeast(1)
			val scale = minOf(
				1.0,
				targetWidth.toDouble() / image.width.toDouble(),
				targetHeight.toDouble() / image.height.toDouble()
			)
			return Dimension(
				(image.width * scale).toInt().coerceAtLeast(1),
				(image.height * scale).toInt().coerceAtLeast(1)
			)
		}
	}
	
	private class LessonContentPanel : JPanel(), Scrollable {
		override fun getPreferredScrollableViewportSize(): Dimension {
			return preferredSize
		}
		
		override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
			return UIScale.scale(16)
		}
		
		override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
			return (visibleRect.height - UIScale.scale(16)).coerceAtLeast(UIScale.scale(16))
		}
		
		override fun getScrollableTracksViewportWidth(): Boolean {
			return true
		}
		
		override fun getScrollableTracksViewportHeight(): Boolean {
			return false
		}
	}
	
	private class WrappingTextPane(private val maxWrapWidth: Int? = null) : JTextPane() {
		override fun getPreferredSize(): Dimension {
			val parentPanel = parent
			val parentWidth = parentPanel?.width?.takeIf { it > 0 }
			val availableWidth = if (parentPanel != null && parentWidth != null) {
				val parentInsets = parentPanel.insets
				(parentWidth - parentInsets.left - parentInsets.right).coerceAtLeast(1)
			} else {
				maxWrapWidth
			}
			
			if (availableWidth != null) {
				val width = maxWrapWidth?.let { availableWidth.coerceAtMost(it) } ?: availableWidth
				val oldSize = size
				setSize(width, Short.MAX_VALUE.toInt())
				val preferred = super.getPreferredSize()
				size = oldSize
				return Dimension(width, preferred.height)
			}
			
			return super.getPreferredSize()
		}
		
		override fun getMaximumSize(): Dimension {
			val preferred = preferredSize
			return Dimension(Int.MAX_VALUE, preferred.height)
		}
	}
}
