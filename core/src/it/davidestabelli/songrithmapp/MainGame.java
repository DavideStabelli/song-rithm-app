package it.davidestabelli.songrithmapp;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.ActorUtils;
import com.kotcrab.vis.ui.widget.ButtonBar;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.ButtonBar.ButtonType;

import it.davidestabelli.songrithmapp.Helper.Configurations;
import it.davidestabelli.songrithmapp.Screens.MenuScreen;

public class MainGame extends Game {
	public static final int V_WIDTH = 1200;
	public static final int V_HEIGHT = 624;
	public static final float PPM = 100;
	public static final int WINDOW_ICON_SIZE = 30;

	public SpriteBatch batch;
	public Configurations configs;

	public VisImage closeWindow;
	public Group windowsIcons;
	
	private Texture closeWindowTexture;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		configs = new Configurations();

		closeWindowTexture = new Texture("exit.png");

		windowsIcons = new Group();

		closeWindow = new VisImage(closeWindowTexture);
        closeWindow.setSize(WINDOW_ICON_SIZE, WINDOW_ICON_SIZE);
        closeWindow.setPosition(Gdx.graphics.getWidth() - WINDOW_ICON_SIZE * 2, Gdx.graphics.getHeight() - WINDOW_ICON_SIZE * 2);
        closeWindow.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
				dispose();
                Gdx.app.exit();
            }
        });
		windowsIcons.addActor(closeWindow);

		setScreen(new MenuScreen(this));
	}

	@Override
	public void render () {		
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
		//batch.dispose();
	}
}
