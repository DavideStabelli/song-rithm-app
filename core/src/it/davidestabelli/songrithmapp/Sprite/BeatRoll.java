package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;

public class BeatRoll extends Group {
    public static final float MENU_BAR_HEIGHT = BeatRollTile.DEFAULT_TAG_HEIGHT/3.5f;

    private Texture tagSelectionTexture;
    private Texture tagCursorTexture;

    private VisImage tagCursor;
    private VisImage tagSelection;

    private int beatTraceIndex;
    private float maxValue;
    private float minValue;
    private String name;
    private ClickListener callableTagClickEvent;

    private Group menuBar;
    private VisTextField barName;
    private VisImage editNameButton;
    private VisImage deleteButton;
    private VisImage changeColorButton;
    private ColorPicker colorPicker;
    private Color color;

    public BeatRoll(int beatTraceIndex, float maxValue, float minValue) {

        tagSelectionTexture = new Texture("beat_slider_zoom_tile_selection.png");
        tagCursorTexture = new Texture("cursor.png");

        tagCursor = new VisImage(tagCursorTexture);
        tagSelection = new VisImage(tagSelectionTexture);

        this.beatTraceIndex = beatTraceIndex;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.callableTagClickEvent = null;

        this.name = "";

        this.menuBar = new Group();
        menuBar.setPosition(0, BeatRollTile.DEFAULT_TAG_HEIGHT);
        menuBar.setWidth(Gdx.graphics.getWidth());

        this.barName = new VisTextField(name);
        barName.setPosition(0,0);
        barName.setSize(100, MENU_BAR_HEIGHT);
        barName.setDisabled(true);
        menuBar.addActor(barName);

        this.editNameButton = new VisImage();
        editNameButton.setDrawable(new Texture("edit.png"));
        editNameButton.setSize(MENU_BAR_HEIGHT,MENU_BAR_HEIGHT);
        editNameButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                barName.setDisabled(!barName.isDisabled());
            }
        });
        editNameButton.setPosition(barName.getX() + barName.getWidth() + MENU_BAR_HEIGHT, 0);
        menuBar.addActor(editNameButton);

        this.deleteButton = new VisImage();
        deleteButton.setDrawable(new Texture("exit.png"));
        deleteButton.setSize(MENU_BAR_HEIGHT,MENU_BAR_HEIGHT);
        deleteButton.setPosition(editNameButton.getX() + editNameButton.getWidth() + MENU_BAR_HEIGHT, 0);
        menuBar.addActor(deleteButton);

        this.changeColorButton = new VisImage();
        changeColorButton.setDrawable(new Texture("pick_color.png"));
        changeColorButton.setSize(MENU_BAR_HEIGHT,MENU_BAR_HEIGHT);
        changeColorButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                getStage().addActor(colorPicker.fadeIn());
            }
        });
        changeColorButton.setPosition(deleteButton.getX() + deleteButton.getWidth() + MENU_BAR_HEIGHT, 0);
        menuBar.addActor(changeColorButton);

        this.colorPicker = new ColorPicker();
        colorPicker.setListener(new ColorPickerListener() {
            @Override
            public void canceled(Color oldColor) {
                color = oldColor;
            }

            @Override
            public void changed(Color newColor) {
                color = newColor;
            }

            @Override
            public void reset(Color previousColor, Color newColor) {
                color = newColor;
            }

            @Override
            public void finished(Color newColor) {
                color = newColor;
            }
        });
        colorPicker.centerWindow();
        colorPicker.toFront();
    }

    public void addTagClickAction(ClickListener callable){
        this.callableTagClickEvent = callable;
    }

    public void updateTags(final MusicConverter music, final Music musicFile){
        tagCursor.setSize(tagCursorTexture.getWidth(), BeatRollTile.DEFAULT_TAG_HEIGHT);
        tagSelection.setSize(BeatRollTile.DEFAULT_TAG_WIDTH, BeatRollTile.DEFAULT_TAG_HEIGHT);
        tagCursor.setPosition(0, 0);
        tagSelection.setPosition(0, 0);

        for (int j = 0; j < music.getBeatTrace().length; j++) {
            int beatTrace = music.getBeatTrace()[j];
            addActor(new BeatRollTile(j, beatTraceIndex, beatTrace, callableTagClickEvent));
        }
        addActor(tagSelection);
        addActor(tagCursor);
        addActor(menuBar);
    }

    public void update(MusicConverter music, float sliderValue){

        long sliderValueMillis = Math.round(sliderValue);

        float cursorX = (sliderValueMillis / (maxValue - minValue)) * (BeatRollTile.DEFAULT_TAG_WIDTH * music.getBeatTrace().length);
        tagCursor.setPosition(cursorX, 0);

        long index = music.getBeatTraceIndexFromMillis(sliderValueMillis);
        tagSelection.setPosition(index * BeatRollTile.DEFAULT_TAG_WIDTH, 0);

        boolean isCursorOverHalfScreen = tagCursor.getX() + getX() >= (Gdx.graphics.getWidth() / 6) * 5;
        boolean isRollOverScreen = (getX() + (BeatRollTile.DEFAULT_TAG_WIDTH * music.getBeatTrace().length)) >= Gdx.graphics.getWidth();

        if (isCursorOverHalfScreen && isRollOverScreen) {
            setX((Gdx.graphics.getWidth() / 6) * 5 - tagCursor.getX());
        }
        if ((getX() + (BeatRollTile.DEFAULT_TAG_WIDTH * music.getBeatTrace().length)) < Gdx.graphics.getWidth())
            setX(Gdx.graphics.getWidth() - (BeatRollTile.DEFAULT_TAG_WIDTH * music.getBeatTrace().length));

        boolean isCursorBeforeScreen = tagCursor.getX() + getX() <= (Gdx.graphics.getWidth() / 6);
        boolean isRollInScreen = getX() <= 0;

        if (isCursorBeforeScreen && isRollInScreen) {
            setX((Gdx.graphics.getWidth() / 6) - tagCursor.getX());
        }
        if (getX() > 0)
            setX(0);

    }
}
