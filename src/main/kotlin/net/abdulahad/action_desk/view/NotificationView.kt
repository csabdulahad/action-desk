package net.abdulahad.action_desk.view

import net.abdulahad.action_desk.engine.notification.NotificationListener
import net.abdulahad.action_desk.engine.notification.NotificationManager
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.lib.view.ButtonUnderlined
import net.abdulahad.action_desk.lib.view.JPanel2
import net.abdulahad.action_desk.lib.view.Popup2
import net.abdulahad.action_desk.lib.view.jlist.ListView
import net.abdulahad.action_desk.model.Notification
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextArea
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

class NotificationView : NotificationListener {
	private val popup = Popup2()
	private val panel = JPanel(BorderLayout()).apply {
		preferredSize = Dimension(400, 500)
	}
	private val listView = ListView(
		itemGap = 12,
		gapTop = 12,
		gapLeft = 12,
		gapBottom = 0,
		gapRight = 12
	)
	
	private var dialog: JDialog? = null
	
	init {
		popup.setContent(panel)
		popup.setDialogDecoratorCallback { d ->
			dialog = d
			setTitleBar(d)
		}
		
		listView.disableHighlightingBG()
		listView.disableTrack()
		
		panel.add(JSeparator(), BorderLayout.NORTH)
		panel.add(listView.scrollPane, BorderLayout.CENTER)
		
		NotificationManager.listen(this)
		loadNotifications()
	}
	
	private fun setTitleBar(dialog: JDialog) {
		val menuBar = JMenuBar()
		val title = JLabel("Notifications").apply {
			border = EmptyBorder(3, 0, 0, 0)
		}
		
		menuBar.add(title)
		menuBar.add(Box.createHorizontalGlue())
		menuBar.add(ButtonUnderlined("Clear all").apply {
			addActionListener {
				NotificationManager.clear()
				hide()
			}
		})
		
		dialog.jMenuBar = menuBar
	}
	
	fun show(button: JButton) {
		popup.show(button)
		NotificationManager.acknowledged()
	}
	
	fun hide() {
		popup.hide()
	}
	
	override fun onNewNotification(notification: Notification) {
		listView.addItem(createItem(notification))
	}
	
	override fun onNotificationClear() {
		listView.clearList(false)
		listView.revalidate()
		listView.repaint()
	}
	
	private fun loadNotifications() {
		NotificationManager.list().forEach { onNewNotification(it) }
	}
	
	private fun createItem(notification: Notification): JPanel2 {
		val itemPanel = JPanel2(BorderLayout())
		
		val westWrapper = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
			isOpaque = false
			border = EmptyBorder(0, 0, 0, 4)
			add(JLabel(notification.icon.icon(20)))
		}
		itemPanel.add(westWrapper, BorderLayout.WEST)
		
		val msgText = JTextArea(notification.msg).apply {
			font = font.deriveFont(font.size2D - 1.2f)
			lineWrap = true
			wrapStyleWord = true
			isEditable = false
			isFocusable = true
			isEnabled = true
			isOpaque = false
			border = null
			minimumSize = Dimension(0, 0)
		}
		itemPanel.add(msgText, BorderLayout.CENTER)
		
		val datePanel = JPanel().apply {
			layout = BoxLayout(this, BoxLayout.Y_AXIS)
			preferredSize = Dimension(100, 40)
			isOpaque = false
			
			val timeLabel = JLabel(notification.time()).apply {
				font = font.deriveFont(font.size2D - 1.8f)
				alignmentX = 1f
				foreground = UIManager.getColor("Label.disabledForeground")
			}
			
			val dateLabel = JLabel(notification.date()).apply {
				font = font.deriveFont(font.size2D - 1.8f)
				alignmentX = 1f
				foreground = UIManager.getColor("Label.disabledForeground")
			}
			
			add(Box.createVerticalGlue())
			add(timeLabel)
			add(dateLabel)
			add(Box.createVerticalGlue())
		}
		itemPanel.add(datePanel, BorderLayout.EAST)
		
		itemPanel.border = EmptyBorder(8, 4, 8, 8)
		itemPanel.isOpaque = false
		itemPanel.putClientProperty("FlatLaf.style", """
			background: @secondaryColor;
			arc: 3;
		""".trimIndent())
		
		return itemPanel
	}
}
