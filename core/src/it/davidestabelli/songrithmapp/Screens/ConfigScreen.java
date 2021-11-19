package it.davidestabelli.songrithmapp.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTextField;
import it.davidestabelli.songrithmapp.MainGame;

import java.lang.reflect.Field;

public class ConfigScreen implements Screen {
    public MainGame game;

    private Stage stage;

    private VisImage backToMenu;
    private Texture backToMenuTexture;

    private Group inputFields;

    public ConfigScreen(MainGame mainGame){
        this.game = mainGame;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        if (!VisUI.isLoaded())
            VisUI.load();

        this.backToMenuTexture = new Texture("back_button.png");

        // back button
        backToMenu = new VisImage(backToMenuTexture);
        backToMenu.setSize(50, 50);
        backToMenu.setPosition(20, Gdx.graphics.getHeight() - backToMenu.getHeight() - 20);
        backToMenu.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                dispose();
                game.setScreen(new MenuScreen(game));
            }
        });
        stage.addActor(backToMenu);

        this.inputFields = new Group();
        Field[] configFields = mainGame.configs.getClass().getFields();
        for(Field configField : configFields){
            switch(configField.getGenericType().getTypeName()){
                case "int":
                    VisTextField inputField = new VisTextField();
                    inputField.setText();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
