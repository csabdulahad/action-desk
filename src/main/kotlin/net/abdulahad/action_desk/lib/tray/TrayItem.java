package net.abdulahad.action_desk.lib.tray;

import com.formdev.flatlaf.extras.FlatSVGUtils;
import dorkbox.systemTray.Menu;
import dorkbox.systemTray.MenuItem;
import net.abdulahad.action_desk.lib.util.Poth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TrayItem {

	String id;
	MenuItem menuItem;

	public TrayItem(String id) {
		this.id = id;
	}

	@NotNull
	public MenuItem getMenu() {
		if (menuItem == null) {
			menuItem = prepareMenu();
		}

		return menuItem;
	}

	@NotNull
	public abstract MenuItem prepareMenu();

	public boolean hasSeparatorAbove() {
		return false;
	}

	protected void onClick() {
	}
	
	@SuppressWarnings("SameParameterValue")
	protected Menu newMenu(String title) {
		return newMenu(title, null);
	}
	
	protected MenuItem newMenuItem(String title) {
		return newMenuItem(title, null);
	}
	
	@SuppressWarnings("SameParameterValue")
	protected MenuItem newMenuItem(String title, @Nullable String icon) {
		var menuItem = new MenuItem(title);
		
		if (icon != null && !icon.equals("null")) {
			var iconURL = Poth.INSTANCE.getURL(icon);
			
			if (icon.endsWith(".svg")) {
				menuItem.setImage(FlatSVGUtils.svg2image(Poth.INSTANCE.getURL(icon), 32, 32));
			} else {
				menuItem.setImage(iconURL);
			}
		}
		
		menuItem.setCallback(_ -> onClick());
		
		return menuItem;
	}
	
	@SuppressWarnings("SameParameterValue")
	protected Menu newMenu(String title, @Nullable String icon) {
		Menu menu = new Menu(title);

		if (icon != null) {
			var iconURL = Poth.INSTANCE.getURL(icon);
			menu.setImage(iconURL);
		}

		menu.setCallback(_ -> onClick());

		return menu;
	}

	public String getID() {
		return id;
	}

}
