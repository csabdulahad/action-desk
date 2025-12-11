package net.abdulahad.action_desk.view

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.helper.Icons
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.helper.ViewHelper
import org.jdesktop.swingx.VerticalLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Window
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel

class About(
	dialog: Window,
): JDialog(dialog) {
	
	val panel = JPanel(BorderLayout())
	
	init {
		setupDialog()
		setActionDeskIcon()
		setDetails()
		
		pack()
		setLocationRelativeTo(dialog)
		
		isVisible = true
	}
	
	private fun setupDialog() {
		title = "About"
		layout = BorderLayout()
		isResizable = false
		modalityType = ModalityType.APPLICATION_MODAL
		minimumSize = Dimension(350, preferredSize.height)
		
		panel.border = BorderFactory.createEmptyBorder(16, 16, 16, 16)
		add(panel)
	}
	
	private fun setActionDeskIcon() {
		JLabel().apply {
			icon = Icons.ACTION_DESK.icon(64)
			border = BorderFactory.createEmptyBorder(0, 0, 0, 16)
			panel.add(this, BorderLayout.WEST)
		}
	}
	
	private fun setDetails() {
		JPanel(VerticalLayout(0)).apply {
			val ad = JLabel(App.getName(" "))
			ad.font = ad.font.deriveFont(22f)
			add(ad)
			
			val developerPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
				add(JLabel("by "))
				add(ViewHelper.getLinkLabel("Abdul Ahad", App.DEVELOPER_LINK, App.DEVELOPER_LINK))
			}
			add(developerPanel)
			
			val githubPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
				border = BorderFactory.createEmptyBorder(16, 0, 0, 0)
				
				add(JLabel("Visit "))
				
				val repoLink = "https://github.com/csabdulahad/action-desk"
				add(ViewHelper.getLinkLabel("GitHub Repository", repoLink, repoLink))
				
				add(JLabel(" to learn more."))
			}
			add(githubPanel)
			
			panel.add(this, BorderLayout.CENTER)
		}
	}
	
}