package net.abdulahad.action_desk.engine.shortcut;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;

public class WinKeyBlocker {
	
    // Hook handle
    private static WinUser.HHOOK hhk;
    private static User32 lib = User32.INSTANCE;

    // Constants for keys
    private static final int WH_KEYBOARD_LL = 13;
    private static final int WM_KEYDOWN = 0x0100;
    private static final int WM_KEYUP = 0x0101;

    // Virtual Key codes
    private static final int VK_LWIN = 0x5B;
    private static final int VK_RWIN = 0x5C;
    // private static final int VK_W = 0x57;
    private static final int VK_W = 0x4B;

    // State of Win key
    private static volatile boolean winPressed = false;

    public static void main() {
        System.out.println("Starting WinKeyBlocker - blocking Win+W...");
        
        // Low-level keyboard procedure callback
        WinUser.LowLevelKeyboardProc keyboardHook = new WinUser.LowLevelKeyboardProc() {
            @Override
            public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
                if (nCode >= 0) {
                    boolean keyDown = (wParam.intValue() == WM_KEYDOWN);

                    int vkCode = info.vkCode;

                    // Track Win key state
                    if (vkCode == VK_LWIN || vkCode == VK_RWIN) {
                        winPressed = keyDown;
                    }

                    // Block Win+W combo
                    if (winPressed && vkCode == VK_W && keyDown) {
                        System.out.println("Blocked Win+W!");
                        // Returning 1 blocks the key event
                        return new LRESULT(1);
                    }
                }
                // Pass event to next hook
                return lib.CallNextHookEx(hhk, nCode, wParam, new LPARAM(Pointer.nativeValue(info.getPointer())));
            }
        };

        // Get module handle (required for SetWindowsHookEx)
        HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);

        // Install the low-level keyboard hook
        hhk = lib.SetWindowsHookEx(WH_KEYBOARD_LL, keyboardHook, hMod, 0);
        if (hhk == null) {
            System.err.println("Failed to install hook");
            System.exit(1);
        }

        // Message loop to keep hook alive
        WinUser.MSG msg = new WinUser.MSG();
        while (lib.GetMessage(msg, null, 0, 0) != 0) {
            lib.TranslateMessage(msg);
            lib.DispatchMessage(msg);
        }

        // Unhook on exit
        lib.UnhookWindowsHookEx(hhk);
    }
	
}