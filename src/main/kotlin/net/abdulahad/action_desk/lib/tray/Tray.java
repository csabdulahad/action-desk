package net.abdulahad.action_desk.lib.tray;

import com.formdev.flatlaf.extras.FlatSVGUtils;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import net.abdulahad.action_desk.lib.util.Poth;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

public abstract class Tray {

	String id;
	String icon;
	SystemTray systemTray;

	ArrayList<TrayItem> trayItems;

	public Tray(String id, String icon) {
		this.id = id;
		this.icon = icon;

		trayItems = new ArrayList<>();
	}

	protected void registerItem() {
	}

	@Nullable
	public TrayItem findTrayItem(String trayId) {
		for (TrayItem trayItem : trayItems) {
			if (trayItem.id.equals(trayId)) {
				return trayItem;
			}
		}

		return null;
	}

	@Nullable
	public MenuItem findMenuItem(String trayId) {
		var trayItem = findTrayItem(trayId);
		if (trayItem == null) return null;

		return trayItem.getMenu();
	}

	public void addMenu(TrayItem... trayItem) {
		for (TrayItem item : trayItem) {
			trayItems.add(item);

			if (systemTray == null) continue;

			systemTray.getMenu().add(item.getMenu());
		}
	}

	public void disableMenu(TrayItem trayItem) {
		trayItem.getMenu().setEnabled(false);
	}

	public void enableMenu(TrayItem trayItem) {
		trayItem.getMenu().setEnabled(true);
	}

	public void disableMenu(String menuID) {
		var item = findTrayItem(menuID);
		if (item == null) return;

		MenuItem menu = item.getMenu();
		menu.setEnabled(false);
	}

	public void enableMenu(String menuID) {
		var item = findTrayItem(menuID);
		if (item == null) return;

		item.getMenu().setEnabled(true);
	}

	public void removeMenu(TrayItem trayItem) {
		if (trayItem == null) return;

		trayItems.remove(trayItem);
		systemTray.getMenu().remove(trayItem.getMenu());
	}

	public void removeMenu(String trayId) {
		var trayItem = findTrayItem(trayId);
		removeMenu(trayItem);
	}

	public void install() {
		if (TrayMan.trayList.containsKey(id)) return;

		TrayMan.trayList.put(id, this);
 
		SystemTray.FORCE_GTK2 = true;
		
		systemTray = SystemTray.get(id);
		
		if (icon.endsWith(".svg")) {
			systemTray.setImage(FlatSVGUtils.svg2image(Poth.INSTANCE.getURL(icon), 32, 32));
		} else {
			systemTray.setImage(Poth.INSTANCE.getURL(icon));
		}
		

		var menu = systemTray.getMenu();
		
		for (TrayItem item : trayItems) {
			if (item.hasSeparatorAbove()) {
				menu.add(new JSeparator());
			}

			menu.add(item.getMenu());
		}
		
	}

	public void uninstall() {
		TrayMan.uninstall(id);
	}

	public void setImage(String path) {
		if (systemTray == null) return;

		systemTray.setImage(Poth.INSTANCE.getURL(path));
	}

	public SystemTray getSystemTray() {
		return systemTray;
	}
	
	public void postInstall() {}

	public void setStatus(String status) {
		systemTray.setStatus(status);
	}
	
}
