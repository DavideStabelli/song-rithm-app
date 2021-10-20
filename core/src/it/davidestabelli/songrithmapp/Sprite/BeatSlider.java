package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisSlider;

import java.util.ArrayList;
import java.util.List;

import it.davidestabelli.songrithmapp.Helper.MusicConverter;
import it.davidestabelli.songrithmapp.Screens.MusicPlayerScreen;

public class BeatSlider extends VisSlider {
    private static final int DEFAULT_TAG_WIDTH = 40;
    private static final int DEFAULT_TAG_HEIGHT = 100;
    private static final int DEFAULT_TAG_IN_SCREEN = 10;

    private Texture tagTexture;
    private Texture tagUpTexture;
    private Texture tagDownTexture;
    private Texture tagDoubleTexture;
    private Texture tagSelectionTexture;
    private Texture tagCursorTexture;

    private VisImage tagSelection;
    private VisImage tagCursor;

    private Group tagList;

    private boolean editMode;

    public BeatSlider(float min, float max, float stepSize, boolean vertical, Stage stage) {
        super(min, max, stepSize, vertical);

        tagTexture = new Texture("beat_slider_zoom_tile.png");
        tagUpTexture = new Texture("beat_slider_zoom_tile_up.png");
        tagDownTexture = new Texture("beat_slider_zoom_tile_down.png");
        tagDoubleTexture = new Texture("beat_slider_zoom_tile_double.png");
        tagSelectionTexture = new Texture("beat_slider_zoom_tile_selection.png");
        tagCursorTexture = new Texture("cursor.png");

        tagCursor = new VisImage(tagCursorTexture);
        tagCursor.setSize(tagCursorTexture.getWidth(), tagCursorTexture.getHeight());
        tagSelection = new VisImage(tagSelectionTexture);
        tagSelection.setSize(tagSelectionTexture.getWidth(), tagSelectionTexture.getHeight());
        
        tagList = new Group();
        editMode = false;
        stage.addActor(tagList);
        stage.addActor(this);
    }

    public void updateTags(MusicConverter music){
        tagList.clearChildren();
        if(editMode) {
            tagList.setPosition(0, getY() + getHeight() + 20);
            tagCursor.setPosition(0, 0);
            tagSelection.setPosition(0, 0);

            for (int i = 0; i < music.getBeatTrace().length; i++) {
                int beatTrace = music.getBeatTrace()[i];
                tagList.addActor(new BeatSliderTag(i, beatTrace));
                /*
                if ((beatTrace & MusicPlayerScreen.LEFT_BEAT) == MusicPlayerScreen.LEFT_BEAT)
                    tagList.addActor(new BeatSliderTag(this, music.getMillisFromBeatTraceIndex(i) / 1000, true));

                if ((beatTrace & MusicPlayerScreen.RIGHT_BEAT) == MusicPlayerScreen.RIGHT_BEAT)
                    tagList.addActor(new BeatSliderTag(this, music.getMillisFromBeatTraceIndex(i) / 1000, false));
                */
            }

            tagList.addActor(tagSelection);
            tagList.addActor(tagCursor);
        }

    }

    /*
    public void addTag(float position, boolean isLeft){
        tagList.addActor(new BeatSliderTag(this, position, isLeft));
    }
    */

    public void update(MusicConverter music, Long millisPosition, float dt){
        float cursorX = (getValue() / (getMaxValue() - getMinValue())) * (DEFAULT_TAG_WIDTH * music.getBeatTrace().length);
        tagCursor.setPosition(cursorX, 0);
        int index = music.getBeatTraceIndexFromMillis(millisPosition);
        tagSelection.setPosition(index * DEFAULT_TAG_WIDTH, 0);

        boolean isCursorOverHalfScreen = tagCursor.getX() + tagList.getX() >= (Gdx.graphics.getWidth() / 2);
        boolean isRollOverScreen = (tagList.getX() + (DEFAULT_TAG_WIDTH * music.getBeatTrace().length)) > Gdx.graphics.getWidth();
        
        if(isCursorOverHalfScreen && isRollOverScreen){
            tagList.setX((Gdx.graphics.getWidth() / 2) - tagCursor.getX());
        }

        boolean isCursorBeforeScreen = tagCursor.getX() + tagList.getX() <= 0;
        boolean isRollInScreen = tagList.getX() < 0;

        if(isCursorBeforeScreen && isRollInScreen){
            tagList.setX(-tagCursor.getX());
        }
    }

    public void setEditMode(boolean editMode){
        this.editMode = editMode;
    }

    private class BeatSliderTag extends VisImage {
        private float sliderIndex;
        private int beatValue; // if its up or down the slider

        public BeatSliderTag(float sliderIndex, int beatValue){
            if((beatValue & (MusicPlayerScreen.LEFT_BEAT | MusicPlayerScreen.RIGHT_BEAT)) == (MusicPlayerScreen.LEFT_BEAT | MusicPlayerScreen.RIGHT_BEAT))
                setDrawable(tagDoubleTexture);
            else if ((beatValue & MusicPlayerScreen.LEFT_BEAT) == MusicPlayerScreen.LEFT_BEAT)
                setDrawable(tagUpTexture);
            else if ((beatValue & MusicPlayerScreen.RIGHT_BEAT) == MusicPlayerScreen.RIGHT_BEAT)
                setDrawable(tagDownTexture);
            else
                setDrawable(tagTexture);

            this.sliderIndex = sliderIndex;
            this.beatValue = beatValue;
            this.setSize(DEFAULT_TAG_WIDTH, DEFAULT_TAG_HEIGHT);
            this.setY(0);
            this.setX(sliderIndex * DEFAULT_TAG_WIDTH);
        }

        /*
        public BeatSliderTag(BeatSlider parentSlider,float sliderPosition, boolean upTheSlider, ClickListener callback){
            this.sliderPosition = sliderPosition;
            this.upTheSlider = upTheSlider;
            this.setSize(DEFAULT_TAG_SIZE, DEFAULT_TAG_SIZE);
            this.parentSlider = parentSlider;
            if(upTheSlider)
                this.setY(parentSlider.getY() + parentSlider.getHeight());
            else
                this.setY(parentSlider.getY() - getHeight());

            float relativeXPosition = (sliderPosition / (parentSlider.getMaxValue() - parentSlider.getMinValue())) * parentSlider.getWidth();

            setX(relativeXPosition + parentSlider.getX());
            this.addListener(callback);
        }
        */
    }
}
