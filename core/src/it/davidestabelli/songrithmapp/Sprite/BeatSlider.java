package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisSlider;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;

public class BeatSlider extends VisSlider {
    private BeatRoll[] beatRolls;

    private MusicConverter music;
    private Music musicFile;

    private boolean editMode;

    public BeatSlider(float min, float max, float stepSize, boolean vertical, Stage stage, int beatNumber, MusicConverter music, Music musicFile) {
        super(min, max, stepSize, vertical);

        this.music = music;
        this.musicFile = musicFile;

        beatRolls = new BeatRoll[beatNumber];
        editMode = false;
        for (int i = 0; i < beatRolls.length; i++) {
            beatRolls[i] = new BeatRoll(i, max, min);
            beatRolls[i].addTagClickAction(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    musicFile.pause();
                    BeatRollTile tag = (BeatRollTile) event.getListenerActor();
                    long millis = music.getMillisFromBeatTraceIndex(tag.getSliderIndex());
                    setValue(millis);
                    musicFile.setPosition(millis / 1000f);
                }
            });
            stage.addActor(beatRolls[i]);
        }
        stage.addActor(this);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        for (int i = 0; i < beatRolls.length; i++)
            beatRolls[i].setPosition(0, getY() + getHeight() + 20 + (20 + BeatRollTile.DEFAULT_TAG_HEIGHT + BeatRoll.MENU_BAR_HEIGHT) * i);
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

    public void setEditMode(boolean editMode){
        this.editMode = editMode;
    }
}
