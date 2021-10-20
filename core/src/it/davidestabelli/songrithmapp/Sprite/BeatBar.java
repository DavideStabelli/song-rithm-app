package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class BeatBar {
    private static final float DEFAULT_BEAT_BLOCK_HEIGHT = 20;
    private static final float DEFAULT_BEAT_BLOCK_SPACE = 10;
    private static final float DEFAULT_MAX_VALUE = 100;
    private static final float DEFAULT_MIN_VALUE = 0;

    private Texture beatBlockTexture;

    private Vector2 size;
    private Vector2 positon;

    private float beatBlockHeight;
    private float beatBlockSpace;

    private float maxValue;
    private float minValue;
    private float actualValue;
    private boolean hidden;

    public BeatBar(float x, float y, float width, float height) {
        this.size = new Vector2(width,height);
        this.positon = new Vector2(x,y);

        beatBlockTexture = new Texture("beat_block.png");

        this.beatBlockHeight = DEFAULT_BEAT_BLOCK_HEIGHT;
        this.beatBlockSpace = DEFAULT_BEAT_BLOCK_SPACE;
        this.maxValue = DEFAULT_MAX_VALUE;
        this.minValue = DEFAULT_MIN_VALUE;
        this.hidden = false;
    }

    public void draw(SpriteBatch batch){
        if(!hidden){
            float actualHeight = ((actualValue - minValue) * size.y) / (maxValue - minValue);
            int blockToDraw = Math.round(actualHeight / (beatBlockHeight + beatBlockSpace));

            for(int i = 0; i < blockToDraw; i++){
                float blockYPosition = positon.y + (i * (beatBlockHeight + beatBlockSpace));
                batch.draw(beatBlockTexture, positon.x, blockYPosition, size.x, beatBlockHeight);
            }
        }
    }    

    public void setBeatBlockHeight(float beatBlockHeight) {
        this.beatBlockHeight = beatBlockHeight;
    }

    public void setBeatBlockSpace(float beatBlockSpace) {
        this.beatBlockSpace = beatBlockSpace;
    }

    public void setActualValue(float actualValue) {
        this.actualValue = actualValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public Vector2 getSize() {
        return size;
    }

    public void setSize(Vector2 size) {
        this.size = size;
    }

    public Vector2 getPositon() {
        return positon;
    }

    public void setPositon(Vector2 positon) {
        this.positon = positon;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}