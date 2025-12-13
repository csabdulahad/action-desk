package net.abdulahad.action_desk

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.icons.FlatWindowCloseIcon
import com.formdev.flatlaf.icons.FlatWindowIconifyIcon
import com.formdev.flatlaf.util.UIScale
import net.abdulahad.action_desk.config.ConfigKeys
import net.abdulahad.action_desk.config.ConfigService
import net.abdulahad.action_desk.model.ThemeDescriptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Font
import java.awt.Frame
import java.io.InputStream
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource
import kotlin.system.exitProcess

object App {
	
	const val DEBUG = true
	
	const val CODE_NAME: String = "action_desk"
	const val NAME: String = "Action Desk"
	const val VERSION: String = "1.0"
	const val DEVELOPER: String = "Abdul Ahad"
	const val DEVELOPER_LINK: String = "https://abdulahad.net"

	val logger: Logger by lazy {
		LoggerFactory.getLogger("AppLogger")
	}
	
	private lateinit var flatThemes: List<ThemeDescriptor>
	
	private lateinit var fontBold: Font
	val FONT_BOLD: Font
		get() = fontBold
	
	fun getName(separator: String = " "): String {
		return "$NAME$separator$VERSION"
	}
	
	fun logInfo(msg: String?) {
		if (DEBUG) println(msg)
		logger.info(msg)
	}
	
	fun logWarn(msg: String?) {
		if (DEBUG) println(msg)
		logger.warn(msg)
	}
	
	fun logErr(msg: String?) {
		if (DEBUG) println(msg)
		logger.error(msg)
	}
	
	fun logTagged(tag: String?, msg: String?) {
		val x = buildString {
			if (!tag.isNullOrBlank()) append("[$tag] ") else append("[TAG] ")
			if (!msg.isNullOrBlank()) append(msg)
		}
		
		if (DEBUG) println(x)
		
		logger.info(x)
	}
	
	fun debug(msg: String?) {
		if (DEBUG) println(msg)
	}
	
	fun setup() {
		FlatLaf.registerCustomDefaultsSource( "themes")
		applyThemeConfig()
		loadLookAndFeel()
	}
	
	fun findFrameByName(name: String): JFrame? {
		for (frame in Frame.getFrames()) {
			if (frame is JFrame) {
				if (name == frame.getName()) {
					return frame
				}
			}
		}
		
		return null
	}
	
	fun listenThemeChange(callback: () -> Unit) {
		UIManager.addPropertyChangeListener { evt ->
			if (evt.propertyName == "lookAndFeel") {
				SwingUtilities.invokeLater(callback)
			}
		}
	}
	
	fun applyCloseIcon() {
		UIManager.put("TitlePane.closeIcon", FlatWindowCloseIcon())
	}
	
	fun applyIconifyIcon() {
		UIManager.put("TitlePane.closeIcon", FlatWindowIconifyIcon())
	}
	
	private fun loadFont(size: Float): Font {
		val fontName = "inter_medium.ttf"
		val stream: InputStream? = javaClass.getResourceAsStream("/fonts/$fontName")
		
		return try {
			val baseFont = Font.createFont(Font.PLAIN, stream)
			baseFont.deriveFont(size)
		} catch (e: Exception) {
			e.printStackTrace()
			Font("SansSerif", Font.PLAIN, size.toInt())
		} finally {
			stream?.close()
		}
	}
	
	private fun setGlobalFont(font: Font) {
		val fontRes = FontUIResource(font)
		
		listOf(
			"TableHeader.font", "TextPane.font", "TitledBorder.font", "Menu.acceleratorFont", "PasswordField.font",
			"MenuBar.font", "Table.font", "ToolBar.font", "Panel.font", "OptionPane.font", "InternalFrame.titleFont",
			"RadioButtonMenuItem.font", "List.font", "PopupMenu.font", "CheckBox.font", "TextArea.font",
			"ToggleButton.font", "MenuItem.acceleratorFont", "Slider.font", "MenuItem.font", "ProgressBar.font",
			"TabbedPane.font", "RadioButton.font", "TextField.font", "Tree.font", "RadioButtonMenuItem.acceleratorFont",
			"EditorPane.font", "CheckBoxMenuItem.acceleratorFont", "DesktopIcon.font", "Button.font", "Label.font",
			"ColorChooser.font", "ToolTip.font", "ComboBox.font", "CheckBoxMenuItem.font", "ScrollPane.font", "Spinner.font",
			"FormattedTextField.font", "Menu.font", "Viewport.font"
		).forEach { type ->
			UIManager.put(type, fontRes)
		}
	}
	
	private fun loadLookAndFeel() {
		val themeName = ConfigService.getString(ConfigKeys.THEME, "dark")
		
		try {
			val theme = ThemeDescriptor.getByThemeName(themeName).createInstance()
			UIManager.setLookAndFeel(theme)
		} catch (e: Exception) {
			val msg = "Failed to load look and feel: $themeName\nCause: ${e.message}"
			logErr(msg)
			runtimeException(msg)
		}
	}
	
	private fun applyThemeConfig() {
		try {
			val sfFont: Font = loadFont(UIScale.scale(14f))
			setGlobalFont(sfFont)
		} catch (e: Exception) {
			val msg = "Failed to apply theme configuration\nCause: ${e.message}"
			logErr(msg)
			runtimeException(msg)
		}
	}
	
	fun getPSBin(): String {
		return ConfigService.getString(ConfigKeys.COMMAND_LINE, "powershell")
	}
	
	fun getSearchFocus(): Boolean {
		return ConfigService.getBool(ConfigKeys.FOCUS_SEARCH, true)
	}

	fun getActionDeskPin(): Boolean {
		return ConfigService.getBool(ConfigKeys.ALWAYS_ON_TOP, true)
	}
	
	fun getHideAfterAction(): Boolean {
		return ConfigService.getBool(ConfigKeys.HIDE_AFTER_ACTION, true)
	}
	
	fun getStartMinimized(): Boolean {
		return ConfigService.getBool(ConfigKeys.START_MINIMIZED, false)
	}
	
	fun getAutoRestart(): Boolean {
		return ConfigService.getBool(ConfigKeys.AUTOSTART, false)
	}
	
	fun close() {
		onClose()
		exitProcess(0)
	}
	
	fun onClose() {
	
	}
	
}
	