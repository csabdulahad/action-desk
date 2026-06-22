package net.abdulahad.action_desk.engine.adcd

import net.abdulahad.action_desk.App
import net.abdulahad.action_desk.data.Env
import net.abdulahad.action_desk.lib.util.Poth
import java.io.BufferedInputStream
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

object DialogSound {
	
	private const val EXTENSION = "wav"
	private val noSoundValues = setOf("", "none", "off", "no", "false", "0", "null")
	
	fun play(sound: DialogSoundSpec?) {
		val src = sound?.src?.trim() ?: return
		
		if (src.lowercase() in noSoundValues) {
			return
		}
		
		Thread {
			try {
				playResolved(src)
			} catch (e: Exception) {
				App.logWarn("ADCD Dialog: failed to play sound: $src")
				App.logWarn(e.message ?: e.toString())
			}
		}.apply {
			name = "ADCD Dialog Sound"
			isDaemon = true
			start()
		}
	}
	
	private fun playResolved(src: String) {
		val source = resolve(src)
		
		if (source == null) {
			App.logWarn("ADCD Dialog: sound not found: $src")
			return
		}
		
		source.openAudioStream().use { audioStream ->
			val clip = AudioSystem.getClip()
			val done = CountDownLatch(1)
			
			clip.addLineListener { event ->
				if (
					event.type == LineEvent.Type.STOP ||
					event.type == LineEvent.Type.CLOSE
				) {
					done.countDown()
				}
			}
			
			clip.use { clip ->
				clip.open(audioStream)
				clip.start()
				
				val timeoutMs = (clip.microsecondLength / 1000L)
					.coerceAtLeast(250L) + 1000L
				
				done.await(timeoutMs, TimeUnit.MILLISECONDS)
			}
		}
	}
	
	private fun resolve(src: String): SoundSource? {
		val ref = src.trim()
		
		if (ref.isBlank()) {
			return null
		}
		
		val file = File(ref)
		
		if (file.isAbsolute) {
			return FileSoundSource(file).takeIf { it.exists() }
		}
		
		val cleanRef = ref
			.replace("\\", "/")
			.trimStart('/')
		
		if (cleanRef.equals("default", ignoreCase = true)) {
			return resolveNamed("default") ?: resolveNamed("info")
		}
		
		if (hasWavExtension(cleanRef)) {
			return resolveWithExtension(cleanRef)
		}
		
		return resolveNamed(cleanRef)
	}
	
	private fun resolveNamed(name: String): SoundSource? {
		return resolveWithExtension("$name.$EXTENSION")
	}
	
	private fun resolveWithExtension(path: String): SoundSource? {
		val appSoundFile = File("${Env.APP_FOLDER}/sound/$path")
		if (appSoundFile.exists() && appSoundFile.isFile) {
			return FileSoundSource(appSoundFile)
		}
		
		if (Poth.resourceExists(path)) {
			return ResourceSoundSource(path)
		}
		
		val resourcePath = "sound/$path"
		if (Poth.resourceExists(resourcePath)) {
			return ResourceSoundSource(resourcePath)
		}
		
		return null
	}
	
	private fun hasWavExtension(path: String): Boolean {
		return path
			.substringAfterLast('/', path)
			.substringAfterLast('.', "")
			.equals(EXTENSION, ignoreCase = true)
	}
	
	private sealed interface SoundSource {
		fun exists(): Boolean
		fun openAudioStream(): AudioInputStream
	}
	
	private class FileSoundSource(
		private val file: File
	) : SoundSource {
		override fun exists(): Boolean {
			return file.exists() && file.isFile && file.extension.equals(EXTENSION, ignoreCase = true)
		}
		
		override fun openAudioStream(): AudioInputStream {
			return AudioSystem.getAudioInputStream(file)
		}
	}
	
	private class ResourceSoundSource(
		private val resourcePath: String
	) : SoundSource {
		override fun exists(): Boolean {
			return Poth.resourceExists(resourcePath) && hasWavExtension(resourcePath)
		}
		
		override fun openAudioStream(): AudioInputStream {
			val input = Poth.getAsStream(resourcePath)
				?: throw IllegalStateException("Sound resource not found: $resourcePath")
			
			return AudioSystem.getAudioInputStream(BufferedInputStream(input))
		}
	}
	
}
