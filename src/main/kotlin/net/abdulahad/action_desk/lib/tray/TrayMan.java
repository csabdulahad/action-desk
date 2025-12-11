package net.abdulahad.action_desk.lib.tray;

import dorkbox.systemTray.SystemTray;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashMap;

public abstract class TrayMan {

	static HashMap<String, Tray> trayList = new HashMap<>();

	@Nullable
	public static Tray findTray(String trayId) {
		return trayList.get(trayId);
	}

	public static SystemTray findSystemTray(String trayId) {
		var tray = trayList.get(trayId);
		if (tray == null) return null;

		return tray.systemTray;
	}

	public static boolean uninstall(String trayId) {
		var tray = trayList.remove(trayId);
		if (tray == null) return false;

		tray.systemTray.remove();

		return true;
	}

	public static boolean install(Class<? extends Tray> trayClass) {
		try {
			var tray = trayClass.getDeclaredConstructor().newInstance();
			tray.registerItem();
			tray.install();
			tray.postInstall();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void showAboveTaskbar(Component component, int offsetRight, int offsetBottom) {
		Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int x = screenBounds.width - component.getWidth() - offsetRight;
		int y = screenBounds.height - component.getHeight() - offsetBottom;

		component.setLocation(x, y);
	}

	public static void showAboveTaskbar(Component component) {
		showAboveTaskbar(component, 4, 4);
	}
	
	public static void reinstall(String id, Class<? extends Tray> trayClass) {
		uninstall(id);
		install(trayClass);
	}
}
