package net.abdulahad.action_desk.view.list_model

import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.model.Action
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.border.EmptyBorder

class ActionEntry : JPanel(), ListCellRenderer<Action> {
	
	private val iconLabel = JLabel()
	private val titleLabel = JLabel()
	private val shortcutLabel = JLabel()
	
	init {
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8))
		setLayout(BorderLayout(8, 8))

		iconLabel.preferredSize = Dimension(24, 24)
		
		shortcutLabel.apply {
			setHorizontalAlignment(SwingConstants.RIGHT)
			setFont(Font(shortcutLabel.getFont().getName(), Font.BOLD, 14))
		}
		
		titleLabel.apply {
			preferredSize = Dimension(24, titleLabel.preferredSize.height)
			border = EmptyBorder(0, 0, 0, 7)
		}
		
		val textPanel = JPanel(BorderLayout())
		textPanel.setOpaque(false)
		textPanel.add(titleLabel, BorderLayout.CENTER)
		textPanel.add(shortcutLabel, BorderLayout.EAST)
		
		setOpaque(true)
		add(iconLabel, BorderLayout.WEST)
		add(textPanel, BorderLayout.CENTER)
	}
	
	override fun getListCellRendererComponent(list: JList<out Action>, value: Action, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
		// Icon
		if (!value.icon.isEmpty()) {
			val icon = value.icon.icon(22)
			iconLabel.icon = icon
		} else {
			iconLabel.setIcon(null)
		}
		
		titleLabel.setText(value.name)
		
		shortcutLabel.setText(value.hotkey)
		
		// Highlight
		if (isSelected) {
			setBackground(list.selectionBackground)
			setForeground(list.selectionForeground)
		} else {
			setBackground(list.getBackground())
			setForeground(list.getForeground())
		}
		
		return this
	}
}