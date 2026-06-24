 package net.abdulahad.action_desk.engine.adcd

import java.awt.Font
import java.awt.GraphicsEnvironment
import javax.swing.JComponent
import javax.swing.UIManager

/**
 * ADCD dialogs can receive arbitrary user text from local scripts.
 *
 * FlatLaf/Swing normally uses the current LookAndFeel font. On Windows that
 * font may not contain Bangla or emoji glyphs, which causes square boxes in
 * message/title/field text. This helper keeps the normal UI font when it can
 * display the text and falls back to Java/platform fonts when it cannot.
 */
object DialogFont {
	
	private val fallbackFamilies = listOf(
		Font.DIALOG,
		"Nirmala UI",
		"Vrinda",
		"Segoe UI Emoji",
		"Segoe UI Symbol",
		Font.SANS_SERIF
	)
	
	private val logicalFamilies = setOf(
		Font.DIALOG,
		Font.DIALOG_INPUT,
		Font.SANS_SERIF,
		Font.SERIF,
		Font.MONOSPACED
	)
	
	private val installedFamilies: Set<String> by lazy {
		GraphicsEnvironment
			.getLocalGraphicsEnvironment()
			.availableFontFamilyNames
			.map { it.lowercase() }
			.toSet()
	}
	
	fun apply(component: JComponent, vararg texts: String?) {
		val text = texts
			.filterNotNull()
			.joinToString(" ")
		
		component.font = bestFontFor(text, component.font)
	}
	
	fun bestFontFor(text: String?, baseFont: Font?): Font {
		val base = baseFont
			?: UIManager.getFont("Label.font")
			?: Font(Font.DIALOG, Font.PLAIN, 12)
		
		if (text.isNullOrBlank()) {
			return base
		}
		
		if (canDisplay(base, text)) {
			return base
		}
		
		fallbackFamilies.forEach { family ->
			if (!isUsableFamily(family)) {
				return@forEach
			}
			
			val candidate = Font(family, base.style, base.size).deriveFont(base.size2D)
			
			if (canDisplay(candidate, text)) {
				return candidate
			}
		}
		
		/*
		 * Final fallback: even if canDisplayUpTo() is not happy with a mixed
		 * string such as Bangla + emoji, Java's logical Dialog font still gives
		 * the text pipeline the best chance to use platform fallback fonts.
		 */
		return Font(Font.DIALOG, base.style, base.size).deriveFont(base.size2D)
	}
	
	private fun canDisplay(font: Font, text: String): Boolean {
		return font.canDisplayUpTo(text) == -1
	}
	
	private fun isUsableFamily(family: String): Boolean {
		return logicalFamilies.contains(family) || installedFamilies.contains(family.lowercase())
	}
	
}
