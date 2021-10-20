package it.davidestabelli.songrithmapp.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import java.io.File;

import it.davidestabelli.songrithmapp.Helper.YouTubeDownlod;
import it.davidestabelli.songrithmapp.MainGame;
import it.davidestabelli.songrithmapp.Helper.ImportedFileHandler;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;
import it.davidestabelli.songrithmapp.Sprite.ImportedFileList;

public class MenuScreen implements Screen {

    public MainGame game;

    private Stage stage;

    VisTextButton startButton;
    VisTextButton importFileButton;
    VisTextButton importFromUrlButton;
    ImportedFileList importedFileList;

    VisWindow importFromUrlPopUp;
    VisTextField urlTextField;
    VisTextButton downloadButton;
    VisLabel linkInfo;
    VisLabel downloadProgressPercentage;
    YouTubeDownlod ytVideo;
    VisProgressBar downloadProgressBar;

    FileChooser fileChooser;

    Texture background;
    Texture blackBackground;

    public MenuScreen(MainGame mainGame) {
        this.game = mainGame;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        if (!VisUI.isLoaded())
            VisUI.load();

        stage.addActor(game.windowsIcons);

        // textures
        background = new Texture("background.png");
        blackBackground = new Texture("black_background.png");

        // start button
        startButton = new VisTextButton("Riproduci Brano");
        startButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {                
                dispose();
                game.setScreen(new MusicPlayerScreen(game, importedFileList.getSelectedMusicFile()));
            }
        });
        startButton.setSize((Gdx.graphics.getWidth() / 6), (Gdx.graphics.getHeight() / 8));
        startButton.setPosition((Gdx.graphics.getWidth() / 4) * 3 - (startButton.getWidth() / 2),
                (Gdx.graphics.getHeight() / 8) * 6);
        stage.addActor(startButton);

        // file picker
        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setResizable(false);
        FileTypeFilter fileTypeFilter = new FileTypeFilter(false);
        fileTypeFilter.addRule("File Audio", "wav", "mp3", "ogg");
        fileChooser.setFileTypeFilter(fileTypeFilter);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected(Array<FileHandle> files) {
                MusicConverter oggMusicFile = new MusicConverter(files.get(0).file());
                ImportedFileHandler.importNewFile(oggMusicFile);
                importedFileList.updateVoices();
            }
        });

        // filePickerButton
        importFileButton = new VisTextButton("Importa File");
        importFileButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                stage.addActor(fileChooser.fadeIn());
            }
        });
        importFileButton.setSize((Gdx.graphics.getWidth() / 6), (Gdx.graphics.getHeight() / 8));
        importFileButton.setPosition((Gdx.graphics.getWidth() / 4) * 3 - (startButton.getWidth() / 2),
                (Gdx.graphics.getHeight() / 8) * 5);
        stage.addActor(importFileButton);

        // import popup
        importFromUrlPopUp = new VisWindow("INSERIRE IL LINK DI YOUTUBE DA CUI SCARICARE IL BRANO");
        importFromUrlPopUp.centerWindow();
        importFromUrlPopUp.setSize((Gdx.graphics.getWidth() / 1.5f), (Gdx.graphics.getHeight() / 3));
        importFromUrlPopUp.addCloseButton();
        importFromUrlPopUp.setResizable(false);
        importFromUrlPopUp.columnDefaults(2).left();
        urlTextField = new VisTextField();
        importFromUrlPopUp.add(urlTextField).expand().fillX();
        importFromUrlPopUp.closeOnEscape();
        downloadButton = new VisTextButton("Download");
        downloadButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                ytVideo = new YouTubeDownlod(urlTextField.getText());

                downloadProgressBar = new VisProgressBar(0,100,0.1f,false);
                downloadProgressPercentage = new VisLabel("");

                importFromUrlPopUp.row();
                importFromUrlPopUp.add(downloadProgressBar).expand().fillX();
                importFromUrlPopUp.add(downloadProgressPercentage);
                ytVideo.downloadAudio();
            }
        });
        importFromUrlPopUp.add(downloadButton);
        linkInfo = new VisLabel(". . .");
        importFromUrlPopUp.row();
        importFromUrlPopUp.add(linkInfo).expand().fill();

        // import from URL
        importFromUrlButton = new VisTextButton("Importa Da URL");
        importFromUrlButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                stage.addActor(importFromUrlPopUp.fadeIn());
            }
        });
        importFromUrlButton.setSize((Gdx.graphics.getWidth() / 6), (Gdx.graphics.getHeight() / 8));
        importFromUrlButton.setPosition((Gdx.graphics.getWidth() / 4) * 3 - (startButton.getWidth() / 2),
                (Gdx.graphics.getHeight() / 8) * 4);
        stage.addActor(importFromUrlButton);

        // file list
        importedFileList = new ImportedFileList(stage);
        importedFileList.setSize((Gdx.graphics.getWidth() / 2), Gdx.graphics.getHeight());
        importedFileList.setPosition(0, 0);
        importedFileList.setMovable(false);
        importedFileList.setResizable(false);
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub
    }

    public void handleInput(float dt) {
    }

    public void update(float dt) {
        handleInput(dt);

        // yt download managment
        if(importFromUrlPopUp != null && ytVideo != null){
            if(ytVideo.isDownloading) {
                downloadProgressBar.setValue(ytVideo.downloadProgress);
                downloadProgressPercentage.setText(String.format("%d%%",ytVideo.downloadProgress));
                linkInfo.setText(ytVideo.videoInfoString);
                downloadButton.setDisabled(true);
                urlTextField.setDisabled(true);
            }
            else {
                importedFileList.updateVoices();

                importFromUrlPopUp.removeActor(downloadProgressBar);
                importFromUrlPopUp.fadeOut();

                downloadButton.setDisabled(false);
                urlTextField.setDisabled(false);

                ytVideo = null;
            }
        }

        if(importedFileList != null){
            importedFileList.update();
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

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
        System.out.println("DISPOSING MENU SCREEN");
        stage.dispose();
        VisUI.dispose();
        background.dispose();
        ;
    }

}