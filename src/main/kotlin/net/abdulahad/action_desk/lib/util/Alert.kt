package net.abdulahad.action_desk.lib.util

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.components.FlatButton
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Frame
import java.awt.Window
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager

class Alert private constructor() {
    
    private var title: String = "Confirm"
    private var message: String = ""
    private var onAck: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    fun confirm(msg: String): Alert {
        this.message = msg
        return this
    }

    fun title(title: String): Alert {
        this.title = title
        return this
    }

    fun onAck(callback: () -> Unit): Alert {
        this.onAck = callback
        return this
    }

    fun onCancel(callback: () -> Unit): Alert {
        this.onCancel = callback
        return this
    }
    
    fun show(parent: Window? = null) {
        lateinit var dialog: JDialog
        
        if (parent != null) {
            dialog = JDialog(parent)
            dialog.isModal = true
        } else {
            dialog = JDialog()
        }
        
        
        dialog.minimumSize = Dimension(320, 220)
        dialog.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
        dialog.layout = BorderLayout(10, 10)
        dialog.isResizable = false
        
        // === Main content panel with padding ===
        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder(15, 15, 15, 15)
        }
        
        // === Icon (centered vertically) ===
        val icon = FlatSVGIcon("icon/warningDialog.svg", 32, 32) // adjust path
        
        val iconLabel = JLabel(icon).apply {
            alignmentY = Component.CENTER_ALIGNMENT
        }
        
        val messageArea = JTextArea(message).apply {
            font = UIManager.getFont("Label.font")
            lineWrap = true
            wrapStyleWord = true
            isEditable = false
            isOpaque = false
            border = null
            alignmentY = Component.CENTER_ALIGNMENT
        }
        
        val scrollPane = JScrollPane(messageArea).apply {
            border = null
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            //preferredSize = Dimension(220, 20) // adjust height as needed
        }
        contentPanel.add(iconLabel)
        contentPanel.add(Box.createRigidArea(Dimension(10, 0)))
        contentPanel.add(scrollPane)
        
        // === Buttons ===
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            val yesBtn = JButton("Yes")
            val cancelBtn = JButton("Cancel")
            cancelBtn.background = UIManager.getColor("accent_color")
            
            yesBtn.addActionListener {
                onAck?.invoke()
                dialog.dispose()
            }
            cancelBtn.addActionListener {
                onCancel?.invoke()
                dialog.dispose()
            }
            
            dialog.addWindowListener(object : java.awt.event.WindowAdapter() {
                override fun windowClosing(e: java.awt.event.WindowEvent) {
                    onCancel?.invoke()
                }
            })
            
            add(yesBtn)
            add(cancelBtn)
        }
        
        dialog.add(contentPanel, BorderLayout.CENTER)
        dialog.add(buttonPanel, BorderLayout.SOUTH)
        dialog.pack()
        dialog.setLocationRelativeTo(parent)
        dialog.isVisible = true
    }
    
    
    companion object {
        fun confirm(msg: String): Alert {
            return Alert().confirm(msg)
        }
    }
}
