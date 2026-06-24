package net.abdulahad.action_desk.view.action_editor.panel

import com.formdev.flatlaf.extras.components.FlatCheckBox
import net.abdulahad.action_desk.lib.json.Json
import net.abdulahad.action_desk.model.Action
import net.abdulahad.action_desk.model.ActionSchedule
import net.abdulahad.action_desk.repo.action_schedule.ActionScheduleDao
import net.abdulahad.action_desk.view.action_editor.ActionEditorPanel
import org.jdesktop.swingx.HorizontalLayout
import org.jdesktop.swingx.VerticalLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class SchedulePanel : JPanel(), ActionEditorPanel {
	
	private val enableScheduleCheckbox = FlatCheckBox()
	
	private val repeatRadio = JRadioButton("Repeat")
	private val dailyRadio = JRadioButton("Daily")
	private val weeklyRadio = JRadioButton("Weekly")
	private val scheduleTypeGroup = ButtonGroup()
	
	private val repeatMinutesSpinner = JSpinner(SpinnerNumberModel(15, 1, 1440, 1))
	private val dailyTimesPanel = ScheduleTimesPanel(defaultTime = "", addButtonText = "+ Add time")
	private val weeklyTimesPanel = ScheduleTimesPanel(defaultTime = "", addButtonText = "+ Add time")
	private val weeklySharedTimesLabelPanel = JPanel(HorizontalLayout(8)).apply {
		add(JLabel("shared times"))
	}
	private val weeklyCustomTimesLabelPanel = JPanel(HorizontalLayout(8)).apply {
		add(JLabel("custom times by day"))
	}
	private val weeklyCustomTimesRowsPanel = JPanel(VerticalLayout(8))
	private val weeklyCustomTimesPanels = mutableMapOf<String, ScheduleTimesPanel>()
	
	private val dayOptions = listOf(
		"MON" to "Mon",
		"TUE" to "Tue",
		"WED" to "Wed",
		"THU" to "Thu",
		"FRI" to "Fri",
		"SAT" to "Sat",
		"SUN" to "Sun",
	)
	
	private val dayCheckBoxes = dayOptions.associate { (value, label) ->
		value to FlatCheckBox().apply {
			text = label
			preferredSize = Dimension(72, preferredSize.height)
			minimumSize = preferredSize
		}
	}
	
	private var existingSchedule: ActionSchedule? = null
	
	init {
		setupPanel()
		setupControls()
		addFields()
		toggleScheduleFields()
	}
	
	private fun setupPanel() {
		layout = VerticalLayout(10)
		border = BorderFactory.createEmptyBorder()
	}
	
	private fun setupControls() {
		enableScheduleCheckbox.text = "Enable schedule"
		enableScheduleCheckbox.addActionListener { toggleScheduleFields() }
		
		scheduleTypeGroup.add(repeatRadio)
		scheduleTypeGroup.add(dailyRadio)
		scheduleTypeGroup.add(weeklyRadio)
		repeatRadio.isSelected = true
		
		repeatRadio.addActionListener { toggleScheduleFields() }
		dailyRadio.addActionListener { toggleScheduleFields() }
		weeklyRadio.addActionListener { toggleScheduleFields() }
		
		dayCheckBoxes.values.forEach { checkBox ->
			checkBox.addActionListener {
				refreshWeeklyCustomTimeRows()
				toggleScheduleFields()
			}
		}
	}
	
	private fun addFields() {
		add(enableScheduleCheckbox)
		add(repeatPanel())
		add(dailyPanel())
		add(weeklyPanel())
	}
	
	private fun repeatPanel(): JPanel {
		return JPanel(HorizontalLayout(8)).apply {
			add(repeatRadio)
			add(JLabel("every"))
			add(repeatMinutesSpinner)
			add(JLabel("minutes"))
		}
	}
	
	private fun dailyPanel(): JPanel {
		return JPanel(VerticalLayout(6)).apply {
			add(JPanel(HorizontalLayout(8)).apply {
				add(dailyRadio)
				add(JLabel("at these times"))
			})
			add(dailyTimesPanel)
		}
	}
	
	private fun weeklyPanel(): JPanel {
		return JPanel(VerticalLayout(6)).apply {
			add(JPanel(HorizontalLayout(8)).apply {
				add(weeklyRadio)
				add(JLabel("on selected days"))
			})
			add(weekdayRowsPanel())
			add(weeklySharedTimesLabelPanel)
			add(weeklyTimesPanel)
			add(weeklyCustomTimesLabelPanel)
			add(weeklyCustomTimesRowsPanel)
		}
	}
	
	private fun weekdayRowsPanel(): JPanel {
		return JPanel(VerticalLayout(4)).apply {
			add(weekdayRow(listOf("MON", "TUE", "WED", "THU")))
			add(weekdayRow(listOf("FRI", "SAT", "SUN")))
		}
	}
	
	private fun weekdayRow(days: List<String>): JPanel {
		return JPanel(GridLayout(1, 4, 8, 0)).apply {
			days.forEach { value ->
				add(dayCheckBoxes.getValue(value))
			}
			repeat(4 - days.size) {
				add(JPanel())
			}
		}
	}
	
	private fun toggleScheduleFields() {
		val enabled = enableScheduleCheckbox.isSelected
		val weeklyEnabled = enabled && weeklyRadio.isSelected
		val showWeeklyTimeSections = weeklyRadio.isSelected && selectedWeekdays().isNotEmpty()
		
		repeatRadio.isEnabled = enabled
		dailyRadio.isEnabled = enabled
		weeklyRadio.isEnabled = enabled
		
		repeatMinutesSpinner.isEnabled = enabled && repeatRadio.isSelected
		dailyTimesPanel.isEnabled = enabled && dailyRadio.isSelected
		weeklyTimesPanel.isEnabled = weeklyEnabled && showWeeklyTimeSections
		
		weeklySharedTimesLabelPanel.isVisible = showWeeklyTimeSections
		weeklyTimesPanel.isVisible = showWeeklyTimeSections
		weeklyCustomTimesLabelPanel.isVisible = showWeeklyTimeSections
		weeklyCustomTimesRowsPanel.isVisible = showWeeklyTimeSections
		
		dayCheckBoxes.values.forEach { checkBox ->
			checkBox.isEnabled = weeklyEnabled
		}
		
		weeklyCustomTimesRowsPanel.isEnabled = weeklyEnabled && showWeeklyTimeSections
		weeklyCustomTimesPanels.values.forEach { panel ->
			panel.isEnabled = weeklyEnabled && showWeeklyTimeSections
		}
		
		revalidate()
		repaint()
	}
	
	override fun save(action: Action): String? {
		if (!enableScheduleCheckbox.isSelected) return null
		
		if (repeatRadio.isSelected) {
			val minutes = (repeatMinutesSpinner.value as Number).toInt()
			
			if (minutes < 1) {
				return "Schedule:Repeat minutes must be at least 1"
			}
		}
		
		if (dailyRadio.isSelected) {
			val error = dailyTimesPanel.validateTimes("Daily times")
			
			if (error != null) {
				return "Schedule:$error"
			}
		}
		
		if (weeklyRadio.isSelected) {
			val selectedDays = selectedWeekdays()
			
			if (selectedDays.isEmpty()) {
				return "Schedule:Select at least one weekly day"
			}
			
			val sharedError = weeklyTimesPanel.validateTimes("Weekly shared times", required = false)
			
			if (sharedError != null) {
				return "Schedule:$sharedError"
			}
			
			val customError = validateWeeklyCustomTimes(selectedDays)
			
			if (customError != null) {
				return "Schedule:$customError"
			}
			
			if (!hasWeeklyTimes(selectedDays)) {
				return "Schedule:Add at least one weekly time"
			}
			
			val duplicateError = validateMergedWeeklyTimes(selectedDays)
			
			if (duplicateError != null) {
				return "Schedule:$duplicateError"
			}
		}
		
		return null
	}
	
	override fun setData(action: Action) {
		if (action.id == 0) {
			setDefaultScheduleView()
			return
		}
		
		existingSchedule = ActionScheduleDao.fetchByActionId(action.id)
		val schedule = existingSchedule
		
		if (schedule == null) {
			setDefaultScheduleView()
			return
		}
		
		enableScheduleCheckbox.isSelected = schedule.enabled
		
		when (schedule.type.lowercase()) {
			ActionSchedule.TYPE_CALENDAR -> setCalendarSchedule(schedule)
			else -> setRepeatSchedule(schedule)
		}
		
		toggleScheduleFields()
	}
	
	fun persistForAction(action: Action) {
		if (action.id == 0) return
		
		if (!enableScheduleCheckbox.isSelected && existingSchedule == null) {
			return
		}
		
		val schedule = buildSchedule(action.id)
		val id = ActionScheduleDao.save(schedule, recalculateNextRunAt = true)
		
		if (id > 0) {
			schedule.id = id
			existingSchedule = schedule
		}
	}
	
	private fun buildSchedule(actionId: Int): ActionSchedule {
		val schedule = existingSchedule ?: ActionSchedule(actionId = actionId)
		schedule.actionId = actionId
		schedule.enabled = enableScheduleCheckbox.isSelected
		
		if (!schedule.enabled) {
			schedule.nextRunAt = null
			return schedule
		}
		
		when {
			dailyRadio.isSelected -> applyDailySchedule(schedule)
			weeklyRadio.isSelected -> applyWeeklySchedule(schedule)
			else -> applyRepeatSchedule(schedule)
		}
		
		return schedule
	}
	
	private fun applyRepeatSchedule(schedule: ActionSchedule) {
		schedule.type = ActionSchedule.TYPE_REPEAT
		schedule.repeatEveryMinutes = (repeatMinutesSpinner.value as Number).toInt()
		schedule.dayRuleType = null
		schedule.daysOfWeekJson = null
		schedule.daysOfMonthJson = null
		schedule.weekPosition = null
		schedule.weekday = null
		schedule.timesJson = null
		schedule.dayTimesJson = null
	}
	
	private fun applyDailySchedule(schedule: ActionSchedule) {
		val times = dailyTimesPanel.getNormalizedTimes()
		
		schedule.type = ActionSchedule.TYPE_CALENDAR
		schedule.repeatEveryMinutes = null
		schedule.dayRuleType = ActionSchedule.DAY_RULE_DAILY
		schedule.daysOfWeekJson = null
		schedule.daysOfMonthJson = null
		schedule.weekPosition = null
		schedule.weekday = null
		schedule.timesJson = Json.stringify(times)
		schedule.dayTimesJson = null
	}
	
	private fun applyWeeklySchedule(schedule: ActionSchedule) {
		val days = selectedWeekdays()
		val sharedTimes = weeklyTimesPanel.getNormalizedTimes()
		val customTimes = selectedDayTimes(days)
		
		schedule.type = ActionSchedule.TYPE_CALENDAR
		schedule.repeatEveryMinutes = null
		schedule.dayRuleType = ActionSchedule.DAY_RULE_WEEKLY
		schedule.daysOfWeekJson = Json.stringify(days)
		schedule.daysOfMonthJson = null
		schedule.weekPosition = null
		schedule.weekday = null
		schedule.timesJson = Json.stringify(sharedTimes)
		schedule.dayTimesJson = if (customTimes.isEmpty()) null else Json.stringify(customTimes)
	}
	
	private fun setDefaultScheduleView() {
		existingSchedule = null
		enableScheduleCheckbox.isSelected = false
		repeatRadio.isSelected = true
		repeatMinutesSpinner.value = 15
		dailyTimesPanel.setTimes(listOf(""))
		weeklyTimesPanel.setTimes(listOf(""))
		setSelectedWeekdays(emptyList())
		setWeeklyCustomTimes(emptyMap())
		toggleScheduleFields()
	}
	
	private fun setRepeatSchedule(schedule: ActionSchedule) {
		repeatRadio.isSelected = true
		repeatMinutesSpinner.value = schedule.repeatEveryMinutes ?: 15
		dailyTimesPanel.setTimes(listOf(""))
		weeklyTimesPanel.setTimes(listOf(""))
		setSelectedWeekdays(emptyList())
		setWeeklyCustomTimes(emptyMap())
	}
	
	private fun setCalendarSchedule(schedule: ActionSchedule) {
		when (schedule.dayRuleType) {
			ActionSchedule.DAY_RULE_WEEKLY -> {
				weeklyRadio.isSelected = true
				dailyTimesPanel.setTimes(listOf(""))
				weeklyTimesPanel.setTimes(scheduleTimes(schedule).ifEmpty { listOf("") })
				setSelectedWeekdays(scheduleDays(schedule))
				setWeeklyCustomTimes(scheduleDayTimes(schedule))
			}
			else -> {
				dailyRadio.isSelected = true
				dailyTimesPanel.setTimes(scheduleTimes(schedule).ifEmpty { listOf("") })
				weeklyTimesPanel.setTimes(listOf(""))
				setSelectedWeekdays(emptyList())
				setWeeklyCustomTimes(emptyMap())
			}
		}
	}
	
	private fun selectedWeekdays(): List<String> {
		return dayOptions
			.map { it.first }
			.filter { value -> dayCheckBoxes.getValue(value).isSelected }
	}
	
	private fun setSelectedWeekdays(days: List<String>) {
		val selected = days.map { it.uppercase() }.toSet()
		
		dayOptions.forEach { (value, _) ->
			dayCheckBoxes.getValue(value).isSelected = selected.contains(value)
		}
		
		refreshWeeklyCustomTimeRows()
	}
	
	private fun selectedDayTimes(days: List<String>): Map<String, List<String>> {
		return days
			.mapNotNull { day ->
				val times = weeklyCustomTimesPanelForDay(day).getNormalizedTimes()
				if (times.isEmpty()) null else day to times
			}
			.toMap()
	}
	
	private fun hasWeeklyTimes(days: List<String>): Boolean {
		if (weeklyTimesPanel.getNormalizedTimes().isNotEmpty()) {
			return true
		}
		
		return days.any { day ->
			weeklyCustomTimesPanelForDay(day).getNormalizedTimes().isNotEmpty()
		}
	}
	
	private fun validateWeeklyCustomTimes(days: List<String>): String? {
		for (day in days) {
			val error = weeklyCustomTimesPanelForDay(day)
				.validateTimes("${dayLabel(day)} custom times", required = false)
			
			if (error != null) {
				return error
			}
		}
		
		return null
	}
	
	private fun validateMergedWeeklyTimes(days: List<String>): String? {
		val sharedTimes = weeklyTimesPanel.getNormalizedTimes()
		
		for (day in days) {
			val seen = mutableSetOf<String>()
			val customTimes = weeklyCustomTimesPanelForDay(day).getNormalizedTimes()
			
			(sharedTimes + customTimes).forEach { time ->
				if (!seen.add(time)) {
					return "${dayLabel(day)} has duplicate time $time"
				}
			}
		}
		
		return null
	}
	
	private fun setWeeklyCustomTimes(dayTimes: Map<String, List<String>>) {
		weeklyCustomTimesPanels.values.forEach { panel ->
			panel.setTimes(listOf(""))
		}
		
		dayTimes.forEach { (day, times) ->
			val normalizedDay = day.uppercase()
			
			if (dayOptions.any { it.first == normalizedDay }) {
				weeklyCustomTimesPanelForDay(normalizedDay)
					.setTimes(times.ifEmpty { listOf("") })
			}
		}
		
		refreshWeeklyCustomTimeRows()
	}
	
	private fun refreshWeeklyCustomTimeRows() {
		weeklyCustomTimesRowsPanel.removeAll()
		
		selectedWeekdays().forEach { day ->
			weeklyCustomTimesRowsPanel.add(JPanel(VerticalLayout(4)).apply {
				add(JLabel("${dayLabel(day)} custom times"))
				add(weeklyCustomTimesPanelForDay(day))
			})
		}
		
		weeklyCustomTimesRowsPanel.revalidate()
		weeklyCustomTimesRowsPanel.repaint()
	}
	
	private fun weeklyCustomTimesPanelForDay(day: String): ScheduleTimesPanel {
		val normalizedDay = day.uppercase()
		
		return weeklyCustomTimesPanels.getOrPut(normalizedDay) {
			ScheduleTimesPanel(defaultTime = "", addButtonText = "+ Add time")
		}
	}
	
	private fun dayLabel(day: String): String {
		val normalizedDay = day.uppercase()
		return dayOptions.firstOrNull { it.first == normalizedDay }?.second ?: normalizedDay
	}
	
	private fun scheduleTimes(schedule: ActionSchedule): List<String> {
		val json = schedule.timesJson ?: return emptyList()
		
		return runCatching {
			Json.parse(json)
				.stringList()
		}.getOrDefault(emptyList())
	}
	
	private fun scheduleDays(schedule: ActionSchedule): List<String> {
		val json = schedule.daysOfWeekJson ?: return emptyList()
		
		return runCatching {
			Json.parse(json)
				.stringList()
				.map { it.uppercase() }
				.filter { day -> dayOptions.any { it.first == day } }
		}.getOrDefault(emptyList())
	}
	
	private fun scheduleDayTimes(schedule: ActionSchedule): Map<String, List<String>> {
		val json = schedule.dayTimesJson ?: return emptyMap()
		
		return runCatching {
			Json.parse(json)
				.map()
				.mapNotNull { (day, value) ->
					val normalizedDay = day.uppercase()
					
					if (dayOptions.none { it.first == normalizedDay }) {
						return@mapNotNull null
					}
					
					val times = value.stringList()
						.map { it.trim() }
						.filter { it.isNotEmpty() }
					
					if (times.isEmpty()) null else normalizedDay to times
				}
				.toMap()
		}.getOrDefault(emptyMap())
	}
	
}
