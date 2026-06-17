package net.abdulahad.action_desk.view

interface ActionDeskPanel {
	fun onSearch(term: String)
	fun refresh()
	fun repaintList()
}