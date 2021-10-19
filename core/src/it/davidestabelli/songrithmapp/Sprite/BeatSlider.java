package it.davidestabelli.songrithmapp.Sprite;

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
    private static final int DEFAULT_TAG_SIZE = 10;

    private Texture tagTexture;

    private Group tagList;

    private boolean editMode;

    public BeatSlider(float min, float max, float stepSize, boolean vertical, Stage stage) {
        super(min, max, stepSize, vertical);

        tagTexture = new Texture("inner_circle.png");
        tagList = new Group();
        editMode = false;

        stage.addActor(tagList);
        stage.addActor(this);
    }

    public void updateTags(MusicConverter music){
        tagList.clearChildren();
        if(editMode) {
            for (int i = 0; i < music.getBeatTrace().length; i++) {
                int beatTrace = music.getBeatTrace()[i];
                if ((beatTrace & MusicPlayerScreen.LEFT_BEAT) == MusicPlayerScreen.LEFT_BEAT)
                    tagList.addActor(new BeatSliderTag(this, music.getMillisFromBeatTraceIndex(i) / 1000, true));

                if ((beatTrace & MusicPlayerScreen.RIGHT_BEAT) == MusicPlayerScreen.RIGHT_BEAT)
                    tagList.addActor(new BeatSliderTag(this, music.getMillisFromBeatTraceIndex(i) / 1000, false));
            }
        }
    }

    public void addTag(float position, boolean isLeft){
        tagList.addActor(new BeatSliderTag(this, position, isLeft));
    }

    public void setEditMode(boolean editMode){
        this.editMode = editMode;
    }

    private class BeatSliderTag extends VisImage {
        private BeatSlider parentSlider;
        private float sliderPosition;
        private boolean upTheSlider; // if its up or down the slider

        public BeatSliderTag(BeatSlider parentSlider, float sliderPosition, boolean upTheSlider){
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
        }

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
    }
}
