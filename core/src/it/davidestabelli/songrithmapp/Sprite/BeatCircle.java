package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import it.davidestabelli.songrithmapp.MainGame;

public class BeatCircle{
    public static final float CIRCLE_RADIUS = 150;
    public static final float END_ANIMATION_CIRCLE_RADIUS = 10;
    public static final float ANIMATION_RADIUS_DELTA = 200;

    private Texture outerTexture;
    private Texture innerTexture;
    private Vector2 position;

    public boolean active;

    private float radius;

    public BeatCircle(Vector2 position, float radius){
        this.outerTexture = new Texture("outer_circle.png");
        this.innerTexture = new Texture("inner_circle.png");
        this.position = position;
        this.radius = radius;
        this.active = false;
    }

    public boolean doAnimation(float dt){
        radius -= ANIMATION_RADIUS_DELTA * dt;
        if(radius <= END_ANIMATION_CIRCLE_RADIUS){
            radius = CIRCLE_RADIUS;
            return true;
        } else {
            return false;
        }
    }

    public void draw(SpriteBatch batch){
        if(active){
            batch.draw(innerTexture,
                    position.x - radius/2,
                    position.y - radius/2,
                    radius,
                    radius);
        } else {
            batch.draw(outerTexture,
                    position.x - radius/2,
                    position.y - radius/2,
                    radius,
                    radius);
        }
    }

    public void dispose(){
        innerTexture.dispose();
        outerTexture.dispose();
    }
}