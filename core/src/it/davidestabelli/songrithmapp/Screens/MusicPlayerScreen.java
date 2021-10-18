package it.davidestabelli.songrithmapp.Screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;

import it.davidestabelli.songrithmapp.MainGame;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;
import it.davidestabelli.songrithmapp.Sprite.BeatBar;

public class MusicPlayerScreen implements Screen {

    public MainGame game;

    private Stage stage;

    VisImage playPauseButton;
    VisImage backToMenu;
    VisSlider musicSlider;
    VisLabel fileLabel;

    List<BeatBar> beatBars;
    float barRefreshTime;

    MusicConverter oggMusicFile;
    Music musicFile;
    Texture playButtonTexture;
    Texture pauseButtonTexture;
    Texture backToMenuTexture;
    Texture background;

    public MusicPlayerScreen(MainGame mainGame, MusicConverter oggMusicFile) {
        this.game = mainGame;
        this.oggMusicFile = oggMusicFile;
        musicFile = Gdx.audio.newMusic(oggMusicFile.getOggTarget());
        musicFile.setLooping(false);
        musicFile.setVolume(1f);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        if (!VisUI.isLoaded())
            VisUI.load();

        // textures
        playButtonTexture = new Texture("play_button.png");
        pauseButtonTexture = new Texture("pause_button.png");
        backToMenuTexture = new Texture("back_button.png");
        background = new Texture("background.png");

        // music slider
        musicSlider = new VisSlider(0f, oggMusicFile.getDuration() / 1000, 0.1f, false);
        musicSlider.setWidth(Gdx.graphics.getWidth() - 100);
        musicSlider.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                musicFile.pause();
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                musicFile.setPosition(musicSlider.getValue());
                musicFile.play();
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicFile.setPosition(musicSlider.getValue());
                super.clicked(event, x, y);
            }
        });
        musicSlider.setPosition(50, 20);
        stage.addActor(musicSlider);

        // file path label
        fileLabel = new VisLabel("");
        fileLabel.setColor(Color.BLACK);
        fileLabel.setPosition(Gdx.graphics.getWidth() - 150, 50 + musicSlider.getHeight());
        stage.addActor(fileLabel);

        // music play/pause button
        playPauseButton = new VisImage(playButtonTexture);
        playPauseButton.setSize(playButtonTexture.getWidth() / 2, playButtonTexture.getWidth() / 2);
        playPauseButton.setPosition(50, 25 + musicSlider.getHeight());
        playPauseButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (musicFile.isPlaying()) {
                    musicFile.pause();
                } else {
                    musicFile.play();
                }
            }
        });
        stage.addActor(playPauseButton);

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

        // beat bar
        beatBars = new ArrayList<BeatBar>();
        float barWidth = (Gdx.graphics.getWidth() / (oggMusicFile.getSpectrumList().length + 2));
        float barHeight = (Gdx.graphics.getHeight() / 3) * 2;
        for (int i = 0; i < oggMusicFile.getSpectrumList().length; i++) {
            List<Float> spectrum = oggMusicFile.getSpectrumList()[i];
            BeatBar beatBar = new BeatBar(barWidth + (barWidth * i), playPauseButton.getY() + playPauseButton.getHeight(), barWidth, barHeight);
            beatBar.setBeatBlockHeight(barHeight / 30);
            beatBar.setBeatBlockSpace(3f);

            beatBar.setMaxValue(Collections.max(spectrum));
            beatBar.setMinValue(Collections.min(spectrum));
            beatBars.add(beatBar);
        }
        barRefreshTime = 0;
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub

    }

    public void handleInput(float dt) {
    }

    public void update(float dt) {
        handleInput(dt);

        if (oggMusicFile != null && musicFile != null) {
            // slider progress
            if (musicFile.isPlaying())
                musicSlider.setValue(musicFile.getPosition());

            // is music playng update
            if (!musicFile.isPlaying())
                playPauseButton.setDrawable(playButtonTexture);
            else
                playPauseButton.setDrawable(pauseButtonTexture);

            Long minuteDuration = (oggMusicFile.getDuration()) / (1000 * 60);
            Long secondsDuration = ((oggMusicFile.getDuration()) % (1000 * 60)) / 1000;

            Integer roundedPosition = Math.round(musicFile.getPosition());

            Long minutePosition = (roundedPosition.longValue()) / (60);
            Long secondsPosition = (roundedPosition.longValue()) % (60);

            // beat bar position
            barRefreshTime += dt;
            if (barRefreshTime >= 0.05f){
                for (int i = 0; i < oggMusicFile.getSpectrumList().length; i++) {
                    BeatBar beatBar = beatBars.get(i);
                    List<Float> spectrum = oggMusicFile.getSpectrumList()[i];

                    Long actualSpectralValue = Math.round((musicFile.getPosition() * spectrum.size())
                            / (oggMusicFile.getDuration().doubleValue() / 1000));
                    if (actualSpectralValue == spectrum.size())
                        actualSpectralValue = actualSpectralValue - 1;                
                        barRefreshTime = 0;
                        beatBar.setActualValue(spectrum.get(actualSpectralValue.intValue()));                
                }
            }
            fileLabel.setText(String.format("%2d:%2d / %2d:%2d", minutePosition,
                    secondsPosition, minuteDuration, secondsDuration));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        for (BeatBar beatBar : beatBars) {
            beatBar.draw(game.batch);
        }        
        game.batch.end();
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
        System.out.println("DISPOSING MUSIC PLAYER SCREEN");
        stage.dispose();
        musicFile.dispose();
        playButtonTexture.dispose();
        pauseButtonTexture.dispose();
        background.dispose();
    }

}