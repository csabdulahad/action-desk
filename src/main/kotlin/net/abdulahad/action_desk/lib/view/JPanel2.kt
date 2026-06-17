package net.abdulahad.action_desk.lib.view

import java.awt.LayoutManager
import javax.swing.JPanel

open class JPanel2 : JPanel {
	var tag1: String = ""
	var tag2: String = ""
	
	constructor() : super()
	constructor(layout: LayoutManager) : super(layout)
}
