package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisSlider;

import java.util.ArrayList;
import java.util.List;

import it.davidestabelli.songrithmapp.Helper.MusicConverter;
import it.davidestabelli.songrithmapp.Screens.MusicPlayerScreen;

public class BeatSlider extends VisSlider {
    private static final int DEFAULT_TAG_WIDTH = 20;
    private static final int DEFAULT_TAG_HEIGHT = Gdx.graphics.getHeight() / 8;

    private Texture tagTexture;
    private Texture tagUpTexture;
    private Texture tagDownTexture;
    private Texture tagDoubleTexture;
    private Texture tagSelectionTexture;
    private Texture tagCursorTexture;

    private VisImage tagSelection;
    private VisImage tagCursor;

    private Group[] tagLists;

    private boolean editMode;

    public BeatSlider(float min, float max, float stepSize, boolean vertical, Stage stage, int beatNumber) {
        super(min, max, stepSize, vertical);

        tagTexture = new Texture("beat_slider_zoom_tile.png");
        tagUpTexture = new Texture("beat_slider_zoom_tile_up.png");
        tagDownTexture = new Texture("beat_slider_zoom_tile_down.png");
        tagDoubleTexture = new Texture("beat_slider_zoom_tile_double.png");
        tagSelectionTexture = new Texture("beat_slider_zoom_tile_selection.png");
        tagCursorTexture = new Texture("cursor.png");

        tagCursor = new VisImage(tagCursorTexture);
        tagCursor.setSize(tagCursorTexture.getWidth(), DEFAULT_TAG_HEIGHT);
        tagSelection = new VisImage(tagSelectionTexture);
        tagSelection.setSize(DEFAULT_TAG_WIDTH, DEFAULT_TAG_HEIGHT);
        
        tagLists = new Group[beatNumber];
        editMode = false;
        for (int i = 0; i < tagLists.length; i++) {
            tagLists[i] = new Group();
            stage.addActor(tagLists[i]);
        }
        for(Group tagList : tagLists) {
            tagList = new Group();
            stage.addActor(tagList);
        }
        stage.addActor(this);
    }

    public BeatSlider(float min, float max, float stepSize, boolean vertical, Stage stage) {
        this(min, max, stepSize, vertical, stage, 1);
    }

    public void updateTags(final MusicConverter music, final Music musicFile){
        for (int i = 0; i < tagLists.length; i++) {
            Group tagList = tagLists[i];
            tagList.clearChildren();
            if (editMode) {
                tagList.setPosition(0, getY() + getHeight() + (20 + DEFAULT_TAG_HEIGHT) * i);
                tagCursor.setPosition(0, 0);
                tagSelection.setPosition(0, 0);

                for (int j = 0; j < music.getBeatTrace().length; j++) {
                    int beatTrace = music.getBeatTrace()[j];
                    tagList.addActor(new BeatSliderTag(j, i, beatTrace, new ClickListener() {
                        public void clicked(InputEvent event, float x, float y) {
                            musicFile.pause();
                            BeatSliderTag tag = (BeatSliderTag) event.getListenerActor();
                            System.out.println(tag.sliderIndex);
                            System.out.println(music.getMillisFromBeatTraceIndex(tag.getSliderIndex()));
                            long millis = music.getMillisFromBeatTraceIndex(tag.getSliderIndex());
                            setValue(millis);
                            musicFile.setPosition(millis / 1000f);
                        }
                    }));
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
    }

    public void update(MusicConverter music, Long millisPosition, float dt){
        long sliderValueMillis = Math.round(getValue());
        //float cursorX = (millisPosition.floatValue() / music.getDuration().floatValue()) * (DEFAULT_TAG_WIDTH * music.getBeatTrace().length);
        float cursorX = (sliderValueMillis / (getMaxValue() - getMinValue())) * (DEFAULT_TAG_WIDTH * music.getBeatTrace().length);
        tagCursor.setPosition(cursorX, 0);
        //long index = music.getBeatTraceIndexFromMillis(millisPosition);
        long index = music.getBeatTraceIndexFromMillis(sliderValueMillis);
        tagSelection.setPosition(index * DEFAULT_TAG_WIDTH, 0);

        for (int i = 0; i < tagLists.length; i++) {
            Group tagList = tagLists[i];
            
            boolean isCursorOverHalfScreen = tagCursor.getX() + tagList.getX() >= (Gdx.graphics.getWidth() / 6) * 5;
            boolean isRollOverScreen = (tagList.getX() + (DEFAULT_TAG_WIDTH * music.getBeatTrace().length)) >= Gdx.graphics.getWidth();

            if (isCursorOverHalfScreen && isRollOverScreen) {
                tagList.setX((Gdx.graphics.getWidth() / 6) * 5 - tagCursor.getX());
            }
            if ((tagList.getX() + (DEFAULT_TAG_WIDTH * music.getBeatTrace().length)) < Gdx.graphics.getWidth())
                tagList.setX(Gdx.graphics.getWidth() - (DEFAULT_TAG_WIDTH * music.getBeatTrace().length));

            boolean isCursorBeforeScreen = tagCursor.getX() + tagList.getX() <= (Gdx.graphics.getWidth() / 6);
            boolean isRollInScreen = tagList.getX() <= 0;

            if (isCursorBeforeScreen && isRollInScreen) {
                tagList.setX((Gdx.graphics.getWidth() / 6) - tagCursor.getX());
            }
            if (tagList.getX() > 0)
                tagList.setX(0);
        }
    }

    public void setEditMode(boolean editMode){
        this.editMode = editMode;
    }

    private class BeatSliderTag extends VisImage {
        private int sliderIndex;
        private int beatValue; // if its up or down the slider

        public BeatSliderTag(int tagIndex, int beatTraceIndex, int beatValue, ClickListener callback){
            if((beatValue & (MusicPlayerScreen.LEFT_BEAT | MusicPlayerScreen.RIGHT_BEAT)) == (MusicPlayerScreen.LEFT_BEAT | MusicPlayerScreen.RIGHT_BEAT))
                setDrawable(tagDoubleTexture);
            else if ((beatValue & (MusicPlayerScreen.LEFT_BEAT << beatTraceIndex)) == (MusicPlayerScreen.LEFT_BEAT << beatTraceIndex))
                setDrawable(tagUpTexture);
            else if ((beatValue & (MusicPlayerScreen.RIGHT_BEAT << beatTraceIndex)) == (MusicPlayerScreen.RIGHT_BEAT << beatTraceIndex))
                setDrawable(tagDownTexture);
            else
                setDrawable(tagTexture);

            this.sliderIndex = tagIndex;
            this.beatValue = beatValue;
            this.setSize(DEFAULT_TAG_WIDTH, DEFAULT_TAG_HEIGHT);
            this.setY(0);
            this.setX(sliderIndex * DEFAULT_TAG_WIDTH);

            this.addListener(callback);
        }

        public int getSliderIndex() {
            return sliderIndex;
        }
    }
}
