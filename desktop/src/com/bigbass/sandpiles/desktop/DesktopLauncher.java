package com.bigbass.sandpiles.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.bigbass.sandpiles.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 1024;
		config.height = 1024;
		
		config.resizable = false;
		
		config.vSyncEnabled = false;
		
		config.title = "Sandpiles";
		
		new LwjglApplication(new Main(), config);
	}
}
