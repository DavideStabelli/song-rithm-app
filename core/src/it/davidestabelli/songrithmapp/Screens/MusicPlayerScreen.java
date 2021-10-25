package it.davidestabelli.songrithmapp.Screens;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;

import it.davidestabelli.songrithmapp.Helper.ImportedFileHandler;
import it.davidestabelli.songrithmapp.MainGame;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;
import it.davidestabelli.songrithmapp.Sprite.BeatBar;
import it.davidestabelli.songrithmapp.Sprite.BeatCircle;
import it.davidestabelli.songrithmapp.Sprite.BeatCircleAnimation;
import it.davidestabelli.songrithmapp.Sprite.BeatSlider;

public class MusicPlayerScreen implements Screen {
    public static final short RIGHT_BEAT = 1;
    public static final short LEFT_BEAT = 2;
    public static final float FUTURE_BEAT_DELTA_TIME = 500;

    public MainGame game;

    private Stage stage;

    VisImage playPauseButton;
    VisImage recButton;
    VisImage backToMenu;
    VisImage clearButton;
    BeatSlider musicSlider;
    VisLabel fileLabel;

    List<BeatBar> beatBars;
    float barRefreshTime;

    MusicConverter music;
    Music musicFile;
    boolean isRec;

    Texture playButtonTexture;
    Texture pauseButtonTexture;
    Texture clearButtonTexture;
    Texture recTexture;
    Texture stopTexture;
    Texture backToMenuTexture;
    Texture background;

    private BeatCircle leftBeatCircle;
    private BeatCircle rightBeatCircle;

    public MusicPlayerScreen(final MainGame mainGame, MusicConverter musicFromMenu) {
        this.game = mainGame;
        this.music = musicFromMenu;
        musicFile = Gdx.audio.newMusic(music.getOggTarget());
        musicFile.setLooping(false);
        musicFile.setVolume(1f);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        if (!VisUI.isLoaded())
            VisUI.load();

        // textures
        playButtonTexture = new Texture("play_button.png");
        pauseButtonTexture = new Texture("pause_button.png");
        clearButtonTexture = new Texture("trash_close.png");
        backToMenuTexture = new Texture("back_button.png");
        background = new Texture("background.png");
        recTexture = new Texture("rec_button.png");
        stopTexture = new Texture("stop_button.png");

        // file path label
        fileLabel = new VisLabel("");
        fileLabel.setColor(Color.BLACK);
        stage.addActor(fileLabel);

        // music play/pause button
        playPauseButton = new VisImage(playButtonTexture);
        playPauseButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (musicFile.isPlaying()) {
                    musicFile.pause();
                    leftBeatCircle.setActivateEffect(false);
                    rightBeatCircle.setActivateEffect(false);
                } else {
                    musicFile.play();
                    if(!isRec) {
                        leftBeatCircle.setActivateEffect(true);
                        rightBeatCircle.setActivateEffect(true);
                    }
                }
            }
        });
        stage.addActor(playPauseButton);

        // music slider
        musicSlider = new BeatSlider(0f, music.getDuration(), 0.01f, false, stage);
        musicSlider.addListener(new ClickListener() {
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                //musicFile.setPosition(musicSlider.getValue());
                super.touchDragged(event, x, y, pointer);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                musicFile.pause();
                leftBeatCircle.setActivateEffect(false);
                rightBeatCircle.setActivateEffect(false);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                musicFile.setPosition(musicSlider.getValue() / 1000);
                if(!isRec){
                    musicFile.play();
                    leftBeatCircle.setActivateEffect(true);
                    rightBeatCircle.setActivateEffect(true);
                }
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicFile.setPosition(musicSlider.getValue() / 1000);
                if(!isRec) {
                    musicFile.play();
                    leftBeatCircle.setActivateEffect(true);
                    rightBeatCircle.setActivateEffect(true);
                }
                super.clicked(event, x, y);
            }
        });

        // beat rec button
        recButton = new VisImage(recTexture);
        recButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isRec) {
                    isRec = false;
                    recButton.setDrawable(recTexture);
                    ImportedFileHandler.updateBeatTrace(music);
                    setStageActorsForPlay();
                } else {
                    isRec = true;
                    recButton.setDrawable(stopTexture);
                    setStageActorsForEdit();
                }
            }
        });
        recButton.setSize(50, 50);
        recButton.setPosition(Gdx.graphics.getWidth() - recButton.getWidth() - 20, Gdx.graphics.getHeight() - recButton.getHeight() - 20);
        stage.addActor(recButton);
        isRec = false;

        // clear beat button
        clearButton = new VisImage(clearButtonTexture);
        clearButton.setSize(50, 50);
        clearButton.setPosition(Gdx.graphics.getWidth() - clearButton.getWidth() - 20, recButton.getY() - clearButton.getHeight() - 20);
        clearButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                music.clearBeatTrace();
                musicSlider.updateTags(music, musicFile);
            }
        });
        stage.addActor(clearButton);

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
        float barWidth = (Gdx.graphics.getWidth() / (music.getSpectrumList().length + 2));
        float barHeight = (Gdx.graphics.getHeight() / 3) * 2;
        for (int i = 0; i < music.getSpectrumList().length; i++) {
            List<Float> spectrum = music.getSpectrumList()[i];
            BeatBar beatBar = new BeatBar(barWidth + (barWidth * i), playPauseButton.getY() + playPauseButton.getHeight(), barWidth, barHeight);
            beatBar.setBeatBlockHeight(barHeight / 30);
            beatBar.setBeatBlockSpace(3f);

            beatBar.setMaxValue(Collections.max(spectrum));
            beatBar.setMinValue(Collections.min(spectrum));
            beatBars.add(beatBar);
        }      
        barRefreshTime = 0;

        // beat circles
        leftBeatCircle = new BeatCircle(new Vector2(Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight()/2), Gdx.graphics.getHeight()/3);
        rightBeatCircle = new BeatCircle(new Vector2((Gdx.graphics.getWidth()/4) * 3, Gdx.graphics.getHeight()/2), Gdx.graphics.getHeight()/3);

        setStageActorsForPlay();
    }

    private void setStageActorsForPlay(){
        leftBeatCircle.setPosition(new Vector2(Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight()/2));
        rightBeatCircle.setPosition(new Vector2(Gdx.graphics.getWidth()/4 * 3, Gdx.graphics.getHeight()/2));
        leftBeatCircle.setActivateEffect(true);
        rightBeatCircle.setActivateEffect(true);

        musicSlider.setWidth(Gdx.graphics.getWidth() / 1.5f);
        musicSlider.setPosition(Gdx.graphics.getWidth()/2 - musicSlider.getWidth()/2 , 20);
        musicSlider.setEditMode(false);
        musicSlider.updateTags(music, musicFile);

        fileLabel.setSize(40,20);
        fileLabel.setPosition(musicSlider.getX() + musicSlider.getWidth() + 20, musicSlider.getY() - (fileLabel.getHeight() - musicSlider.getHeight()) / 2);

        playPauseButton.setSize(40, 40);
        playPauseButton.setPosition(musicSlider.getX() - playPauseButton.getWidth() - 20, musicSlider.getY() - (playPauseButton.getHeight() - musicSlider.getHeight()) / 2);

        for (BeatBar beatBar : beatBars) 
            beatBar.setHidden(false);

        clearButton.setVisible(false);
    }

    private void setStageActorsForEdit(){
        leftBeatCircle.setPosition(new Vector2(Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight()/ 1.5f));
        rightBeatCircle.setPosition(new Vector2(Gdx.graphics.getWidth()/4 * 3, Gdx.graphics.getHeight()/1.5f));
        leftBeatCircle.setActivateEffect(false);
        rightBeatCircle.setActivateEffect(false);

        fileLabel.setSize(60,30);
        fileLabel.setPosition((Gdx.graphics.getWidth()/2) - fileLabel.getWidth()/2, 20);

        playPauseButton.setSize(40, 40);
        playPauseButton.setPosition(fileLabel.getX() - playPauseButton.getWidth() - 10, fileLabel.getY() + fileLabel.getHeight()/2 - playPauseButton.getHeight()/2);

        musicSlider.setWidth(Gdx.graphics.getWidth() - 10);
        musicSlider.setPosition(5 , playPauseButton.getHeight() + playPauseButton.getY() + 20);
        musicSlider.setEditMode(true);
        musicSlider.updateTags(music, musicFile);

        for (BeatBar beatBar : beatBars) 
            beatBar.setHidden(true);

        clearButton.setVisible(true);
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub

    }

    public void handleInput(float dt) {
    }

    public void update(float dt) {
        handleInput(dt);

        if (music != null && musicFile != null) {
            // slider progress
            if (musicFile.isPlaying())
                musicSlider.setValue(musicFile.getPosition() * 1000);

            // is music playing update
            if (!musicFile.isPlaying())
                playPauseButton.setDrawable(playButtonTexture);
            else
                playPauseButton.setDrawable(pauseButtonTexture);

            String stringPosition = LocalTime.ofSecondOfDay(Math.round(musicSlider.getValue() / 1000)).format(MusicConverter.AUDIO_FORMAT);

            fileLabel.setText(String.format("%s / %s", stringPosition, music.getStringedDuration()));

            // beat bar position
            if(!isRec && musicFile.isPlaying()) {
                barRefreshTime += dt;
                if (barRefreshTime >= 0.05f) {
                    for (int i = 0; i < music.getSpectrumList().length; i++) {
                        BeatBar beatBar = beatBars.get(i);
                        List<Float> spectrum = music.getSpectrumList()[i];

                        Long actualSpectralValue = Math.round((musicFile.getPosition() * spectrum.size())
                                / (music.getDuration().doubleValue() / 1000));
                        if (actualSpectralValue == spectrum.size())
                            actualSpectralValue = actualSpectralValue - 1;
                        barRefreshTime = 0;
                        beatBar.setActualValue(spectrum.get(actualSpectralValue.intValue()));
                    }
                }
            }
        }

        // pause with space key
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            if (musicFile.isPlaying()) {
                musicFile.pause();
                leftBeatCircle.setActivateEffect(false);
                rightBeatCircle.setActivateEffect(false);
            } else {
                musicFile.play();
                if(!isRec) {
                    leftBeatCircle.setActivateEffect(true);
                    rightBeatCircle.setActivateEffect(true);
                }
            }
        }

        long millisPosition = Math.round(musicFile.getPosition() * 1000);

        // rec update
        if(isRec){
            if(Gdx.input.isKeyJustPressed(Input.Keys.F)){
                music.setBeatTrace(millisPosition, LEFT_BEAT);
                musicSlider.updateTags(music, musicFile);
            }
            if(Gdx.input.isKeyJustPressed(Input.Keys.J)){
                music.setBeatTrace(millisPosition, RIGHT_BEAT);
                musicSlider.updateTags(music, musicFile);
            }
            if(Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)){
                music.setBeatTrace(millisPosition, 0);
                musicSlider.updateTags(music, musicFile);
            }
        }

        // slider zoom update
        if(isRec){
            musicSlider.update(music, millisPosition, dt);
        }

        // set beat cirle activation
        int beatTrace = music.getBeatTrace(musicSlider.getValue());
        if((beatTrace & LEFT_BEAT) == LEFT_BEAT && musicFile.isPlaying())
            leftBeatCircle.setActive(true);
        else
            leftBeatCircle.setActive(false);

        if((beatTrace & RIGHT_BEAT) == RIGHT_BEAT && musicFile.isPlaying())
            rightBeatCircle.setActive(true);
        else
            rightBeatCircle.setActive(false);

        // set beat cirle animation

        Long futureBeatTracePosition = music.getBeatTraceIndexFromMillis(musicSlider.getValue() + leftBeatCircle.getAnimationDuration());
        int futureBeatTrace = music.getBeatTrace()[futureBeatTracePosition.intValue()];
        if((futureBeatTrace & LEFT_BEAT) == LEFT_BEAT && musicFile.isPlaying())
            leftBeatCircle.addCircleAnimation(futureBeatTracePosition);

        if((futureBeatTrace & RIGHT_BEAT) == RIGHT_BEAT && musicFile.isPlaying())
            rightBeatCircle.addCircleAnimation(futureBeatTracePosition);

        leftBeatCircle.update(dt);
        rightBeatCircle.update(dt);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if(musicFile.isPlaying())
            for (BeatBar beatBar : beatBars)
                beatBar.draw(game.batch);

        leftBeatCircle.draw(game.batch);
        rightBeatCircle.draw(game.batch);
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