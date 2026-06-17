package net.abdulahad.action_desk.engine.shortcut

import java.awt.event.KeyEvent

object ShortcutParser {
    
    /**
     * Win32 Modifier Constants for RegisterHotKey
     */
    const val MOD_ALT       = 0x0001
    const val MOD_CONTROL   = 0x0002
    const val MOD_SHIFT     = 0x0004
    const val MOD_WIN       = 0x0008
    
    fun getModifiers(combo: String): Int {
        var mod = 0
        val parts = combo.uppercase().split("+")
        
        if (parts.contains("CTRL"))  mod = mod or MOD_CONTROL
        if (parts.contains("ALT"))   mod = mod or MOD_ALT
        if (parts.contains("SHIFT")) mod = mod or MOD_SHIFT
        if (parts.contains("WIN"))   mod = mod or MOD_WIN
        
        return mod
    }
    
    fun getKeyCode(combo: String): Int {
        // We take the last part (the actual key) and remove spaces
        // This handles "Back[space]Slash" as "BACKSLASH"
		return when (val keyPart = combo.split("+").last().uppercase().replace(" ", "").trim()) {
            // Symbols (Win32 VK Codes)
            "BACKSLASH"    -> 0xDC // VK_OEM_5
            "BACKQUOTE"    -> 0xC0 // VK_OEM_3
            "BACKTICK"     -> 0xC0 // VK_OEM_3
            "MINUS"        -> 0xBD // VK_OEM_MINUS
            "EQUALS"       -> 0xBB // VK_OEM_PLUS
            "OPENBRACKET"  -> 0xDB // VK_OEM_4
            "CLOSEBRACKET" -> 0xDD // VK_OEM_6
            "SEMICOLON"    -> 0xBA // VK_OEM_1
            "QUOTE"        -> 0xDE // VK_OEM_7
            "COMMA"        -> 0xBC // VK_OEM_COMMA
            "PERIOD"       -> 0xBE // VK_OEM_PERIOD
            "SLASH"        -> 0xBF // VK_OEM_2
            "SPACE"        -> 0x20 // VK_SPACE
            "ENTER"        -> 0x0D // VK_RETURN
            
            // Numpad
            "NUMPAD-0"     -> 0x60
            "NUMPAD-1"     -> 0x61
            "NUMPAD-2"     -> 0x62
            "NUMPAD-3"     -> 0x63
            "NUMPAD-4"     -> 0x64
            "NUMPAD-5"     -> 0x65
            "NUMPAD-6"     -> 0x66
            "NUMPAD-7"     -> 0x67
            "NUMPAD-8"     -> 0x68
            "NUMPAD-9"     -> 0x69
            "NUMPAD/"      -> 0x6F
            "NUMPAD+"      -> 0x6B
            "NUMPAD-"      -> 0x6D
            "NUMPAD*"      -> 0x6A
            
            // Navigation & Edit
            "INSERT"       -> 0x2D
            "HOME"         -> 0x24
            "END"          -> 0x23
            "PAGEUP"       -> 0x21
            "PAGEDOWN"     -> 0x22
            "UP"           -> 0x26
            "DOWN"         -> 0x28
            "LEFT"         -> 0x25
            "RIGHT"        -> 0x27
            
            // Function Keys
            "F1"           -> 0x70
            "F2"           -> 0x71
            "F3"           -> 0x72
            "F4"           -> 0x73
            "F5"           -> 0x74
            "F6"           -> 0x75
            "F7"           -> 0x76
            "F8"           -> 0x77
            "F9"           -> 0x78
            "F10"          -> 0x79
            "F11"          -> 0x7A
            "F12"          -> 0x7B
            
            "CONTEXTMENU"  -> 0x5D
            
            else -> {
                // If it's a single character like 'A' or '1',
                // AWT's code matches Windows VK code perfectly.
                if (keyPart.length == 1) {
                    KeyEvent.getExtendedKeyCodeForChar(keyPart[0].code)
                } else {
                    0
                }
            }
        }
    }
    
}