package net.abdulahad.action_desk.engine.adcd

data class DialogSpec(
	val title: String,
	val icon: String? = null,
	val message: String? = null,
	val width: String = "auto",
	val height: String = "auto",
	val padding: DialogPadding = DialogPadding.zero(),
	val minWidth: Int = 240,
	val maxWidth: Int = 900,
	val minHeight: String? = null,
	val maxHeight: String = "80vh",
	val position: String = "center",
	val dismissible: Boolean = true,
	val alwaysOnTop: Boolean = false,
	val showInTaskbar: Boolean = true,
	val awaitResult: Boolean = true,
	val sound: DialogSoundSpec? = null,
	val timeoutMs: Int? = null,
	val layout: DialogLayoutSpec = DialogLayoutSpec(),
	val buttons: DialogButtonsSpec = DialogButtonsSpec(),
	val fields: List<DialogFieldRowSpec> = emptyList()
)

data class DialogSoundSpec(
	val src: String
)

data class DialogLayoutSpec(
	val direction: String = "column",
	val gap: Int = 0,
	val padding: DialogPadding = DialogPadding.zero(),
	val alignItems: String = "start"
)

data class DialogFieldRowSpec(
	val margin: DialogPadding = DialogPadding.zero(),
	val layout: DialogLayoutSpec = DialogLayoutSpec(direction = "row"),
	val items: List<DialogFieldSpec> = emptyList()
)

data class DialogFieldSpec(
	val type: String = "input",
	val name: String? = null,
	val label: String? = null,
	val labelPosition: String = "top",
	val value: String? = null,
	val src: String? = null,
	val width: Int? = null,
	val height: Int? = null,
	val placeholder: String? = null,
	val checked: Boolean = false,
	val required: Boolean = false,
	val dataType: String = "string",
	val minLen: Int? = null,
	val maxLen: Int? = null,
	val min: Double? = null,
	val max: Double? = null,
	val allowedValues: List<String> = emptyList(),
	val caseSensitive: Boolean = false,
	val options: List<DialogOptionSpec> = emptyList()
)

data class DialogOptionSpec(
	val value: String,
	val label: String
) {
	override fun toString(): String {
		return label
	}
}

data class DialogButtonsSpec(
	val visible: Boolean = true,
	val model: String? = null,
	val layout: String = "right",
	val gap: Int = 8,
	val padding: DialogPadding = DialogPadding(10, 10, 10, 10),
	val showBorder: Boolean = true,
	val confirm: DialogButtonSpec = DialogButtonSpec(
		id = "ok",
		label = "OK",
		role = "submit",
		defaultButton = true
	),
	val cancel: DialogButtonSpec = DialogButtonSpec(
		id = "cancel",
		label = "Cancel",
		role = "cancel"
	),
	val acknowledge: DialogButtonSpec = DialogButtonSpec(
		id = "ok",
		label = "OK",
		role = "neutral",
		defaultButton = true
	),
	val items: List<DialogButtonSpec> = emptyList()
)

data class DialogButtonSpec(
	val id: String,
	val label: String,
	val role: String = "neutral",
	val defaultButton: Boolean = false,
	val color: String? = null,
	val background: String? = null,
	val padding: DialogPadding? = null,
	val fontSize: Float? = null,
	val borderRadius: Int? = null
)

data class DialogResult(
	val button: String,
	val values: Map<String, Any?> = emptyMap()
)

data class DialogValidationError(
	val fieldName: String,
	val message: String
)

data class DialogPadding(
	val top: Int = 0,
	val right: Int = 0,
	val bottom: Int = 0,
	val left: Int = 0
) {
	
	operator fun plus(other: DialogPadding): DialogPadding {
		return DialogPadding(
			top = top + other.top,
			right = right + other.right,
			bottom = bottom + other.bottom,
			left = left + other.left
		)
	}
	
	companion object {
		fun zero(): DialogPadding {
			return DialogPadding()
		}
		
		fun all(value: Int): DialogPadding {
			return DialogPadding(value, value, value, value)
		}
	}
	
}
