package it.davidestabelli.songrithmapp.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import it.davidestabelli.songrithmapp.MainGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = MainGame.V_HEIGHT*3;
		config.width = MainGame.V_WIDTH*3;
		config.resizable = false;
		new LwjglApplication(new MainGame(), config);
	}
}
