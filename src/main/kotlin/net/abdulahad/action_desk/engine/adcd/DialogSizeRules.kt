package net.abdulahad.action_desk.engine.adcd

import java.awt.Dimension
import kotlin.math.roundToInt

data class DialogAxisRange(
	val min: Int,
	val max: Int
) {
	fun clamp(value: Int): Int {
		return value.coerceIn(min, max)
	}
}

data class DialogWidthPlan(
	val dialogWidth: Int,
	val bodyOuterWidth: Int,
	val bodyInnerWidth: Int,
	val bodyPadding: DialogPadding
)

object DialogSizeRules {
	
	private const val SCREEN_WIDTH_MARGIN = 80
	private const val SCREEN_HEIGHT_MARGIN = 120
	private const val ABSOLUTE_MIN_WIDTH = 120
	private const val ABSOLUTE_MIN_HEIGHT = 120
	private const val ABSOLUTE_MIN_BODY_INNER_WIDTH = 80
	
	fun widthRange(spec: DialogSpec, screen: Dimension): DialogAxisRange {
		val safeScreenWidth = safeScreenWidth(screen)
		
		val minWidth = spec.minWidth
			.coerceAtLeast(1)
			.coerceAtMost(safeScreenWidth)
		
		/*
		 * CSS-like rule: if min_width is greater than max_width,
		 * min_width wins. We should never crash with an empty range.
		 */
		val maxWidth = spec.maxWidth
			.coerceAtLeast(minWidth)
			.coerceAtMost(safeScreenWidth)
			.coerceAtLeast(minWidth)
		
		return DialogAxisRange(minWidth, maxWidth)
	}
	
	fun heightRange(spec: DialogSpec, screen: Dimension): DialogAxisRange {
		val safeScreenHeight = safeScreenHeight(screen)
		
		val minHeight = spec.minHeight
			?.let { DialogStyleParser.sizeValue(it, screen.height, 0) }
			?: 0
		
		val safeMinHeight = minHeight
			.coerceAtLeast(0)
			.coerceAtMost(safeScreenHeight)
		
		val parsedMaxHeight = DialogStyleParser.sizeValue(
			spec.maxHeight,
			screen.height,
			(screen.height * 0.8).roundToInt()
		)
		
		/*
		 * Same rule as width: if min_height is greater than max_height,
		 * min_height wins. No empty coerce range.
		 */
		val maxHeight = parsedMaxHeight
			.coerceAtLeast(safeMinHeight.coerceAtLeast(1))
			.coerceAtMost(safeScreenHeight)
			.coerceAtLeast(safeMinHeight)
		
		return DialogAxisRange(safeMinHeight, maxHeight)
	}
	
	fun resolveWidth(spec: DialogSpec, preferredWidth: Int, screen: Dimension): Int {
		val range = widthRange(spec, screen)
		
		val requestedWidth = if (DialogStyleParser.isAutoSize(spec.width)) {
			preferredWidth
		} else {
			DialogStyleParser.sizeValue(spec.width, screen.width, preferredWidth)
		}
		
		return range.clamp(
			requestedWidth.coerceAtLeast(ABSOLUTE_MIN_WIDTH)
		)
	}
	
	fun resolveWidthPlan(
		spec: DialogSpec,
		preferredBodyInnerWidth: Int,
		preferredNonBodyWidth: Int,
		screen: Dimension
	): DialogWidthPlan {
		val bodyPadding = spec.padding + spec.layout.padding
		val horizontalPadding = bodyPadding.left + bodyPadding.right
		
		val preferredBodyOuterWidth = preferredBodyInnerWidth
			.coerceAtLeast(1) + horizontalPadding
		
		val preferredDialogWidth = preferredBodyOuterWidth
			.coerceAtLeast(preferredNonBodyWidth)
		
		val dialogWidth = resolveWidth(
			spec = spec,
			preferredWidth = preferredDialogWidth,
			screen = screen
		)
		
		val bodyInnerWidth = (dialogWidth - horizontalPadding)
			.coerceAtLeast(ABSOLUTE_MIN_BODY_INNER_WIDTH)
		
		return DialogWidthPlan(
			dialogWidth = dialogWidth,
			bodyOuterWidth = dialogWidth,
			bodyInnerWidth = bodyInnerWidth,
			bodyPadding = bodyPadding
		)
	}
	
	fun resolveHeight(spec: DialogSpec, preferredHeight: Int, screen: Dimension): Int {
		val range = heightRange(spec, screen)
		
		val requestedHeight = if (DialogStyleParser.isAutoSize(spec.height)) {
			preferredHeight
		} else {
			DialogStyleParser.sizeValue(spec.height, screen.height, preferredHeight)
		}
		
		return range.clamp(
			requestedHeight.coerceAtLeast(ABSOLUTE_MIN_HEIGHT)
		)
	}
	
	private fun safeScreenWidth(screen: Dimension): Int {
		return (screen.width - SCREEN_WIDTH_MARGIN).coerceAtLeast(240)
	}
	
	private fun safeScreenHeight(screen: Dimension): Int {
		return (screen.height - SCREEN_HEIGHT_MARGIN).coerceAtLeast(240)
	}
	
}
