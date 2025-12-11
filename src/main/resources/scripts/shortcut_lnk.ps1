function Create-Shortcut {
    param (
        [string]$ShortcutPath,
        [string]$TargetPath,
        [string]$Arguments = "",
        [string]$StartIn = "",
        [string]$ShortcutKey = "",
        [ValidateSet("Normal", "Minimized", "Maximized")]
        [string]$WindowState = "Normal",
        [string]$IconLocation = "",
        [switch]$RunAsAdmin = $false
    )

    # Validate shortcut path
    if (-not $ShortcutPath.EndsWith(".lnk")) {
        #Write-Host "Error: ShortcutPath must end with '.lnk'" -ForegroundColor Red
        #return
    }

    # Create a WScript Shell COM Object
    $WScriptShell = New-Object -ComObject WScript.Shell

    # Create a new shortcut object
    $Shortcut = $WScriptShell.CreateShortcut($ShortcutPath)

    # Set properties
    $Shortcut.TargetPath = $TargetPath  # The executable path
    $Shortcut.Arguments = $Arguments    # Arguments for the executable
    $Shortcut.WorkingDirectory = $StartIn  # "Start in" directory

    # Set window state
    switch ($WindowState.ToLower()) {
        "minimized" { $Shortcut.WindowStyle = 7 }
        "maximized" { $Shortcut.WindowStyle = 3 }
        default { $Shortcut.WindowStyle = 1 }  # Normal
    }

    # Set the shortcut key (Hotkey)
    if ($ShortcutKey) { $Shortcut.Hotkey = $ShortcutKey }

    # Set the icon if provided
    if ($IconLocation) { $Shortcut.IconLocation = $IconLocation }

    # Save the shortcut
    $Shortcut.Save()

    # Enable "Run as Administrator" if requested
    if ($RunAsAdmin) {
        $bytes = [System.IO.File]::ReadAllBytes($ShortcutPath)
        $bytes[0x15] = $bytes[0x15] -bor 0x20  # Modify the shortcut flag to request admin rights
        [System.IO.File]::WriteAllBytes($ShortcutPath, $bytes)
    }
}