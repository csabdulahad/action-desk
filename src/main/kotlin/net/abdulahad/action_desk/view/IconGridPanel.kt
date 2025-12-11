package net.abdulahad.action_desk.view

import com.formdev.flatlaf.ui.FlatLineBorder
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.helper.Icons.icon
import net.abdulahad.action_desk.lib.util.Poth
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Insets
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder

class IconGridPanel(
    dialog: Window,
    private val onIconClick: (String) -> Unit
) : JDialog (dialog) {

    private val iconsPerBatch: Int = 20
    private val iconsPerRow: Int = 5
    
    private var scrollPane: JScrollPane
    private lateinit var iconPanel: JPanel
    
    private lateinit var iconNames: List<String>
    private var loadedCount = 0
    private val total: Int
    
    init {
        title = "Choose an icon"
        modalityType = ModalityType.APPLICATION_MODAL
        isResizable = false
        
        loadIconNames()
        total = iconNames.size
        
        setupGridPanel()
        
        scrollPane = JScrollPane(iconPanel)
        scrollPane.border = EmptyBorder(6, 6, 6, 6)
        scrollPane.preferredSize = Dimension(320, 260)
        scrollPane.verticalScrollBar.unitIncrement = 8
        
        scrollPane.verticalScrollBar.addAdjustmentListener { e ->
            val bar = e.adjustable
            if (!e.valueIsAdjusting &&
                bar.value + bar.visibleAmount >= bar.maximum - 20
            ) {
                if (hasMore()) {
                    loadMoreIcons()
                }
            }
        }
        
        loadMoreIcons()
        
        contentPane.add(scrollPane)
        pack()
        setLocationRelativeTo(parent)
        isVisible = true
    }
    
    private fun loadIconNames() {
        val defaultList  = Poth.getAsStream("icons/default_icons.txt")
            ?. bufferedReader()
            ?. readLines()
            ?: emptyList()
        
        // Read from icons.txt (local filesystem)
        val path = Env.APP_FOLDER + "/icons.txt"
        
        val customList = if (Poth.fileExists(path)) {
            File(path).readLines()
        } else {
            emptyList()
        }
        
        
        // Merge, remove duplicates using set, preserve order using LinkedHashSet
        iconNames = (listOf("empty") + customList + defaultList)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toCollection(LinkedHashSet())
            .toList()
    }
    
    private fun loadMoreIcons() {
        val end = (loadedCount + iconsPerBatch).coerceAtMost(total)
        
        for (i in loadedCount until end) {
            val name = iconNames[i]
            
            val icon = name.icon(28)
            
            val label = JLabel(icon)
            label.isOpaque = true
            label.horizontalAlignment = JLabel.CENTER
            label.verticalAlignment = JLabel.CENTER
            label.border = BorderFactory.createEmptyBorder(6, 6, 6, 6)
            
            // 🔁 Hover effect
            label.addMouseListener(object : MouseAdapter() {
                override fun mouseEntered(e: MouseEvent?) {
                    // label.background = UIManager.getColor("MenuItem.selectionBackground") ?: Color.LIGHT_GRAY
					label.border = MatteBorder(2, 2, 2 ,2, UIManager.getColor("PopupMenu.borderColor"))
                }
                
                override fun mouseExited(e: MouseEvent?) {
					label.border = null
                    // label.background = null
                }
                
                override fun mouseClicked(e: MouseEvent?) {
                    dispose()
                    onIconClick(name)
                }
            })
            
            iconPanel.add(label)
        }
        
        loadedCount = end
        
        revalidate()
        repaint()
    }
    
    private fun hasMore(): Boolean = loadedCount < total
    
    private fun setupGridPanel() {
        iconPanel = JPanel()
        iconPanel.layout = GridLayout(0, iconsPerRow, 6, 6)
    }
    
}