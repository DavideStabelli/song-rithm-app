package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisRadioButton;
import com.kotcrab.vis.ui.widget.VisSlider;
import it.davidestabelli.songrithmapp.Helper.Configurations;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;
import it.davidestabelli.songrithmapp.Helper.SimpleAudioPlayer;

import java.time.LocalTime;

import static it.davidestabelli.songrithmapp.Screens.MusicPlayerScreen.LEFT_BEAT;
import static it.davidestabelli.songrithmapp.Screens.MusicPlayerScreen.RIGHT_BEAT;

public class BeatSlider extends VisSlider {
    private BeatRoll[] beatRolls;
    private Group beatRollsGroup;
    private VisRadioButton[] barSelector;
    private VisLabel sliderTimeInfo;

    private MusicConverter music;
    //private Music musicFile;
    private SimpleAudioPlayer musicPlayer;

    private boolean editMode;
    private Stage stage;

    public BeatSlider(float min, float max, float stepSize, boolean vertical, Stage stage, MusicConverter music, SimpleAudioPlayer player) {
        super(min, max, stepSize, vertical);

        this.music = music;
        this.musicPlayer = player;

        this.editMode = false;
        this.stage = stage;
        this.beatRollsGroup = new Group();
        this.sliderTimeInfo = new VisLabel("");
        sliderTimeInfo.setVisible(false);
        stage.addActor(sliderTimeInfo);
        addListener(new ClickListener(){
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                String stringPosition = LocalTime.ofSecondOfDay(Math.round(getValue())).format(MusicConverter.AUDIO_FORMAT);
                sliderTimeInfo.setText(stringPosition);
                GlyphLayout layoutLabel = sliderTimeInfo.getGlyphLayout();
                float sliderX = (getValue() * getWidth() / (getMaxValue() - getMinValue())) + getX() - layoutLabel.width / 2;
                sliderTimeInfo.setPosition(sliderX,getY() + getHeight() + layoutLabel.height);
                super.touchDragged(event, x, y, pointer);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                float sliderX = (getValue() * getWidth() / (getMaxValue() - getMinValue())) + getX();
                sliderTimeInfo.setPosition(sliderX,getY() + getHeight() + 15);
                String stringPosition = LocalTime.ofSecondOfDay(Math.round(getValue())).format(MusicConverter.AUDIO_FORMAT);
                sliderTimeInfo.setText(stringPosition);
                sliderTimeInfo.setVisible(true);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                sliderTimeInfo.setVisible(false);
                super.touchUp(event, x, y, pointer, button);
            }

        });

        resetRolls();

        stage.addActor(beatRollsGroup);
        stage.addActor(this);
    }

    public void resetRolls(){
        beatRollsGroup.clearChildren();

        int beatNumber = music.getNumberOfBeatTraces();
        this.beatRolls = new BeatRoll[beatNumber];
        this.barSelector = new VisRadioButton[beatNumber];
        for (int i = 0; i < beatRolls.length; i++) {
            barSelector[i] = new VisRadioButton("");
            barSelector[i].addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    for(VisRadioButton selector : barSelector)
                        selector.setChecked(false);
                    VisRadioButton selector = (VisRadioButton) event.getListenerActor();
                    selector.setChecked(true);
                }
            });

            beatRolls[i] = new BeatRoll(i, barSelector[i], getMaxValue(), getMinValue(), this);
            beatRolls[i].addTagClickAction(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    musicPlayer.pause();
                    BeatRollTile tag = (BeatRollTile) event.getListenerActor();
                    double seconds = music.getSecondsFromBeatTraceIndex(tag.getSliderIndex());
                    setValue(Double.valueOf(seconds).floatValue());
                    musicPlayer.setSecondPosition(getValue());

                    for(VisRadioButton selector : barSelector)
                        selector.setChecked(false);
                    barSelector[tag.getBeatTraceIndex()].setChecked(true);
                }
            });

            beatRollsGroup.addActor(beatRolls[i]);
        }

        barSelector[0].setChecked(true);
    }

    public void handleInput(Configurations configs){
        if(!isTextBoxSelected()) {
            double secondsPosition = (musicPlayer.getSecondsPosition());
            int beatTrace = music.getBeatTrace(secondsPosition);
            if (Gdx.input.isKeyJustPressed(configs.editLeftBeatKey)) {
                music.setBeatTrace(secondsPosition, LEFT_BEAT << (2 * getSelectedBeatIndex()), true);
                updateTags();
            }
            if (Gdx.input.isKeyJustPressed(configs.editRightBeatKey)) {
                music.setBeatTrace(secondsPosition, RIGHT_BEAT << (2 * getSelectedBeatIndex()), true);
                updateTags();
            }
            if (Gdx.input.isKeyJustPressed(configs.deleteBeatKey)) {
                int value = LEFT_BEAT + RIGHT_BEAT;
                value = value << (2 * getSelectedBeatIndex());
                value = value ^ 255;
                value = beatTrace & value;
                music.setBeatTrace(secondsPosition, value, false);
                updateTags();
            }
        }
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        beatRollsGroup.setPosition(0, getY() + getHeight() + 20);
        for (int i = 0; i < beatRolls.length; i++)
            beatRolls[i].setPosition(0, (20 + BeatRollTile.DEFAULT_TAG_HEIGHT + BeatRoll.MENU_BAR_HEIGHT) * i);
    }

    public void updateTags(){
        for (int i = 0; i < beatRolls.length; i++) {
            BeatRoll beatRoll = beatRolls[i];
            beatRoll.clearChildren();
            if (editMode)
                beatRoll.updateTags(music);
        }
    }

    public void update(MusicConverter music){
        for (int i = 0; i < beatRolls.length; i++) {
            BeatRoll beatRoll = beatRolls[i];
            beatRoll.update(music, getValue());
        }
    }

    public int getSelectedBeatIndex(){
        for (int i = 0; i < barSelector.length; i++) {
            if(barSelector[i].isChecked())
                return i;
        }
        return 0;
    }

    public boolean isTextBoxSelected(){
        BeatRoll selectedRoll = beatRolls[getSelectedBeatIndex()];
        return selectedRoll.isNameFieldFocused();
    }

    public int getBeatRollsLenght(){
        return beatRolls.length;
    }

    public void setEditMode(boolean editMode){
        this.editMode = editMode;
    }

    public MusicConverter getMusic() {
        return music;
    }
}
