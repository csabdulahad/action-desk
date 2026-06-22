package net.abdulahad.action_desk.helper

import com.formdev.flatlaf.extras.FlatSVGIcon
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.lib.util.Poth
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

object Icons {
	
	private val JUI_ICON_EXTENSIONS = setOf("svg", "png", "jpg", "jpeg")
	
	const val ACTION_DESK = "actionDesk"
	const val ADD = "add"
	const val ANY_TYPE = "anyType"
	const val APP_CODE_PROJECT = "appCodeProject"
	const val BINARY_DATA = "binaryData"
	const val BLUE_KEY = "blueKey"
	const val BODY = "body"
	const val BOOKMARK = "bookmark"
	const val BREAKPOINT_FIELD_UNSUSPENDENT = "breakpointFieldUnsuspendent"
	const val BUILD = "build"
	const val CHANGE_LIST = "changelist"
	const val CHECKED = "checked"
	const val CLEAR_CASH = "clearCash"
	const val CLOSE = "close"
	const val CMAKE_TARGET_EXECUTABLE = "CMakeTargetExecutable"
	const val COLLECTION = "collection"
	const val COLLECTION_KEY = "collectionKey"
	const val COMMENT = "comment"
	const val COMMIT = "commit"
	const val COMPOSE_PROJECT_STOPPED = "composeProjectStopped"
	const val CONSOLE_RUN = "consoleRun"
	const val COPY = "copy"
	const val COPY_OF_FOLDER = "copyOfFolder"
	const val COVERAGE = "coverage"
	const val CUSTOM_GDB_RUN_CONFIGURATION = "customGdbRunConfiguration"
	const val CUT = "cut"
	const val CWM_ENABLE_CALL = "cwmEnableCall"
	const val CWM_SCREEN_ON = "cwmScreenOn"
	const val CWM_SHARE = "cwmShare"
	const val DARK_THEME = "darkTheme"
	const val DEBUG = "debug"
	const val DELETE = "delete"
	const val DIFF = "diff"
	const val DOWNLOAD = "download"
	const val EDIT = "edit"
	const val EMPTY = "empty"
	const val ERROR = "error"
	const val EVALUATE_EXPRESSION = "evaluateExpression"
	const val EXPORT = "export"
	const val EXT_ANNOTATION = "extAnnotation"
	const val FEEDBACK_RATING_ON = "feedbackRatingOn"
	const val FILTER = "filter"
	const val FIND = "find"
	const val FOLDER = "folder"
	const val FRAMEWORK = "framework"
	const val GRID = "grid"
	const val GROUPS = "groups"
	const val HISTORY = "history"
	const val HOME_FOLDER = "homeFolder"
	const val ID = "id"
	const val INFO = "info"
	const val INFO_OUTLINE = "infoOutline"
	const val INSPECTIONS_OK = "inspectionsOK"
	const val INSPECTIONS_OK_EMPTY = "inspectionsOKEmpty"
	const val INSPECTIONS_POWER_SAVE_MODE = "inspectionsPowerSaveMode"
	const val INSPECTIONS_TRAFFIC_OFF = "inspectionsTrafficOff"
	const val INSTALL = "install"
	const val INTENTION_BULB = "intentionBulb"
	const val JAVA_SCRIPT_DEBUG_CONFIGURATION = "javaScriptDebugConfiguration"
	const val KEYBOARD = "keyboard"
	const val LEARN = "learn"
	const val LIBRARY = "library"
	const val LIGHT_THEME = "lightTheme"
	const val LIGHTNING = "lightning"
	const val LOCATION = "location"
	const val LOCKED = "locked"
	const val MAGIC_RESOLVE_TOOLBAR = "magicResolveToolbar"
	const val MINIMIZE = "minimize"
	const val MONGO_FIELD = "mongoField"
	const val MORE_HORIZONTAL = "moreHorizontal"
	const val MORE_VERTICAL = "moreVertical"
	const val NOTIFICATIONS = "notifications"
	const val OPEN = "open"
	const val OPEN_IN_NEW_WINDOW = "openInNewWindow"
	const val PASTE = "paste"
	const val PHP = "php"
	const val PIN = "pin"
	const val PIN_SELECTED = "pinSelected"
	const val PLUGIN = "plugin"
	const val PREVIEW_ONLY = "previewOnly"
	const val PRINT = "print"
	const val PROFILE_CPU = "profileCPU"
	const val PROJECT_WIDE_ANALYSIS_OFF = "projectWideAnalysisOff"
	const val PYTHON_CONSOLE = "pythonConsole"
	const val READER_MODE = "readerMode"
	const val REFRESH = "refresh"
	const val RELAY = "relay"
	const val REPORT = "report"
	const val RESET = "reset"
	const val RUN = "run"
	const val RUN2 = "run2"
	const val SAVE = "save"
	const val SEARCH = "search"
	const val SEPARATOR_HORIZONTAL = "separatorHorizontal"
	const val SERVER = "server"
	const val SERVICES = "services"
	const val SETTINGS = "settings"
	const val SHIELD = "shield"
	const val SHOW_IGNORED = "showIgnored"
	const val SHOW_PASSED = "showPassed"
	const val SINGLE_STOPPED_CONTAINER = "singleStoppedContainer"
	const val SPARK = "spark"
	const val STRUCTURE = "structure"
	const val SUBSCRIPTION = "subscription"
	const val SUCCESS = "success"
	const val SYSTEM_THEME = "systemTheme"
	const val TARGET = "target"
	const val TEST_CUSTOM = "testCustom"
	const val TEST_UNKNOWN = "testUnknown"
	const val TOOL_WINDOW_VARIABLE_VIEW = "toolWindowVariableView"
	const val TOGGLE_VISIBILITY = "toggleVisibility"
	const val TRIGGER = "trigger"
	const val UNLOCKED = "unlocked"
	const val UPDATE = "update"
	const val UPDATE_RUNNING_APPLICATION = "updateRunningApplication"
	const val USER = "user"
	const val USER_GROUP = "userGroup"
	const val VOLUME = "volume"
	const val WARNING_INTRODUCTION = "warningIntroduction"
	const val WATCH = "watch"
	const val WARN = "warn"
	const val WINDOWS = "microsoftWindows"
	const val WEB = "web"
	const val XHTML = "xhtml"
	const val XML = "xml"
	
	private fun resizeIcon(icon: ImageIcon, width: Int = 22, height: Int = 22): ImageIcon {
		val scaledImage = icon.image.getScaledInstance(width, height, Image.SCALE_SMOOTH)
		return ImageIcon(scaledImage)
	}
	
	fun String.icon(size: Int = 20): Icon {
		// #1 svg - resource
		val svgResource = "icons/svg/$this.svg"
		if (Poth.resourceExists(svgResource)) {
			return FlatSVGIcon(svgResource, size, size)
		}
		
		// #2 png - resource
		val pngResource = "icons/png/$this.png"
		if (Poth.resourceExists(pngResource)) {
			val icon = ImageIcon(pngResource)
			return resizeIcon(icon, size, size)
		}
		
		// #3 svg - app folder
		val customSVG = "${Env.APP_FOLDER}/icons/$this.svg"
		if (Poth.fileExists(customSVG)) {
			val icon = FlatSVGIcon(File(customSVG).toURI().toURL())
			val base = icon.iconWidth.toFloat() // intrinsic size
			
			return icon.derive(size / base)
		}
		
		// #4 png - app folder
		val customPNG = "${Env.APP_FOLDER}/icons/$this.png"
		if (Poth.fileExists(customPNG)) {
			val img = ImageIO.read(File(customPNG))
			return HiDpiPngIcon(img, size)
		}
		
		return FlatSVGIcon("icons/svg/empty.svg", size, size)
	}
	
	class ContainedIcon(
		private val icon: Icon,
		private val width: Int,
		private val height: Int
	) : Icon {
		
		override fun getIconWidth() = width
		override fun getIconHeight() = height
		
		override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
			val sourceWidth = icon.iconWidth.takeIf { it > 0 } ?: width
			val sourceHeight = icon.iconHeight.takeIf { it > 0 } ?: height
			val scale = minOf(
				width.toDouble() / sourceWidth.toDouble(),
				height.toDouble() / sourceHeight.toDouble()
			)
			val drawWidth = (sourceWidth * scale).toInt().coerceAtLeast(1)
			val drawHeight = (sourceHeight * scale).toInt().coerceAtLeast(1)
			val drawX = x + ((width - drawWidth) / 2)
			val drawY = y + ((height - drawHeight) / 2)
			val g2 = g.create() as Graphics2D
			
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			g2.translate(drawX, drawY)
			g2.scale(scale, scale)
			icon.paintIcon(c, g2, 0, 0)
			g2.dispose()
		}
	}
	
	class HiDpiPngIcon(
		private val image: BufferedImage,
		private val size: Int
	) : Icon {
		
		override fun getIconWidth() = size
		override fun getIconHeight() = size
		
		override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
			val g2 = g.create() as Graphics2D
			
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			
			g2.drawImage(image, x, y, size, size, null)
			g2.dispose()
		}
	}
	
	fun Icon.toImageIcon(): ImageIcon {
		return when (this) {
			is ImageIcon -> this
			else -> {
				val w = iconWidth
				val h = iconHeight
				val img = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
				val g = img.createGraphics()
				paintIcon(null, g, 0, 0)
				g.dispose()
				ImageIcon(img)
			}
		}
	}
	
	fun String.dialogIcon(size: Int = 32): Icon? {
		val ref = trim()
		
		if (ref.isBlank()) {
			return null
		}
		
		val file = File(ref)
		
		/*
		 * Absolute path:
		 * Use exactly what caller provided.
		 */
		if (file.isAbsolute) {
			return loadJuiIconFile(file, size)
		}
		
		val cleanRef = ref
			.replace("\\", "/")
			.trimStart('/')
		
		/*
		 * Relative path with extension:
		 * First try app-folder icons.
		 * Then try bundled resources.
		 *
		 * Examples:
		 *   "custom/ex.svg"
		 *   "ex.svg"
		 *   "icons/svg/run.svg"
		 */
		if (hasJuiIconExtension(cleanRef)) {
			loadJuiIconFile(File("${Env.APP_FOLDER}/icons/$cleanRef"), size)?.let {
				return it
			}
			
			loadJuiResourceIcon(cleanRef, size)?.let {
				return it
			}
			
			loadJuiResourceIcon("icons/$cleanRef", size)?.let {
				return it
			}
			
			return null
		}
		
		/*
		 * Plain icon name:
		 * Reuse existing Action Desk icon resolver.
		 *
		 * Examples:
		 *   "run"
		 *   "settings"
		 *   "server"
		 */
		return icon(size)
	}
	
	fun String.dialogImageIcon(width: Int = 64, height: Int = 64): Icon? {
		val ref = trim()
		
		if (ref.isBlank()) {
			return null
		}
		
		val safeWidth = width.coerceAtLeast(1)
		val safeHeight = height.coerceAtLeast(1)
		val file = File(ref)
		
		/*
		 * Absolute path:
		 * Use exactly what caller provided.
		 */
		if (file.isAbsolute) {
			return loadJuiImageFile(file, safeWidth, safeHeight)
		}
		
		val cleanRef = ref
			.replace("\\", "/")
			.trimStart('/')
		
		/*
		 * Relative path with extension:
		 * First try app-folder icons.
		 * Then try bundled resources.
		 */
		if (hasJuiIconExtension(cleanRef)) {
			loadJuiImageFile(File("${Env.APP_FOLDER}/icons/$cleanRef"), safeWidth, safeHeight)?.let {
				return it
			}
			
			loadJuiResourceImage(cleanRef, safeWidth, safeHeight)?.let {
				return it
			}
			
			loadJuiResourceImage("icons/$cleanRef", safeWidth, safeHeight)?.let {
				return it
			}
			
			return null
		}
		
		/*
		 * Plain image/icon name:
		 * Try the app icons folder and bundled icons using the same name-without-
		 * extension idea as JUI icon support, but include jpg/jpeg too.
		 */
		JUI_ICON_EXTENSIONS.forEach { ext ->
			loadJuiImageFile(File("${Env.APP_FOLDER}/icons/$cleanRef.$ext"), safeWidth, safeHeight)?.let {
				return it
			}
		}
		
		loadJuiResourceImage("icons/svg/$cleanRef.svg", safeWidth, safeHeight)?.let {
			return it
		}
		
		loadJuiResourceImage("icons/png/$cleanRef.png", safeWidth, safeHeight)?.let {
			return it
		}
		
		JUI_ICON_EXTENSIONS.forEach { ext ->
			loadJuiResourceImage("icons/$cleanRef.$ext", safeWidth, safeHeight)?.let {
				return it
			}
		}
		
		return null
	}
	
	private fun loadJuiIconFile(file: File, size: Int): Icon? {
		if (!file.exists() || !file.isFile) {
			return null
		}
		
		val ext = file.extension.lowercase()
		
		if (ext !in JUI_ICON_EXTENSIONS) {
			return null
		}
		
		return try {
			when (ext) {
				"svg" -> {
					val icon = FlatSVGIcon(file.toURI().toURL())
					val base = icon.iconWidth
						.takeIf { it > 0 }
						?: size
					
					icon.derive(size.toFloat() / base.toFloat())
				}
				
				else -> {
					val image = ImageIO.read(file) ?: return null
					HiDpiPngIcon(image, size)
				}
			}
		} catch (_: Exception) {
			null
		}
	}
	
	private fun loadJuiResourceIcon(resourcePath: String, size: Int): Icon? {
		if (!Poth.resourceExists(resourcePath)) {
			return null
		}
		
		val ext = resourcePath
			.substringAfterLast('.', "")
			.lowercase()
		
		if (ext !in JUI_ICON_EXTENSIONS) {
			return null
		}
		
		return try {
			when (ext) {
				"svg" -> FlatSVGIcon(resourcePath, size, size)
				
				else -> {
					val image = Poth.getAsStream(resourcePath)?.use {
						ImageIO.read(it)
					} ?: return null
					
					HiDpiPngIcon(image, size)
				}
			}
		} catch (_: Exception) {
			null
		}
	}
	
	private fun loadJuiImageFile(file: File, width: Int, height: Int): Icon? {
		if (!file.exists() || !file.isFile) {
			return null
		}
		
		val ext = file.extension.lowercase()
		
		if (ext !in JUI_ICON_EXTENSIONS) {
			return null
		}
		
		return try {
			when (ext) {
				"svg" -> ContainedIcon(
					FlatSVGIcon(file.toURI().toURL()),
					width,
					height
				)
				
				else -> {
					val image = ImageIO.read(file) ?: return null
					ContainedIcon(ImageIcon(image), width, height)
				}
			}
		} catch (_: Exception) {
			null
		}
	}
	
	private fun loadJuiResourceImage(resourcePath: String, width: Int, height: Int): Icon? {
		if (!Poth.resourceExists(resourcePath)) {
			return null
		}
		
		val ext = resourcePath
			.substringAfterLast('.', "")
			.lowercase()
		
		if (ext !in JUI_ICON_EXTENSIONS) {
			return null
		}
		
		return try {
			when (ext) {
				"svg" -> ContainedIcon(
					FlatSVGIcon(Poth.getURL(resourcePath)),
					width,
					height
				)
				
				else -> {
					val image = Poth.getAsStream(resourcePath)?.use {
						ImageIO.read(it)
					} ?: return null
					
					ContainedIcon(ImageIcon(image), width, height)
				}
			}
		} catch (_: Exception) {
			null
		}
	}
	
	private fun hasJuiIconExtension(path: String): Boolean {
		val ext = path
			.substringAfterLast('/', path)
			.substringAfterLast('.', "")
			.lowercase()
		
		return ext in JUI_ICON_EXTENSIONS
	}
	
}