package net.abdulahad.action_desk.job

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.dao.ActionDao
import net.abdulahad.action_desk.data.AppConfig
import net.abdulahad.action_desk.data.AppValues
import net.abdulahad.action_desk.engine.ActionRunner
import java.io.File

object StartupJobs {

    fun runAutoStartActions() {
        CoroutineScope(Dispatchers.Default).launch {
            val actions = ActionDao.fetchAutoRunActions()

            if (actions.isEmpty()) return@launch

            App.logInfo("ActionDesk: starting actions with AD")

            actions.forEach { action ->
                ActionRunner.runAction(action, diagnose = false, bootupRun = true)
            }
        }
    }

    fun validateADAutoStartLink() {
        CoroutineScope(Dispatchers.Default).launch {
            App.logInfo("ActionDesk: validating AD auto start link")

            val autoStart = AppConfig.getAutoRun()
            val shortcut  = "${AppValues.START_UP_FOLDER}/ActionDesk.lnk"
            val exists    = File(shortcut).exists()

            if (!autoStart && exists) {
                AppConfig.applyAutoRun(false)
                return@launch
            }

            if (autoStart && !exists) {
                AppConfig.applyAutoRun(true)
            }
        }
    }

}