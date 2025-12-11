package net.abdulahad.action_desk.data

object AppValues {
	
	val START_UP_FOLDER: String
		get() {
			return "${Env.getUserHomeDir()}/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup"
		}
	
	val CREATE_SHORTCUT_LNK : String
		get() {
			return "${Env.APP_FOLDER}/scripts/shortcut_lnk.ps1"
		}
	
	val ACTION_DESK_ICON_ICO = "${Env.APP_FOLDER}/action_desk.ico"
	
	val ACTION_DESK_LNK = "${Env.APP_FOLDER}/action_desk.lnk"
}