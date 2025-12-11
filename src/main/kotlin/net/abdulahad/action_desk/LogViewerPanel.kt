package net.abdulahad.action_desk

import java.awt.BorderLayout
import java.awt.Font
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.JTree
import javax.swing.border.EmptyBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class LogViewerPanel : JPanel(BorderLayout()) {

    private val treeModel = DefaultTreeModel(DefaultMutableTreeNode("Logs"))
    private val logTree = JTree(treeModel)
    private val logTextArea = JTextArea().apply {
        font = Font("JetBrains Mono", Font.PLAIN, 13)
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    }

    init {
        logTree.apply {
            isRootVisible = false
            showsRootHandles = true
            border = EmptyBorder(5, 5, 5, 5)
            addTreeSelectionListener {
                val node = lastSelectedPathComponent as? DefaultMutableTreeNode
                val logId = node?.userObject as? LogReference
                logId?.let { loadLogContent(it) }
            }
        }

        val scrollText = JScrollPane(logTextArea)
        val scrollTree = JScrollPane(logTree)

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTree, scrollText).apply {
            resizeWeight = 0.3
            border = BorderFactory.createEmptyBorder()
            dividerSize = 4
        }

        add(splitPane, BorderLayout.CENTER)
        populateTreeWithLogs()
    }

    private fun loadLogContent(logRef: LogReference) {
        val content = File("c:/users/ahad/desktop/test.log").readText()
        logTextArea.text = content
    }

    private fun populateTreeWithLogs() {
        val root = treeModel.root as DefaultMutableTreeNode
        root.removeAllChildren()

        val logs = loadLogsGroupedByDate()
        logs.forEach { (date, entries) ->
            val dateNode = DefaultMutableTreeNode(date)
            entries.forEach { entry ->
                dateNode.add(DefaultMutableTreeNode(entry))
            }
            root.add(dateNode)
        }

        treeModel.reload()
    }

    data class LogReference(val time: String, val path: String) {
        override fun toString() = time
    }

    fun loadLogsGroupedByDate(): Map<String, List<LogReference>> {
        // Replace with actual loading logic
        return mapOf(
            "21 May 2025" to listOf(
                LogReference("12:00:32", "logs/2025-05-21_12-00-32.log"),
                LogReference("13:12:23", "logs/2025-05-21_13-12-23.log")
            ),
            "31 Aug 2025" to listOf(
                LogReference("19:00:00", "logs/2025-08-31_19-00-00.log")
            )
        )
    }
}
