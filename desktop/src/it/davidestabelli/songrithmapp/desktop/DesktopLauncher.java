package it.davidestabelli.songrithmapp.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import it.davidestabelli.songrithmapp.MainGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = MainGame.V_HEIGHT;
		config.width = MainGame.V_WIDTH;
		config.resizable = false;
		config.fullscreen = false;
		new LwjglApplication(new MainGame(), config);

	}
}
