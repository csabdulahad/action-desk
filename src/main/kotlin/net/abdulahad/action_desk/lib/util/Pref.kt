package net.abdulahad.action_desk.lib.util

import java.util.prefs.Preferences

object Pref {
	private val pref: Preferences?
		get() = Preferences.userRoot().node("a2_desk")
	
	fun isSet(key: String?): Boolean {
		return pref!!.get(key, null) != null
	}
	
	fun getString(key: String?, defaultValue: String?): String? {
		return pref!!.get(key, defaultValue)
	}
	
	fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
		return pref!!.getBoolean(key, defaultValue)
	}
	
	fun putBoolean(key: String?, value: Boolean) {
		pref!!.putBoolean(key, value)
	}
	
	fun putString(key: String?, value: String?) {
		pref!!.put(key, value)
	}
	
	fun getInt(key: String?, defaultValue: Int): Int {
		return pref!!.getInt(key, defaultValue)
	}
	
	fun putInt(key: String?, value: Int) {
		pref!!.putInt(key, value)
	}
}