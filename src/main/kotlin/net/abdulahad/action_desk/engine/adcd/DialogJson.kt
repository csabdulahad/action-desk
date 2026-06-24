package net.abdulahad.action_desk.engine.adcd

import net.abdulahad.action_desk.lib.json.Json
import net.abdulahad.action_desk.lib.json.JsonValue

object DialogJson {
	
	fun parseDialogSpec(json: String): DialogSpec {
		val root = try {
			Json.parseObject(json)
		} catch (e: Exception) {
			throw IllegalArgumentException("Invalid JSON: ${e.message}")
		}
		
		return DialogSpec(
			title = root.path("title").string("Action Desk") ?: "Action Desk",
			icon = root.path("icon").string(),
			message = root.path("message").string(),
			width = DialogStyleParser.dimension(root.path("width"), "auto"),
			height = DialogStyleParser.dimension(root.path("height"), "auto"),
			padding = DialogStyleParser.padding(root.path("padding"), DialogPadding.zero()),
			minWidth = root.path("min_width").int(240),
			maxWidth = root.path("max_width").int(900),
			minHeight = DialogStyleParser.nullableDimension(root.path("min_height")),
			maxHeight = DialogStyleParser.dimension(root.path("max_height"), "80vh"),
			position = root.path("position").string("center") ?: "center",
			dismissible = root.path("dismissible").bool(true),
			alwaysOnTop = root.path("always_on_top").bool(false),
			showInTaskbar = root.path("show_in_taskbar").bool(true),
			awaitResult = root.path("await_result").bool(true),
			sound = parseSound(root.path("sound")),
			timeoutMs = root.path("timeout_ms").intOrNull()?.coerceAtLeast(0),
			layout = parseLayout(root.path("layout")),
			buttons = parseButtons(root.path("buttons")),
			fields = parseFields(root.path("fields"))
		)
	}
	
	fun resultToJson(result: DialogResult): String {
		return Json.stringify(result)
	}
	
	fun errorToJson(error: String): String {
		return Json.stringify(
			mapOf(
				"ok" to false,
				"error" to error
			)
		)
	}
	
	fun dialogDisabledToJson(): String {
		return Json.stringify(
			mapOf(
				"status" to "dialog_disabled",
				"button" to "disabled",
				"values" to emptyMap<String, Any?>()
			)
		)
	}
	
	private fun parseLayout(
		node: JsonValue,
		defaultValue: DialogLayoutSpec = DialogLayoutSpec()
	): DialogLayoutSpec {
		if (!node.isMap) return defaultValue
		
		return DialogLayoutSpec(
			direction = (node.path("direction").string(defaultValue.direction) ?: defaultValue.direction).lowercase(),
			gap = node.path("gap").int(defaultValue.gap),
			padding = DialogStyleParser.padding(node.path("padding"), defaultValue.padding),
			alignItems = (node.path("align_items").string(defaultValue.alignItems) ?: defaultValue.alignItems).lowercase()
		)
	}
	
	private fun parseFields(node: JsonValue): List<DialogFieldRowSpec> {
		if (!node.isList) return emptyList()
		
		return node.list().mapNotNull { rowNode ->
			parseFieldRow(rowNode)
		}
	}
	
	private fun parseFieldRow(node: JsonValue): DialogFieldRowSpec? {
		if (!node.isMap) return null
		
		val map = node.map()
		val itemsNode = node.path("items")
		
		if (map.containsKey("items")) {
			if (!itemsNode.isList) {
				return null
			}
			
			val items = itemsNode.list().mapNotNull { itemNode ->
				parseField(itemNode)
			}
			
			if (items.isEmpty()) {
				return null
			}
			
			return DialogFieldRowSpec(
				margin = DialogStyleParser.padding(node.path("margin"), DialogPadding.zero()),
				layout = parseLayout(
					node.path("layout"),
					DialogLayoutSpec(direction = "row")
				),
				items = items
			)
		}
		
		if (!isDirectFieldNode(map)) {
			return null
		}
		
		val field = parseField(node) ?: return null
		
		return DialogFieldRowSpec(
			margin = DialogStyleParser.padding(node.path("margin"), DialogPadding.zero()),
			items = listOf(field)
		)
	}
	
	private fun isDirectFieldNode(map: Map<String, JsonValue>): Boolean {
		return directFieldKeys.any { key -> map.containsKey(key) }
	}
	
	private val directFieldKeys = setOf(
		"type",
		"name",
		"label",
		"label_position",
		"value",
		"src",
		"width",
		"height",
		"placeholder",
		"checked",
		"required",
		"data_type",
		"min_len",
		"max_len",
		"min",
		"max",
		"allowed_values",
		"case_sensitive",
		"options"
	)
	
	private fun parseField(node: JsonValue): DialogFieldSpec? {
		if (!node.isMap) return null
		
		return DialogFieldSpec(
			type = (node.path("type").string("input") ?: "input").lowercase(),
			name = node.path("name").string(),
			label = node.path("label").string(),
			labelPosition = (node.path("label_position").string("top") ?: "top").lowercase(),
			value = node.path("value").string(),
			src = node.path("src").string(),
			width = node.path("width").intOrNull()?.coerceAtLeast(1),
			height = node.path("height").intOrNull()?.coerceAtLeast(1),
			placeholder = node.path("placeholder").string(),
			checked = node.path("checked").bool(
				node.path("value").bool(false)
			),
			required = node.path("required").bool(false),
			dataType = (node.path("data_type").string("string") ?: "string").lowercase(),
			minLen = node.path("min_len").intOrNull(),
			maxLen = node.path("max_len").intOrNull(),
			min = node.path("min").doubleOrNull(),
			max = node.path("max").doubleOrNull(),
			allowedValues = node.path("allowed_values").stringList(),
			caseSensitive = node.path("case_sensitive").bool(false),
			options = parseOptions(node.path("options"))
		)
	}
	
	private fun parseOptions(node: JsonValue): List<DialogOptionSpec> {
		if (!node.isList) return emptyList()
		
		return node.list().mapNotNull { optionNode ->
			when (optionNode.raw()) {
				is String -> {
					val value = optionNode.string("") ?: ""
					DialogOptionSpec(value, value)
				}
				
				is Map<*, *> -> {
					val value = optionNode.path("value").string("") ?: ""
					val label = optionNode.path("label").string(value) ?: value
					
					if (value.isBlank()) null else DialogOptionSpec(value, label)
				}
				
				else -> null
			}
		}
	}
	
	private fun parseButtons(node: JsonValue): DialogButtonsSpec {
		if (!node.isMap) {
			return DialogButtonsSpec()
		}
		
		return DialogButtonsSpec(
			visible = node.path("visible").bool(true),
			model = node.path("model").string()?.lowercase(),
			layout = (node.path("layout").string("right") ?: "right").lowercase(),
			gap = node.path("gap").int(8),
			padding = DialogStyleParser.padding(node.path("padding"), DialogPadding(10, 10, 10, 10)),
			showBorder = node.path("show_border").bool(true),
			confirm = parseButtonConfig(
				node.path("confirm"),
				DialogButtonSpec(
					id = "ok",
					label = "OK",
					role = "submit",
					defaultButton = true
				)
			),
			cancel = parseButtonConfig(
				node.path("cancel"),
				DialogButtonSpec(
					id = "cancel",
					label = "Cancel",
					role = "cancel"
				)
			),
			acknowledge = parseButtonConfig(
				node.path("acknowledge"),
				DialogButtonSpec(
					id = "ok",
					label = "OK",
					role = "neutral",
					defaultButton = true
				)
			),
			items = parseButtonItems(node.path("items"))
		)
	}
	
	private fun parseButtonItems(node: JsonValue): List<DialogButtonSpec> {
		if (!node.isList) {
			return emptyList()
		}
		
		return node.list().mapNotNull { buttonNode ->
			parseCustomButton(buttonNode)
		}
	}
	
	private fun parseCustomButton(node: JsonValue): DialogButtonSpec? {
		if (!node.isMap) {
			return null
		}
		
		val id = node.path("id").string("") ?: ""
		
		if (id.isBlank()) {
			return null
		}
		
		return parseButtonConfig(
			node,
			DialogButtonSpec(
				id = id,
				label = id,
				role = "neutral"
			)
		)
	}
	
	private fun parseButtonConfig(
		node: JsonValue,
		defaultValue: DialogButtonSpec
	): DialogButtonSpec {
		if (!node.isMap) {
			return defaultValue
		}
		
		val labelDefault = node.path("text").string(defaultValue.label) ?: defaultValue.label
		
		return DialogButtonSpec(
			id = node.path("id").string(defaultValue.id) ?: defaultValue.id,
			label = node.path("label").string(labelDefault) ?: labelDefault,
			role = (node.path("role").string(defaultValue.role) ?: defaultValue.role).lowercase(),
			defaultButton = node.path("default").bool(defaultValue.defaultButton),
			color = node.path("color").string(),
			background = node.path("background").string(),
			padding = DialogStyleParser.nullablePadding(node.path("padding")),
			fontSize = node.path("font_size").floatOrNull(),
			borderRadius = node.path("border_radius").intOrNull()?.coerceAtLeast(0)
		)
	}
	
	private fun parseSound(node: JsonValue): DialogSoundSpec? {
		return when (val raw = node.raw()) {
			null -> null
			
			is Boolean -> {
				if (raw) DialogSoundSpec("default") else null
			}
			
			is Map<*, *> -> {
				val src = node.path("src").string()?.trim() ?: return null
				if (isNoSoundValue(src)) null else DialogSoundSpec(src)
			}
			
			else -> {
				val src = node.string()?.trim() ?: return null
				if (isNoSoundValue(src)) null else DialogSoundSpec(src)
			}
		}
	}
	
	private fun isNoSoundValue(value: String): Boolean {
		return value.isBlank() || value.lowercase() in setOf(
			"none",
			"off",
			"no",
			"false",
			"0",
			"null"
		)
	}
	
}
