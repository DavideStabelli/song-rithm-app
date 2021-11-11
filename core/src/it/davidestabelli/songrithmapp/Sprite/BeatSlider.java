package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisRadioButton;
import com.kotcrab.vis.ui.widget.VisSlider;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;

public class BeatSlider extends VisSlider {
    private BeatRoll[] beatRolls;
    private Group beatRollsGroup;
    private VisRadioButton[] barSelector;

    private MusicConverter music;
    private Music musicFile;

    private boolean editMode;
    private Stage stage;

    public BeatSlider(float min, float max, float stepSize, boolean vertical, Stage stage, MusicConverter music, Music musicFile) {
        super(min, max, stepSize, vertical);

        this.music = music;
        this.musicFile = musicFile;

        this.editMode = false;
        this.stage = stage;
        this.beatRollsGroup = new Group();

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
                    musicFile.pause();
                    BeatRollTile tag = (BeatRollTile) event.getListenerActor();
                    long millis = music.getMillisFromBeatTraceIndex(tag.getSliderIndex());
                    setValue(millis);
                    musicFile.setPosition(millis / 1000f);

                    for(VisRadioButton selector : barSelector)
                        selector.setChecked(false);
                    barSelector[tag.getBeatTraceIndex()].setChecked(true);
                }
            });

            beatRollsGroup.addActor(beatRolls[i]);
        }

        barSelector[0].setChecked(true);
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
                beatRoll.updateTags(music, musicFile);
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
