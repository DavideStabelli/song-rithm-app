package it.davidestabelli.songrithmapp.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import it.davidestabelli.songrithmapp.Helper.ImportedFileHandler;
import it.davidestabelli.songrithmapp.MainGame;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class ConfigScreen implements Screen {
    private static final float CONFIG_WIDTH = 200;
    private static final float CONFIG_HEIGHT = 20f;

    public MainGame game;

    private Stage stage;

    private VisImage backToMenu;
    private Texture backToMenuTexture;

    private VisWindow centerWindow;

    private Texture background;

    public ConfigScreen(MainGame mainGame){
        this.game = mainGame;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        if (!VisUI.isLoaded())
            VisUI.load();

        this.backToMenuTexture = new Texture("back_button.png");

        background = new Texture("background.png");

        // back button
        backToMenu = new VisImage(backToMenuTexture);
        backToMenu.setSize(50, 50);
        backToMenu.setPosition(20, Gdx.graphics.getHeight() - backToMenu.getHeight() - 20);
        backToMenu.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                dispose();
                ImportedFileHandler.saveConfigurations(mainGame.configs);
                game.setScreen(new MenuScreen(game));
            }
        });
        stage.addActor(backToMenu);

        centerWindow = new VisWindow("Configurazioni");
        centerWindow.centerWindow();
        centerWindow.setMovable(false);
        centerWindow.setSize((Gdx.graphics.getWidth() / 1.2f), (Gdx.graphics.getHeight() / 1.2f));
        centerWindow.setResizable(false);
        int numberOfColumns = Math.round((centerWindow.getWidth() - CONFIG_WIDTH) / CONFIG_WIDTH);
        centerWindow.columnDefaults(numberOfColumns).left();

        Field[] configFields = mainGame.configs.getClass().getFields();
        int rowNumber = 1;
        for(int i = 0; i < configFields.length; i++){
            Group fieldComponent = new Group();
            Field configField = configFields[i];
            int columnNumber = (i+1) % numberOfColumns;

            Widget inputField = null;

            switch(configField.getGenericType().getTypeName()){
                case "int":
                    Annotation[] annotations= configField.getDeclaredAnnotations();
                    if(annotations.length > 0){
                        switch(annotations[0].annotationType().getSimpleName()) {
                            case "KeyConfiguration":
                                inputField = new VisTextField();
                                try{
                                    ((VisTextField)inputField).setText(Input.Keys.toString(configField.getInt(mainGame.configs)));
                                }catch(Exception e){}
                                inputField.addListener(new ClickListener() {
                                    @Override
                                    public boolean keyTyped(InputEvent event, char character) {
                                        try{
                                            configField.set(mainGame.configs, event.getKeyCode());
                                            ((VisTextField)event.getListenerActor()).setText(Input.Keys.toString(configField.getInt(mainGame.configs)));
                                        }catch(Exception e){}
                                        return false;
                                    }
                                });
                                break;
                        }
                    } else {
                        inputField = new VisTextField();
                        try{
                            ((VisTextField)inputField).setText(String.valueOf(configField.getInt(mainGame.configs)));
                        }catch(Exception e){}
                        inputField.addListener(new ChangeListener() {
                            @Override
                            public void changed(ChangeEvent event, Actor actor) {
                                VisTextField changedField = (VisTextField) actor;
                                try {
                                    configField.set(mainGame.configs, changedField.getText());
                                } catch (Exception e) {}
                            }
                        });
                    }
                    break;
                default:
                    inputField = new VisTextField();
                    try{
                        ((VisTextField)inputField).setText(String.valueOf(configField.get(mainGame.configs)));
                    }catch(Exception e){}
                    inputField.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            VisTextField changedField = (VisTextField) actor;
                            try {
                                configField.set(mainGame.configs, changedField.getText());
                            } catch (Exception e) {}
                        }
                    });
                    break;
            }

            inputField.setWidth(CONFIG_WIDTH);
            fieldComponent.addActor(inputField);
            VisLabel label = new VisLabel(configField.getName());
            label.setY(inputField.getHeight());
            fieldComponent.addActor(label);
            centerWindow.add(fieldComponent).expand().fillX();
            if(columnNumber == numberOfColumns){
                centerWindow.row();
                rowNumber++;
            }
        }
        stage.addActor(centerWindow.fadeIn());
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.end();
        stage.act();
        stage.draw();
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
        stage.dispose();
    }
}
