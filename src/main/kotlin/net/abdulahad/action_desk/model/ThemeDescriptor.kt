package net.abdulahad.action_desk.model

import com.formdev.flatlaf.*
import com.formdev.flatlaf.themes.FlatMacDarkLaf
import kotlin.reflect.KClass

data class ThemeDescriptor (
    val displayName: String,
    val classRef: KClass<out FlatLaf>
) {
    
    val className: String
        get() = classRef.java.name
    
    fun createInstance(): FlatLaf {
        return classRef.java.getDeclaredConstructor().newInstance()
    }
    
    companion object {
        
        private var themes: List<ThemeDescriptor> = listOf (
			ThemeDescriptor("light", FlatIntelliJLaf::class),
			ThemeDescriptor("dark", FlatMacDarkLaf::class),
		)
  
		fun getByClassName(className: String): ThemeDescriptor {
            return themes.find { it.className == className } ?: themes.last()
        }
        
        fun getByThemeName(name: String): ThemeDescriptor {
            return themes.find { it.displayName == name } ?: themes.last()
        }
        
    }
    
}