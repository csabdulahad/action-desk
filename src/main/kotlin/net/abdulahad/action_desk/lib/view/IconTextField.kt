package net.abdulahad.action_desk.lib.view

import org.jdesktop.swingx.HorizontalLayout
import javax.swing.Box
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JPanel

class IconTextField(icon: Icon? = null, gap: Int = 8) : JPanel(HorizontalLayout()) {
    
    val textField = ScrollableTextField()
    
    init {
        if (icon != null) {
            add(JLabel(icon))
            add(Box.createHorizontalStrut(gap))
        }
        
        add(textField)
    }
    
}