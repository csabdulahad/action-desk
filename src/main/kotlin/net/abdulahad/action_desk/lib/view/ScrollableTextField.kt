package net.abdulahad.action_desk.lib.view

import com.formdev.flatlaf.extras.components.FlatTextField
import java.awt.Insets
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

class ScrollableTextField(private val applyIconFix: Boolean = false) : FlatTextField() {
    
    init {
        addMouseWheelListener(HorizontalScrollListener())
    }
    
    private inner class HorizontalScrollListener : MouseWheelListener {
        override fun mouseWheelMoved(e: MouseWheelEvent) {
            if (e.isShiftDown) {
                val field = this@ScrollableTextField
                val currentOffset = field.scrollOffset
                val scrollAmount = e.unitsToScroll * 10
                val newOffset = (currentOffset + scrollAmount).coerceAtLeast(0)
                field.scrollOffset = newOffset
                e.consume()
            }
        }
    }
    
    override fun paste() {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val contents = clipboard.getContents(null)
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                val text = contents.getTransferData(DataFlavor.stringFlavor) as? String
                text?.let {
                    replaceSelection(it)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    
    override fun getInsets(): Insets {
        val insets = super.getInsets()
        
        if (!applyIconFix) return insets
        
        // Add extra left padding (e.g. 8 px) to shift icon + text right
        return Insets(insets.top + 1, insets.left + 8, insets.bottom, insets.right)
    }
}
