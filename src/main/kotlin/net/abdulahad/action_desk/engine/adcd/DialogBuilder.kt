package net.abdulahad.action_desk.engine.adcd

import com.formdev.flatlaf.extras.components.FlatTextField
import com.formdev.flatlaf.util.UIScale
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.helper.Icons.dialogIcon
import net.abdulahad.action_desk.helper.Icons.dialogImageIcon
import net.abdulahad.action_desk.helper.Icons.toImageIcon
import net.abdulahad.action_desk.lib.view.ColorUtil
import net.abdulahad.action_desk.lib.view.DialogPositioner
import net.abdulahad.action_desk.lib.view.ValidationOutline
import net.abdulahad.action_desk.view.ActionDesk
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.Dimension
import java.awt.Font
import java.awt.Frame
import java.awt.GridLayout
import java.awt.Insets
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.ImageIcon
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JRadioButton
import javax.swing.RootPaneContainer
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.ScrollPaneConstants
import javax.swing.Scrollable
import javax.swing.SwingConstants
import javax.swing.Timer
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

class DialogBuilder(
	private val spec: DialogSpec,
	private val onComplete: (DialogResult) -> Unit
) {
	
	private data class DialogRadioItem(
		val button: JRadioButton,
		val option: DialogOptionSpec
	)
	
	private val namedFields = mutableMapOf<String, JComponent>()
	private val fieldSpecs = mutableMapOf<String, DialogFieldSpec>()
	
	private lateinit var dialog: Window
	private lateinit var rootPaneContainer: RootPaneContainer
	private lateinit var bodyScrollPane: JScrollPane
	
	private var result = DialogResult("dismiss")
	
	private var completed = false
	
	private var timeoutTimer: Timer? = null
	
	fun show() {
		dialog = createWindow()
		rootPaneContainer = dialog as RootPaneContainer
		rootPaneContainer.rootPane.border = null
		
		applyIcon()
		installDismissKeyBinding()
		
		dialog.addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent) {
				if (spec.dismissible) {
					completeDialog(DialogResult("dismiss"))
				}
			}
			
			override fun windowClosed(e: WindowEvent) {
				if (!completed) {
					completeDialog(DialogResult("dismiss"))
				}
			}
		})
		
		val buttonBar = buildButtonBar()
		val widthPlan = resolveWidthPlan(buttonBar)
		
		installCustomTitleBar(widthPlan.dialogWidth)
		
		dialog.add(buildBody(widthPlan), BorderLayout.CENTER)
		
		buttonBar?.let {
			dialog.add(it, BorderLayout.PAGE_END)
		}
		
		dialog.pack()
		applySizeRule(widthPlan)
		DialogPositioner.place(dialog, spec.position, ActionDesk.graphicsConfiguration)
		startTimeoutIfNeeded()
		
		dialog.isVisible = true
		DialogSound.play(spec.sound)
		println(spec.sound)
	}
	
	private fun createWindow(): Window {
		return if (spec.showInTaskbar) {
			/*
			 * JFrame is still required for a reliable taskbar entry. Keep the real
			 * title for taskbar / Alt-Tab, while FlatLaf's title text is hidden and
			 * replaced by our embedded title component.
			 */
			JFrame().apply {
				title = spec.title
				defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
				layout = BorderLayout()
				isResizable = false
				isAlwaysOnTop = spec.alwaysOnTop
				type = Window.Type.NORMAL
			}
		} else {
			/*
			 * Use ActionDesk as a non-modal owner so the ADCD dialog window stays out of the
			 * taskbar. Do not use UTILITY because Windows/FlatLaf gives utility
			 * dialogs a smaller title pane. The native title is intentionally blank
			 * because JDialog does not hide FlatLaf's built-in title as reliably as
			 * JFrame; our custom JLabel below is the visible title.
			 */
			JDialog(null as Frame?).apply {
				title = ""
				modalityType = Dialog.ModalityType.MODELESS
				defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
				layout = BorderLayout()
				isResizable = false
				isAlwaysOnTop = spec.alwaysOnTop
				type = Window.Type.NORMAL
			}
		}
	}

	private fun installDismissKeyBinding() {
		val inputMap = rootPaneContainer.rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
		val actionMap = rootPaneContainer.rootPane.actionMap
		
		inputMap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			"adcd-dialog-dismiss"
		)
		
		val dismissAction: javax.swing.Action = object : javax.swing.AbstractAction() {
			override fun actionPerformed(e: ActionEvent?) {
				if (spec.dismissible) {
					completeDialog(DialogResult("dismiss"))
				}
			}
		}
		
		actionMap.put("adcd-dialog-dismiss", dismissAction)
	}
	
	private fun buildBody(widthPlan: DialogWidthPlan): JComponent {
		val bodyPadding = widthPlan.bodyPadding
		
		val body = DialogBodyPanel().apply {
			layout = BoxLayout(this, BoxLayout.Y_AXIS)
			border = BorderFactory.createEmptyBorder(
				bodyPadding.top,
				bodyPadding.left,
				bodyPadding.bottom,
				bodyPadding.right
			)
		}
		
		if (!spec.message.isNullOrBlank()) {
			addBodyChild(body, buildMessage(spec.message, widthPlan.bodyInnerWidth))
		}
		
		if (spec.fields.isNotEmpty()) {
			spec.fields.forEach { row ->
				addBodyChild(body, buildFieldRow(row, widthPlan.bodyInnerWidth))
			}
		}
		
		bodyScrollPane = JScrollPane(body).apply {
			border = null
			viewportBorder = null
			horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
			verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
			verticalScrollBar.unitIncrement = 20
			
			/*
			 * When the viewport is wider than the body preferred width,
			 * JScrollPane can otherwise show its own default background
			 * around the body. In dark mode that appears as an ugly
			 * white/light border.
			 */
			viewport.background = body.background
		}
		
		return bodyScrollPane
	}
	

	private fun buildFieldRow(row: DialogFieldRowSpec, availableWidth: Int): JComponent {
		val direction = row.layout.direction.lowercase()
		val isColumn = direction == "column"
		
		val rowOuterWidth = (
			availableWidth
				- row.margin.left
				- row.margin.right
		).coerceAtLeast(1)
		
		val rowInnerWidth = (
			rowOuterWidth
				- row.layout.padding.left
				- row.layout.padding.right
		).coerceAtLeast(1)
		
		val rowPanel = JPanel().apply {
			isOpaque = false
			layout = BoxLayout(
				this,
				if (isColumn) BoxLayout.Y_AXIS else BoxLayout.X_AXIS
			)
			border = EmptyBorder(
				row.layout.padding.top,
				row.layout.padding.left,
				row.layout.padding.bottom,
				row.layout.padding.right
			)
		}
		
		addRowItems(rowPanel, row, isColumn, rowInnerWidth)
		applyFixedComponentWidth(rowPanel, rowOuterWidth)
		
		if (row.margin.top == 0 && row.margin.right == 0 && row.margin.bottom == 0 && row.margin.left == 0) {
			return rowPanel
		}
		
		val marginWrapper = JPanel(BorderLayout()).apply {
			isOpaque = false
			border = EmptyBorder(
				row.margin.top,
				row.margin.left,
				row.margin.bottom,
				row.margin.right
			)
			add(rowPanel, BorderLayout.CENTER)
		}
		
		applyFixedComponentWidth(marginWrapper, availableWidth)
		
		return marginWrapper
	}
	
	private fun addRowItems(rowPanel: JPanel, row: DialogFieldRowSpec, isColumn: Boolean, availableWidth: Int) {
		val itemWidths = resolveRowItemWidths(row, isColumn, availableWidth)
		
		row.items.forEachIndexed { index, field ->
			if (index > 0) {
				addRowGap(rowPanel, row.layout.gap, isColumn)
			}
			
			val itemWidth = itemWidths.getOrElse(index) { availableWidth.coerceAtLeast(1) }
			val component = buildField(field, itemWidth)
			applyRowItemSizing(component, row.layout, isColumn, itemWidth)
			rowPanel.add(component)
		}
	}
	
	private fun resolveRowItemWidths(
		row: DialogFieldRowSpec,
		isColumn: Boolean,
		availableWidth: Int
	): List<Int> {
		if (row.items.isEmpty()) {
			return emptyList()
		}
		
		if (isColumn) {
			return row.items.map { resolveColumnItemWidth(availableWidth) }
		}
		
		if (row.items.size <= 1) {
			return row.items.map { field ->
				resolveSingleRowItemWidth(field, availableWidth)
			}
		}
		
		val totalGap = row.layout.gap.coerceAtLeast(0) * (row.items.size - 1)
		val availableForItems = (availableWidth - totalGap).coerceAtLeast(1)
		val fixedWidths = row.items.map { field -> resolveFixedRowItemWidth(field) }
		val fixedTotal = fixedWidths.filterNotNull().sum()
		val flexibleCount = fixedWidths.count { it == null }
		
		if (flexibleCount == 0) {
			return fixedWidths.map { it ?: 1 }
		}
		
		val flexibleWidth = ((availableForItems - fixedTotal).coerceAtLeast(1) / flexibleCount)
			.coerceAtLeast(1)
		
		return fixedWidths.map { width ->
			width ?: flexibleWidth
		}
	}
	
	private fun resolveSingleRowItemWidth(field: DialogFieldSpec, availableWidth: Int): Int {
		return resolveFixedRowItemWidth(field)
			?: availableWidth.coerceAtLeast(1)
	}
	
	private fun resolveFixedRowItemWidth(field: DialogFieldSpec): Int? {
		return when (field.type) {
			"image" -> resolveImageBoxSize(field).width
			else -> null
		}
	}
	
	private fun resolveColumnItemWidth(availableWidth: Int): Int {
		/*
		 * Column layout is intentionally form-first: fields keep using the
		 * available content width. align_items is a row-only v1 feature.
		 */
		return availableWidth.coerceAtLeast(1)
	}
	
	private fun addRowGap(rowPanel: JPanel, gap: Int, isColumn: Boolean) {
		if (gap <= 0) return
		
		rowPanel.add(
			if (isColumn) {
				Box.createVerticalStrut(gap)
			} else {
				Box.createHorizontalStrut(gap)
			}
		)
	}
	
	private fun applyRowItemSizing(component: JComponent, layout: DialogLayoutSpec, isColumn: Boolean, availableWidth: Int) {
		if (isColumn) {
			component.alignmentX = Component.LEFT_ALIGNMENT
		} else {
			component.alignmentX = Component.LEFT_ALIGNMENT
			component.alignmentY = rowVerticalAlignment(layout.alignItems)
		}
		
		if (component is JSeparator) {
			applyFixedComponentWidth(component, availableWidth)
			component.maximumSize = Dimension(Int.MAX_VALUE, component.preferredSize.height)
			return
		}
		
		applyFixedComponentWidth(component, availableWidth)
		
		component.maximumSize = Dimension(
			resolveRowItemMaxWidth(isColumn, availableWidth),
			component.preferredSize.height
		)
	}
	
	private fun resolveRowItemMaxWidth(isColumn: Boolean, availableWidth: Int): Int {
		if (isColumn) {
			return Int.MAX_VALUE
		}
		
		return availableWidth.coerceAtLeast(1)
	}
	
	private fun rowVerticalAlignment(alignItems: String): Float {
		return when (alignItems.lowercase()) {
			"center" -> Component.CENTER_ALIGNMENT
			else -> Component.TOP_ALIGNMENT
		}
	}
	
	private fun buildField(field: DialogFieldSpec, availableWidth: Int): JComponent {
		/*
		 * Static/display-only items do not need the field label wrapper.
		 * They are their own visual component.
		 */
		if (!isValueField(field)) {
			return buildFieldComponent(field, availableWidth)
		}
		
		/*
		 * Checkbox already renders its own text beside the checkbox.
		 */
		if (field.type == "checkbox") {
			val component = buildFieldComponent(field, availableWidth)
			registerFieldComponent(field, component)
			return component
		}
		
		return when (field.labelPosition.lowercase()) {
			"left" -> buildHorizontalLabelField(field, availableWidth, labelFirst = true)
			"right" -> buildHorizontalLabelField(field, availableWidth, labelFirst = false)
			else -> {
				val component = buildFieldComponent(field, availableWidth)
				registerFieldComponent(field, component)
				buildVerticalLabelField(field, component, availableWidth)
			}
		}
	}
	
	private fun registerFieldComponent(field: DialogFieldSpec, component: JComponent) {
		if (field.name.isNullOrBlank()) {
			return
		}
		
		component.name = field.name
		namedFields[field.name] = component
		fieldSpecs[field.name] = field
	}
	
	private fun buildVerticalLabelField(field: DialogFieldSpec, component: JComponent, availableWidth: Int): JComponent {
		val panel = JPanel().apply {
			isOpaque = false
			layout = BoxLayout(this, BoxLayout.Y_AXIS)
		}
		
		if (shouldRenderFieldLabel(field)) {
			panel.add(buildFieldLabel(field.label ?: "", availableWidth))
		}
		
		panel.add(component)
		applyFixedComponentWidth(panel, availableWidth)
		
		return panel
	}
	
	private fun buildHorizontalLabelField(
		field: DialogFieldSpec,
		availableWidth: Int,
		labelFirst: Boolean
	): JComponent {
		if (!shouldRenderFieldLabel(field)) {
			val component = buildFieldComponent(field, availableWidth)
			registerFieldComponent(field, component)
			return component
		}
		
		val label = JLabel(field.label).apply {
			maximumSize = preferredSize
		}
		
		val componentWidth = (availableWidth - label.preferredSize.width - 8)
			.coerceAtLeast(1)
		val component = buildFieldComponent(field, componentWidth)
		registerFieldComponent(field, component)
		
		label.labelFor = component
		
		val panel = JPanel(BorderLayout(8, 0)).apply {
			isOpaque = false
		}
		
		if (labelFirst) {
			panel.add(label, BorderLayout.LINE_START)
			panel.add(component, BorderLayout.CENTER)
		} else {
			panel.add(component, BorderLayout.CENTER)
			panel.add(label, BorderLayout.LINE_END)
		}
		
		applyFixedComponentWidth(panel, availableWidth)
		return panel
	}
	
	private fun shouldRenderFieldLabel(field: DialogFieldSpec): Boolean {
		return !field.label.isNullOrBlank() &&
				field.labelPosition != "none" &&
				field.labelPosition != "placeholder"
	}
	
	private fun buildFieldComponent(field: DialogFieldSpec, availableWidth: Int): JComponent {
		return when (field.type) {
			"label" -> buildStaticLabel(field, availableWidth)
			"text" -> buildStaticText(field, availableWidth)
			"divider" -> buildDivider(field, availableWidth)
			"image" -> buildImage(field)
			"password" -> buildPasswordField(field, availableWidth)
			"number" -> buildTextField(field, availableWidth)
			"select" -> buildSelectField(field, availableWidth)
			"textarea" -> buildTextarea(field, availableWidth)
			"checkbox" -> buildCheckboxField(field)
			"radio" -> buildRadioField(field, availableWidth)
			else -> buildTextField(field, availableWidth)
		}
	}
	
	private fun buildStaticLabel(field: DialogFieldSpec, availableWidth: Int): JComponent {
		/*
		 * Static label is display-only. If users want a field label,
		 * they should use the field's "label" property on an input component.
		 */
		return buildWrappingDisplayText(field.value ?: field.label ?: "", availableWidth)
	}
	
	private fun buildMessage(message: String, availableWidth: Int): JComponent {
		return buildWrappingDisplayText(message, availableWidth)
	}
	
	private fun buildStaticText(field: DialogFieldSpec, availableWidth: Int): JComponent {
		return buildWrappingDisplayText(field.value ?: field.label ?: "", availableWidth)
	}
	
	private fun buildFieldLabel(text: String, availableWidth: Int): JTextArea {
		return buildWrappingDisplayText(text, availableWidth) as JTextArea
	}
	
	private fun buildWrappingDisplayText(text: String, availableWidth: Int): JComponent {
		val textArea = JTextArea(text).apply {
			font = UIManager.getFont("Label.font")
			foreground = UIManager.getColor("Label.foreground")
			lineWrap = true
			wrapStyleWord = true
			isEditable = false
			isFocusable = false
			isOpaque = false
			border = null
			margin = Insets(0, 0, 0, 0)
		}
		
		applyWrappingDisplayTextSize(textArea, availableWidth)
		
		return textArea
	}
	
	private fun applyWrappingDisplayTextSize(textArea: JTextArea, availableWidth: Int) {
		val wrapWidth = availableWidth.coerceAtLeast(1)
		
		/*
		 * JTextArea calculates wrapped height correctly only after
		 * it has been given a real width.
		 *
		 * Height here is only temporary for calculation. We do not keep
		 * this huge height as preferred/max height.
		 */
		textArea.setSize(wrapWidth, Short.MAX_VALUE.toInt())
		
		val preferredHeight = textArea.preferredSize.height
		
		textArea.preferredSize = Dimension(wrapWidth, preferredHeight)
		textArea.minimumSize = Dimension(0, preferredHeight)
		textArea.maximumSize = Dimension(Int.MAX_VALUE, preferredHeight)
	}

	private fun buildDivider(field: DialogFieldSpec, availableWidth: Int): JComponent {
		return JSeparator(SwingConstants.HORIZONTAL).apply {
			applyFixedComponentWidth(this, availableWidth)
		}
	}
	
	private fun buildImage(field: DialogFieldSpec): JComponent {
		val size = resolveImageBoxSize(field)
		val imageRef = field.src
			?.trim()
			?.takeIf { it.isNotBlank() }
		
		val icon = imageRef?.dialogImageIcon(size.width, size.height)
		
		if (imageRef != null && icon == null) {
			App.logInfo("ADCD Dialog: image not found or unsupported: $imageRef")
		}
		
		return JLabel(icon).apply {
			isOpaque = false
			isFocusable = false
			horizontalAlignment = SwingConstants.CENTER
			verticalAlignment = SwingConstants.CENTER
			preferredSize = size
			minimumSize = size
			maximumSize = size
		}
	}
	
	private fun resolveImageBoxSize(field: DialogFieldSpec): Dimension {
		val width = field.width ?: field.height ?: DIALOG_IMAGE_DEFAULT_SIZE
		val height = field.height ?: field.width ?: DIALOG_IMAGE_DEFAULT_SIZE
		
		return Dimension(
			width.coerceAtLeast(1),
			height.coerceAtLeast(1)
		)
	}
	
	private fun buildRadioField(field: DialogFieldSpec, availableWidth: Int): JComponent {
		val panel = JPanel().apply {
			isOpaque = false
			layout = BoxLayout(this, BoxLayout.Y_AXIS)
			putClientProperty("dialog.field.type", "radio")
		}
		
		val group = ButtonGroup()
		val radioItems = mutableListOf<DialogRadioItem>()
		
		field.options.forEachIndexed { index, option ->
			val radio = JRadioButton(option.label).apply {
				isOpaque = false
				
				if (field.value != null && option.value == field.value) {
					isSelected = true
				}
				
				/*
				 * If no explicit value is provided, do not auto-select.
				 * This allows required radio groups to fail validation properly.
				 * */
			}
			
			group.add(radio)
			radioItems.add(DialogRadioItem(radio, option))
			
			if (index > 0) {
				panel.add(Box.createVerticalStrut(4))
			}
			
			panel.add(radio)
		}
		
		panel.putClientProperty("adcd.dialog.radio.items", radioItems)
		applyFixedComponentWidth(panel, availableWidth)
		
		return panel
	}
	
	private fun buildCheckboxField(field: DialogFieldSpec): JComponent {
		return JCheckBox().apply {
			isSelected = field.checked
			
			if (field.labelPosition != "none" && !field.label.isNullOrBlank()) {
				text = field.label
			}
		}
	}
	
	private fun buildTextField(field: DialogFieldSpec, availableWidth: Int): JComponent {
		return configureTextField(FlatTextField(), field).apply {
			applyFixedComponentWidth(this, availableWidth)
		}
	}
	
	private fun buildPasswordField(field: DialogFieldSpec, availableWidth: Int): JComponent {
		return configureTextField(JPasswordField(), field).apply {
			applyFixedComponentWidth(this, availableWidth)
		}
	}
	
	private fun <T : JTextField> configureTextField(component: T, field: DialogFieldSpec): T {
		component.text = field.value ?: ""
		
		val placeholder = resolvePlaceholder(field)
		
		if (!placeholder.isNullOrBlank()) {
			component.putClientProperty("JTextField.placeholderText", placeholder)
		}
		
		return component
	}
	
	private fun resolvePlaceholder(field: DialogFieldSpec): String? {
		if (field.labelPosition == "placeholder" && !field.label.isNullOrBlank()) {
			return field.label
		}
		
		if (!field.placeholder.isNullOrBlank()) {
			return field.placeholder
		}
		
		return null
	}
	
	private fun buildTextarea(field: DialogFieldSpec, availableWidth: Int): JComponent {
		val textArea = JTextArea(field.value ?: "").apply {
			rows = 4
			lineWrap = true
			wrapStyleWord = true
		}
		
		return JScrollPane(textArea).apply {
			name = field.name
			border = BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"))
			putClientProperty("dialog.textarea", textArea)
			applyFixedComponentWidth(this, availableWidth)
		}
	}
	
	private fun buildSelectField(field: DialogFieldSpec, availableWidth: Int): JComponent {
		return JComboBox(field.options.toTypedArray()).apply {
			if (field.value != null) {
				for (i in 0 until itemCount) {
					val item = getItemAt(i)
					
					if (item.value == field.value) {
						selectedIndex = i
						break
					}
				}
			}
			
			applyFixedComponentWidth(this, availableWidth)
		}
	}
	
	private fun buildButtonBar(): JComponent? {
		if (!spec.buttons.visible) {
			return null
		}
		
		val buttons = resolveButtons()
		
		if (buttons.isEmpty()) {
			return null
		}
		
		val buttonPadding = spec.buttons.padding
		
		val buttonBar = JPanel(BorderLayout()).apply {
			val paddingBorder = EmptyBorder(
				buttonPadding.top,
				buttonPadding.left,
				buttonPadding.bottom,
				buttonPadding.right
			)
			
			border = if (spec.buttons.showBorder) {
				BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(
						1,
						0,
						0,
						0,
						UIManager.getColor("Component.borderColor")
					),
					paddingBorder
				)
			} else {
				paddingBorder
			}
		}
		
		val innerPanel = buildButtonBarInnerPanel(buttons)
		buttonBar.add(innerPanel, BorderLayout.CENTER)
		
		return buttonBar
	}
	
	private fun resolveButtons(): List<DialogButtonSpec> {
		return when (resolveButtonModel()) {
			"custom" -> spec.buttons.items
			"acknowledge" -> listOf(
				spec.buttons.acknowledge.copy(
					role = "neutral"
				)
			)
			else -> listOf(
				spec.buttons.cancel.copy(
					role = "cancel",
					defaultButton = false
				),
				spec.buttons.confirm.copy(
					role = "submit"
				)
			)
		}
	}
	
	private fun resolveButtonModel(): String {
		val model = spec.buttons.model
			?.trim()
			?.lowercase()
		
		if (model == "confirm" || model == "acknowledge" || model == "custom") {
			return model
		}
		
		return if (spec.fields.isNotEmpty()) {
			"confirm"
		} else {
			"acknowledge"
		}
	}
	
	private fun handleButton(button: DialogButtonSpec) {
		when (button.role) {
			"submit" -> {
				if (!validateFields()) {
					return
				}
				
				completeDialog(
					DialogResult(
						button = button.id,
						values = collectValues()
					)
				)
			}
			
			"cancel" -> {
				completeDialog(
					DialogResult(
						button = button.id,
						values = emptyMap()
					)
				)
			}
			
			else -> {
				completeDialog(
					DialogResult(
						button = button.id,
						values = emptyMap()
					)
				)
			}
		}
	}
	
	private fun collectValues(): Map<String, Any?> {
		val values = linkedMapOf<String, Any?>()
		
		namedFields.forEach { (name, component) ->
			values[name] = readValue(component)
		}
		
		return values
	}
	
	private fun readValue(component: JComponent): Any? {
		if (component.getClientProperty("dialog.field.type") == "radio") {
			@Suppress("UNCHECKED_CAST")
			val radioItems = component.getClientProperty("adcd.dialog.radio.items") as? List<DialogRadioItem>
			
			return radioItems
				?.firstOrNull { it.button.isSelected }
				?.option
				?.value
		}
		
		return when (component) {
			is JCheckBox -> component.isSelected
			is JPasswordField -> String(component.password)
			is JTextField -> component.text
			
			is JComboBox<*> -> {
				when (val item = component.selectedItem) {
					is DialogOptionSpec -> item.value
					else -> item?.toString()
				}
			}
			
			is JScrollPane -> {
				val textArea = component.getClientProperty("dialog.textarea") as? JTextArea
				textArea?.text
			}
			
			else -> null
		}
	}
	
	private fun validateFields(): Boolean {
		clearErrors()
		
		val errors = mutableListOf<DialogValidationError>()
		
		fieldSpecs.forEach { (name, field) ->
			val component = namedFields[name] ?: return@forEach
			val value = readValue(component)
			
			DialogValidator.validate(name, field, value)?.let { error ->
				errors.add(error)
			}
		}
		
		if (errors.isEmpty()) {
			return true
		}
		
		applyErrors(errors)
		return false
	}
	
	private fun clearErrors() {
		namedFields.values.forEach { component ->
			ValidationOutline.clear(component)
		}
	}
	
	private fun applyErrors(errors: List<DialogValidationError>) {
		errors.forEach { error ->
			val component = namedFields[error.fieldName] ?: return@forEach
			
			ValidationOutline.error(component, error.message)
		}
		
		val firstErrorComponent = namedFields[errors.first().fieldName]
		firstErrorComponent?.requestFocusInWindow()
	}
	
	private fun resolveWidthPlan(buttonBar: JComponent?): DialogWidthPlan {
		return DialogSizeRules.resolveWidthPlan(
			spec = spec,
			preferredBodyInnerWidth = measureBodyNaturalContentWidth(),
			preferredNonBodyWidth = buttonBar?.preferredSize?.width ?: 0,
			screen = Toolkit.getDefaultToolkit().screenSize
		)
	}
	
	private fun measureBodyNaturalContentWidth(): Int {
		var width = 1
		
		if (!spec.message.isNullOrBlank()) {
			width = width.coerceAtLeast(measureNaturalTextWidth(spec.message))
		}
		
		spec.fields.forEach { row ->
			width = width.coerceAtLeast(measureFieldRowNaturalWidth(row))
		}
		
		return width
	}
	
	private fun measureFieldRowNaturalWidth(row: DialogFieldRowSpec): Int {
		if (row.items.isEmpty()) {
			return 1
		}
		
		val rowPadding = row.margin.left + row.margin.right + row.layout.padding.left + row.layout.padding.right
		val itemWidths = row.items.map { field ->
			measureFieldNaturalWidth(field)
		}
		
		if (row.layout.direction.lowercase() == "column") {
			return (itemWidths.maxOrNull() ?: 1) + rowPadding
		}
		
		val totalGap = row.layout.gap.coerceAtLeast(0) * (row.items.size - 1)
		
		return itemWidths.sum() + totalGap + rowPadding
	}
	
	private fun measureFieldNaturalWidth(field: DialogFieldSpec): Int {
		if (!isValueField(field)) {
			return when (field.type) {
				"divider" -> 1
				"image" -> resolveImageBoxSize(field).width
				else -> measureNaturalTextWidth(field.value ?: field.label ?: "")
			}
		}
		
		val componentWidth = measureFieldComponentNaturalWidth(field)
		
		if (field.type == "checkbox" || !shouldRenderFieldLabel(field)) {
			return componentWidth
		}
		
		val labelWidth = measureNaturalTextWidth(field.label ?: "")
		
		return when (field.labelPosition.lowercase()) {
			"left", "right" -> labelWidth + 8 + componentWidth
			else -> labelWidth.coerceAtLeast(componentWidth)
		}
	}
	
	private fun measureFieldComponentNaturalWidth(field: DialogFieldSpec): Int {
		return when (field.type) {
			"select" -> field.options
				.maxOfOrNull { option -> measureNaturalTextWidth(option.label) }
				?.plus(56)
				?: 160
			"textarea" -> 260
			"radio" -> field.options
				.maxOfOrNull { option -> measureNaturalTextWidth(option.label) }
				?.plus(32)
				?: 160
			"checkbox" -> measureNaturalTextWidth(field.label ?: "") + 32
			else -> 220
		}
	}
	
	private fun measureNaturalTextWidth(text: String): Int {
		val label = JLabel()
		label.font = UIManager.getFont("Label.font") ?: label.font
		val fontMetrics = label.getFontMetrics(label.font)
		
		return text
			.lines()
			.maxOfOrNull { line ->
				fontMetrics.stringWidth(line)
			}
			?: 1
	}
	
	private fun applySizeRule(widthPlan: DialogWidthPlan) {
		val screen = Toolkit.getDefaultToolkit().screenSize
		val widthRange = DialogSizeRules.widthRange(spec, screen)
		val heightRange = DialogSizeRules.heightRange(spec, screen)
		
		val finalHeight = DialogSizeRules.resolveHeight(
			spec = spec,
			preferredHeight = dialog.height,
			screen = screen
		)
		
		dialog.size = Dimension(widthPlan.dialogWidth, finalHeight)
		dialog.minimumSize = Dimension(
			widthRange.min.coerceAtMost(widthPlan.dialogWidth),
			heightRange.min.coerceAtMost(finalHeight)
		)
	}

	private fun addBodyChild(body: JPanel, component: JComponent) {
		if (body.componentCount > 0 && spec.layout.gap > 0) {
			body.add(Box.createVerticalStrut(spec.layout.gap))
		}
		
		component.alignmentX = Component.LEFT_ALIGNMENT
		
		component.maximumSize = Dimension(
			Int.MAX_VALUE,
			component.preferredSize.height
		)
		
		body.add(component)
	}
	
	private fun applyFixedComponentWidth(component: JComponent, width: Int) {
		val preferredHeight = component.preferredSize.height.coerceAtLeast(1)
		val fixedWidth = width.coerceAtLeast(1)
		
		component.preferredSize = Dimension(fixedWidth, preferredHeight)
		component.minimumSize = Dimension(0, preferredHeight)
		component.maximumSize = Dimension(Int.MAX_VALUE, preferredHeight)
	}
	
	private fun isValueField(field: DialogFieldSpec): Boolean {
		return when (field.type) {
			"label", "text", "divider", "image" -> false
			else -> true
		}
	}
	
	private fun applyIcon() {
		val icon = loadDialogImageIcon(32, logMissing = true) ?: return
		dialog.setIconImage(icon.image)
	}
	
	private fun installCustomTitleBar(dialogWidth: Int) {
		val rootPane = rootPaneContainer.rootPane
		
		/*
		 * Same idea as ActionDesk: embed a JMenuBar into the FlatLaf title pane.
		 * The important difference from the previous patch is that the menu bar is
		 * given a width derived from the resolved dialog width. Otherwise, FlatLaf
		 * uses the menu bar's tiny preferred width and the JLabel has no room to
		 * expand/ellipsis.
		 */
		rootPane.putClientProperty("JRootPane.useWindowDecorations", true)
		rootPane.putClientProperty("JRootPane.menuBarEmbedded", true)
		rootPane.putClientProperty("JRootPane.titleBarShowTitle", false)
		rootPane.putClientProperty("JRootPane.titleBarShowIcon", false)
		rootPane.putClientProperty("JRootPane.titleBarShowMaximize", false)
		rootPane.putClientProperty("JRootPane.titleBarHeight", DIALOG_TITLE_BAR_HEIGHT)
		
		if (!spec.showInTaskbar && rootPaneContainer is JDialog) {
			(rootPaneContainer as JDialog).title = ""
		}
		
		val titleBarWidth = (dialogWidth - DIALOG_TITLE_BAR_BUTTON_SPACE)
			.coerceAtLeast(DIALOG_TITLE_BAR_MIN_WIDTH)
		
		val menuBar = JMenuBar().apply {
			isOpaque = false
			border = null
			layout = BorderLayout()
			isFocusable = false
			preferredSize = Dimension(titleBarWidth, DIALOG_TITLE_BAR_HEIGHT)
			minimumSize = Dimension(0, DIALOG_TITLE_BAR_HEIGHT)
			maximumSize = Dimension(Int.MAX_VALUE, DIALOG_TITLE_BAR_HEIGHT)
			putClientProperty("JRootPane.titleBarCaption", true)
		}
		
		menuBar.add(buildTitleBarContent(titleBarWidth), BorderLayout.CENTER)
		
		when (val container = rootPaneContainer) {
			is JFrame -> container.jMenuBar = menuBar
			is JDialog -> container.jMenuBar = menuBar
		}
	}
	
	private fun buildTitleBarContent(titleBarWidth: Int): JComponent {
		val panel = JPanel(BorderLayout(6, 0)).apply {
			isOpaque = false
			border = EmptyBorder(1, 0, 0, 0)
			isFocusable = false
			preferredSize = Dimension(titleBarWidth, DIALOG_TITLE_BAR_HEIGHT)
			minimumSize = Dimension(0, DIALOG_TITLE_BAR_HEIGHT)
			maximumSize = Dimension(Int.MAX_VALUE, DIALOG_TITLE_BAR_HEIGHT)
			putClientProperty("JRootPane.titleBarCaption", true)
		}
		
		loadDialogImageIcon(20, logMissing = false)?.let { icon ->
			panel.add(
				JLabel(icon).apply {
					isOpaque = false
					isFocusable = false
					verticalAlignment = SwingConstants.CENTER
					putClientProperty("JRootPane.titleBarCaption", true)
				},
				BorderLayout.LINE_START
			)
		}
		
		val titleLabel = JLabel(spec.title).apply {
			isOpaque = false
			isFocusable = false
			horizontalAlignment = SwingConstants.LEFT
			verticalAlignment = SwingConstants.CENTER
			border = EmptyBorder(DIALOG_TITLE_TEXT_TOP_PADDING, 0, 0, 0)
			font = UIManager.getFont("Label.font") ?: font
			foreground = UIManager.getColor("Label.foreground") ?: foreground
			minimumSize = Dimension(0, DIALOG_TITLE_BAR_HEIGHT)
			preferredSize = Dimension(0, DIALOG_TITLE_BAR_HEIGHT)
			maximumSize = Dimension(Int.MAX_VALUE, DIALOG_TITLE_BAR_HEIGHT)
			toolTipText = spec.title.takeIf { it.isNotBlank() }
			putClientProperty("JRootPane.titleBarCaption", true)
		}
		
		panel.add(titleLabel, BorderLayout.CENTER)
		
		return panel
	}
	
	private fun loadDialogImageIcon(size: Int, logMissing: Boolean): ImageIcon? {
		val iconRef = spec.icon
			?.trim()
			?.takeIf { it.isNotBlank() }
			?: return null
		
		val icon = iconRef.dialogIcon(size)
		
		if (icon == null) {
			if (logMissing) {
				App.logInfo("ADCD Dialog: icon not found or unsupported: $iconRef")
			}
			
			return null
		}
		
		return icon.toImageIcon()
	}
	
	private fun startTimeoutIfNeeded() {
		val timeoutMs = resolveTimeoutMs()
		
		if (timeoutMs <= 0) {
			return
		}
		
		timeoutTimer = Timer(timeoutMs) {
			completeDialog(
				DialogResult(
					button = "timeout",
					values = emptyMap()
				)
			)
		}.apply {
			isRepeats = false
			start()
		}
	}
	
	private fun stopTimeoutTimer() {
		timeoutTimer?.stop()
		timeoutTimer = null
	}
	
	private fun resolveTimeoutMs(): Int {
		/*
		 * Forms should not auto-close by default because that can be dangerous.
		 * But if timeout_ms is explicitly provided as a positive number, honor it.
		 */
		if (spec.fields.isNotEmpty()) {
			return spec.timeoutMs ?: 0
		}
		
		/*
		 * Message-only dialogs auto-hide after 5 seconds by default.
		 */
		if (!spec.message.isNullOrBlank()) {
			return spec.timeoutMs ?: 5000
		}
		
		return spec.timeoutMs ?: 0
	}
	
	private fun buildButtonBarInnerPanel(buttons: List<DialogButtonSpec>): JPanel {
		if (spec.buttons.layout == "fill") {
			return buildFillButtonBar(buttons)
		}
		
		return buildAlignedButtonBar(buttons)
	}
	
	private fun buildFillButtonBar(buttons: List<DialogButtonSpec>): JPanel {
		val panel = JPanel(GridLayout(1, buttons.size.coerceAtLeast(1), spec.buttons.gap, 0))
		
		buttons.forEach { buttonSpec ->
			panel.add(createDialogButton(buttonSpec))
		}
		
		return panel
	}
	
	private fun buildAlignedButtonBar(buttons: List<DialogButtonSpec>): JPanel {
		val panel = JPanel().apply {
			layout = BoxLayout(this, BoxLayout.X_AXIS)
		}
		
		when (spec.buttons.layout) {
			"center" -> panel.add(Box.createHorizontalGlue())
			"right" -> panel.add(Box.createHorizontalGlue())
		}
		
		buttons.forEachIndexed { index, buttonSpec ->
			if (index > 0 && spec.buttons.gap > 0) {
				panel.add(Box.createHorizontalStrut(spec.buttons.gap))
			}
			
			panel.add(createDialogButton(buttonSpec))
		}
		
		when (spec.buttons.layout) {
			"center" -> panel.add(Box.createHorizontalGlue())
			"left" -> panel.add(Box.createHorizontalGlue())
		}
		
		return panel
	}
	
	private fun createDialogButton(buttonSpec: DialogButtonSpec): JButton {
		val button = JButton(buttonSpec.label)
		
		applyButtonStyle(button, buttonSpec)
		
		button.addActionListener {
			handleButton(buttonSpec)
		}
		
		if (buttonSpec.defaultButton) {
			rootPaneContainer.rootPane.defaultButton = button
		}
		
		return button
	}
	
	private fun completeDialog(dialogResult: DialogResult) {
		if (completed) {
			return
		}
		
		completed = true
		result = dialogResult
		
		stopTimeoutTimer()
		dialog.dispose()
		onComplete(dialogResult)
	}
	
	private fun applyButtonStyle(button: JButton, buttonSpec: DialogButtonSpec) {
		buttonSpec.padding?.let { padding ->
			button.margin = Insets(
				padding.top,
				padding.left,
				padding.bottom,
				padding.right
			)
		}
		
		buttonSpec.fontSize?.let { fontSize ->
			button.font = button.font.deriveFont(Font.PLAIN, fontSize)
		}
		
		ColorUtil.parse(buttonSpec.color)?.let { color ->
			button.foreground = color
		}
		
		ColorUtil.parse(buttonSpec.background)?.let { background ->
			button.background = background
			button.isOpaque = true
			button.isContentAreaFilled = true
		}
		
		buttonSpec.borderRadius?.let { radius ->
			button.putClientProperty("JButton.buttonType", "roundRect")
			button.putClientProperty("FlatLaf.style", "arc: $radius")
		}
	}
	
	private companion object {
		val DIALOG_TITLE_BAR_HEIGHT: Int = UIScale.scale(32)
		val DIALOG_TITLE_BAR_BUTTON_SPACE: Int = UIScale.scale(96)
		val DIALOG_TITLE_BAR_MIN_WIDTH: Int = UIScale.scale(120)
		val DIALOG_TITLE_TEXT_TOP_PADDING: Int = UIScale.scale(1)
		val DIALOG_IMAGE_DEFAULT_SIZE: Int = UIScale.scale(64)
	}
	
	private class DialogBodyPanel : JPanel(), Scrollable {
		
		override fun getPreferredScrollableViewportSize(): Dimension {
			return preferredSize
		}
		
		override fun getScrollableUnitIncrement(
			visibleRect: Rectangle,
			orientation: Int,
			direction: Int
		): Int {
			return 20
		}
		
		override fun getScrollableBlockIncrement(
			visibleRect: Rectangle,
			orientation: Int,
			direction: Int
		): Int {
			return 80
		}
		
		override fun getScrollableTracksViewportWidth(): Boolean {
			return true
		}
		
		override fun getScrollableTracksViewportHeight(): Boolean {
			return false
		}
	}
	
}