package net.abdulahad.action_desk.lib

import java.awt.Component
import java.awt.Container
import java.security.MessageDigest

inline fun <reified T : Component> Container.findByName(name: String): T? {
	return findByNameTypedInternal(name, T::class.java)
}

fun <T : Component> Container.findByNameTypedInternal(name: String, clazz: Class<T>): T? {
	for (child in this.components) {
		if (clazz.isInstance(child) && child.name == name) return clazz.cast(child)
		if (child is Container) {
			child.findByNameTypedInternal(name, clazz)?.let { return it }
		}
	}
	
	return null
}

fun String.md5(): String {
	return MessageDigest.getInstance("MD5")
		.digest(this.toByteArray())
		.joinToString("") { "%02x".format(it) }
}