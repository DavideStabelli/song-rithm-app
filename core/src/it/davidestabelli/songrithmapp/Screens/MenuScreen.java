package it.davidestabelli.songrithmapp.Screens;

import java.io.File;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import it.davidestabelli.songrithmapp.MainGame;

public class MenuScreen implements Screen{

    public MainGame game;

    private Stage stage;

    VisTextButton startButton;
    VisTextButton importFileButton;
    VisImageButton playPauseButton;

    VisLabel fileLabel;

    FileChooser fileChooser;

    Boolean isMusicPlaying;
    FileHandle filePicked;
    Music musicFile;
    Texture playButtonTexture;
    Texture pauseButtonTexture;

    public MenuScreen (MainGame mainGame){
        this.game = mainGame;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        VisUI.load();
        isMusicPlaying = false;

        // textures
        playButtonTexture = new Texture("play_button.png");
        pauseButtonTexture = new Texture("pause_button.png");

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
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setResizable(false);
        FileTypeFilter fileTypeFilter = new FileTypeFilter(false);
        fileTypeFilter.addRule("File Audio", "wav","mp3","ogg");
        fileChooser.setFileTypeFilter(fileTypeFilter);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                filePicked = files.get(0);
                fileLabel.setText(filePicked.path());
                musicFile = Gdx.audio.newMusic(filePicked);
                musicFile.setLooping(false);
                musicFile.setVolume(1f);
                musicFile.play();
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

        // music play/pause button        
        playPauseButton = new VisImageButton(new VisImage(playButtonTexture).getDrawable(),new VisImage(playButtonTexture).getDrawable(),new VisImage(pauseButtonTexture).getDrawable());
        playPauseButton.setPosition((Gdx.graphics.getWidth()/2) - (playButtonTexture.getWidth()/4), 50);
        playPauseButton.setSize(playButtonTexture.getWidth()/2, playButtonTexture.getWidth()/2);
        playPauseButton.setColor(1, 1, 1, 1);
        playPauseButton.addListener(new ClickListener() {
            public void clicked (InputEvent event, float x, float y) {
                isMusicPlaying = !isMusicPlaying;
                playPauseButton.setChecked(isMusicPlaying);
                if(isMusicPlaying){
                    musicFile.play();
                } else {
                    musicFile.pause();
                }
            }
        });
        stage.addActor(playPauseButton);

        // music slider

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