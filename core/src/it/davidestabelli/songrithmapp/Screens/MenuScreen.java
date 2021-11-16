package it.davidestabelli.songrithmapp.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import it.davidestabelli.songrithmapp.Helper.YouTubeDownlod;
import it.davidestabelli.songrithmapp.MainGame;
import it.davidestabelli.songrithmapp.Helper.ImportedFileHandler;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;
import it.davidestabelli.songrithmapp.Sprite.ImportedFileList;

public class MenuScreen implements Screen {
    private static final float CLIPBOARD_CHECK_INTERVAL = 1f;

    //Variables
    public MainGame game;
    private Stage stage;
    private YouTubeDownlod ytVideo;
    private MusicConverter importingMusicFile;
    private float clipboardCheckTime;

    //Stage Elements
    private VisTextButton startButton;
    private ImportedFileList importedFileList;

    //Options Elements
    private VisTextButton configButton;

    //Downloader Elements
    private VisTextButton importFromUrlButton;
    private VisWindow importFromUrlPopUp;
    private VisTextField urlTextField;
    private VisTextButton downloadButton;
    private VisImageButton closeButton;
    private VisLabel linkInfo;
    private VisLabel downloadProgressPercentage;
    private VisProgressBar downloadProgressBar;

    //Import Elements
    private VisTextButton importFileButton;
    private FileChooser fileChooser;

    //Texture
    private Texture background;
    private Texture blackBackground;

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
                MusicConverter music = importedFileList.getSelectedMusicFile();
                if(music != null) {
                    dispose();
                    game.setScreen(new MusicPlayerScreen(game, importedFileList.getSelectedMusicFile()));
                }
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
                importingMusicFile = new MusicConverter(files.get(0).file());
                //ImportedFileHandler.importNewFile(oggMusicFile);
                openImportMusicPopUp();
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

        // config Button
        configButton = new VisTextButton("Opzioni");
        configButton.setSize((Gdx.graphics.getWidth() / 6), (Gdx.graphics.getHeight() / 8));
        configButton.setPosition((Gdx.graphics.getWidth() / 4) * 3 - (startButton.getWidth() / 2),
                (Gdx.graphics.getHeight() / 8) * 3);
        stage.addActor(configButton);

        // import from URL
        importFromUrlButton = new VisTextButton("Importa Da URL");
        importFromUrlButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                openDownloadMusicPopUp();
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

    public void openImportMusicPopUp(){
        // import popup
        importFromUrlPopUp = new VisWindow("IL BRANO È IN FASE DI CONVERSIONE");
        importFromUrlPopUp.centerWindow();
        importFromUrlPopUp.setSize((Gdx.graphics.getWidth() / 1.5f), (Gdx.graphics.getHeight() / 3));

        importFromUrlPopUp.setResizable(false);
        importFromUrlPopUp.columnDefaults(2).left();
        downloadProgressBar = new VisProgressBar(0,100,0.1f,false);
        downloadProgressPercentage = new VisLabel("");
        importFromUrlPopUp.add(downloadProgressBar).expand().fillX();
        importFromUrlPopUp.add(downloadProgressPercentage);

        linkInfo = new VisLabel(". . .");
        importFromUrlPopUp.row();
        importFromUrlPopUp.add(linkInfo).expand().fill();

        urlTextField = new VisTextField();
        urlTextField.setDisabled(false);

        stage.addActor(importFromUrlPopUp.fadeIn());
    }

    public void openDownloadMusicPopUp(){
        // import popup
        importFromUrlPopUp = new VisWindow("INSERIRE IL LINK DI YOUTUBE DA CUI SCARICARE IL BRANO");
        importFromUrlPopUp.centerWindow();
        importFromUrlPopUp.setSize((Gdx.graphics.getWidth() / 1.5f), (Gdx.graphics.getHeight() / 3));
        // adding close button
        Label titleLabel = importFromUrlPopUp.getTitleLabel();
        Table titleTable = importFromUrlPopUp.getTitleTable();

        this.closeButton = new VisImageButton("close-window");
        titleTable.add(this.closeButton).padRight(-importFromUrlPopUp.getPadRight() + 0.7f);
        this.closeButton.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                importFromUrlPopUp.fadeOut();
            }
        });
        this.closeButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                event.cancel();
                return true;
            }
        });

        if (titleLabel.getLabelAlign() == Align.center && titleTable.getChildren().size == 2)
            titleTable.getCell(titleLabel).padLeft(this.closeButton.getWidth() * 2);

        importFromUrlPopUp.setResizable(false);
        importFromUrlPopUp.columnDefaults(2).left();
        urlTextField = new VisTextField();
        urlTextField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                downloadButton.setDisabled(false);
                linkInfo.setText(". . .");
            }
        });
        importFromUrlPopUp.add(urlTextField).expand().fillX();
        downloadButton = new VisTextButton("Download");
        downloadButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                ytVideo = new YouTubeDownlod(urlTextField.getText());

                if(ytVideo.isUrlValid()) {
                    downloadProgressBar = new VisProgressBar(0, 100, 0.1f, false);
                    downloadProgressPercentage = new VisLabel("");

                    importFromUrlPopUp.row();
                    importFromUrlPopUp.add(downloadProgressBar).expand().fillX();
                    importFromUrlPopUp.add(downloadProgressPercentage);
                    ytVideo.downloadAudio();
                } else {
                    linkInfo.setText("LINK NON VALIDO");
                    downloadButton.setDisabled(true);
                }
            }
        });
        importFromUrlPopUp.add(downloadButton);
        linkInfo = new VisLabel(". . .");
        importFromUrlPopUp.row();
        importFromUrlPopUp.add(linkInfo).expand().fill();
        clipboardCheckTime = 0;

        stage.addActor(importFromUrlPopUp.fadeIn());
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
        if(importFromUrlPopUp != null){
            if (importingMusicFile != null) {
                if(importingMusicFile.getImportingPercentage() < 100) {
                    downloadProgressBar.setValue(importingMusicFile.getImportingPercentage());
                    downloadProgressPercentage.setText(String.format("%d%%", importingMusicFile.getImportingPercentage()));
                    linkInfo.setText(importingMusicFile.getFileName());
                } else {
                    if(ytVideo != null)
                        importingMusicFile.getSource().delete();
                    importingMusicFile = null;
                    ytVideo = null;
                    importFromUrlPopUp.fadeOut();
                    importedFileList.updateVoices();
                }
            } else if(ytVideo != null) {
                if (ytVideo.downloadingState == YouTubeDownlod.DOWNLOAD_IN_PROGRESS) {
                    downloadProgressBar.setValue(ytVideo.downloadProgress);
                    downloadProgressPercentage.setText(String.format("%d%%", ytVideo.downloadProgress));
                    linkInfo.setText(ytVideo.videoInfoString);
                    downloadButton.setDisabled(true);
                    urlTextField.setDisabled(true);
                    closeButton.setDisabled(true);
                } else if (ytVideo.downloadingState == YouTubeDownlod.DOWNLOAD_FINISHED) {
                    importFromUrlPopUp.removeActor(downloadProgressBar);
                    importFromUrlPopUp.fadeOut();

                    downloadButton.setDisabled(false);
                    urlTextField.setDisabled(false);
                    closeButton.setDisabled(false);

                    importingMusicFile = ytVideo.getGeneratedMusicFile();
                    //ytVideo = null;
                    openImportMusicPopUp();
                }
            }

            if(!urlTextField.isDisabled()){
                clipboardCheckTime += dt;
                if (clipboardCheckTime >= CLIPBOARD_CHECK_INTERVAL) {
                    clipboardCheckTime = 0;
                    try {
                        String clipboardData = (String) Toolkit.getDefaultToolkit()
                                .getSystemClipboard().getData(DataFlavor.stringFlavor);

                        if (clipboardData != null) {
                            new URL(clipboardData);
                            String[] splittedBySlash = clipboardData.split("/");
                            String[] splittedByEqual = splittedBySlash[splittedBySlash.length - 1].split("=");
                            if (splittedByEqual[0].equals(YouTubeDownlod.YOUTUBE_URL_WATCH)) {
                                urlTextField.setText(clipboardData);
                                linkInfo.setText(". . .");
                                downloadButton.setDisabled(false);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
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
    }

}