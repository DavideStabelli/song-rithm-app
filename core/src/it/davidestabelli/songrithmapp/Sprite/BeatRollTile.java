package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisImage;
import it.davidestabelli.songrithmapp.Screens.MusicPlayerScreen;

public class BeatRollTile extends VisImage {
    public static final int DEFAULT_TAG_WIDTH = 20;
    public static final int DEFAULT_TAG_HEIGHT = Gdx.graphics.getHeight() / 8;

    private int sliderIndex;
    private int beatTraceIndex;

    private static Texture tagTexture = new Texture("beat_slider_zoom_tile.png");
    private static Texture tagUpTexture = new Texture("beat_slider_zoom_tile_up.png");
    private static Texture tagDownTexture = new Texture("beat_slider_zoom_tile_down.png");
    private static Texture tagDoubleTexture = new Texture("beat_slider_zoom_tile_double.png");

    public BeatRollTile(int tagIndex, int beatTraceIndex, int beatValue, ClickListener callback){
        int leftBeatValue = (MusicPlayerScreen.LEFT_BEAT << (2*beatTraceIndex));
        int rightBeatValue = (MusicPlayerScreen.RIGHT_BEAT << (2*beatTraceIndex));
        if((beatValue & (leftBeatValue | rightBeatValue)) == (leftBeatValue | rightBeatValue))
            setDrawable(tagDoubleTexture);
        else if ((beatValue & leftBeatValue) == leftBeatValue)
            setDrawable(tagUpTexture);
        else if ((beatValue & rightBeatValue) == rightBeatValue)
            setDrawable(tagDownTexture);
        else
            setDrawable(tagTexture);

        this.sliderIndex = tagIndex;
        this.beatTraceIndex = beatTraceIndex;
        this.setSize(DEFAULT_TAG_WIDTH, DEFAULT_TAG_HEIGHT);
        this.setY(0);
        this.setX(sliderIndex * DEFAULT_TAG_WIDTH);

        if(callback != null)
            this.addListener(callback);
    }

    public int getSliderIndex() {
        return sliderIndex;
    }

    public int getBeatTraceIndex() {
        return beatTraceIndex;
    }
}
