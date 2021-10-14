package it.davidestabelli.songrithmapp.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileUtils;

import java.io.File;

import it.davidestabelli.songrithmapp.MainGame;
import it.davidestabelli.songrithmapp.Sprite.BeatCircle;

public class MenuScreen implements Screen{

    public MainGame game;

    private Stage stage;

    VisTextButton startButton;
    VisTextButton importFileButton;

    VisLabel fileLabel;

    FileChooser fileChooser;

    File filePicked;

    public MenuScreen (MainGame mainGame){
        this.game = mainGame;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        VisUI.load();

        // start button
        startButton = new VisTextButton("prova");
        startButton.addListener(new ClickListener() {
            public void clicked (InputEvent event, float x, float y) {
                game.setScreen(new PlayScreen(game));
            }
        });
        startButton.setPosition((Gdx.graphics.getWidth()/2) - (startButton.getWidth()/2),Gdx.graphics.getHeight()/2);
        stage.addActor(startButton);

        // file picker
        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                filePicked = files.get(0).file();
                fileLabel.setText(filePicked.getPath());
            }
        });

        // filePickerButton
        importFileButton = new VisTextButton("Importa File");
        importFileButton.addListener(new ClickListener() {
            public void clicked (InputEvent event, float x, float y) {
                stage.addActor(fileChooser.fadeIn());
            }
        });
        importFileButton.setPosition((Gdx.graphics.getWidth()/2) - (importFileButton.getWidth()/2),Gdx.graphics.getHeight()/2 - (importFileButton.getHeight()*2));
        stage.addActor(importFileButton);

        // file path label
        fileLabel = new VisLabel("Scegli un file...");
        fileLabel.setColor(Color.BLACK);
        fileLabel.setPosition((Gdx.graphics.getWidth()/2) + (importFileButton.getWidth() ), Gdx.graphics.getHeight()/2 - (importFileButton.getHeight()*2));
        stage.addActor(fileLabel);
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub

    }

    public void handleInput(float dt) {
    }

    public void update(float dt) {
        handleInput(dt);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        stage.dispose();
        VisUI.dispose();
    }

}