package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import it.davidestabelli.songrithmapp.MainGame;

public class BeatCircle{
    public static final float CIRCLE_RADIUS = 150;
    public static final float END_ANIMATION_CIRCLE_RADIUS = 10;
    public static final float ANIMATION_RADIUS_DELTA = 200;

    private Texture texture;
    private Vector2 position;

    private float radius;

    public BeatCircle(Vector2 position){
        this.texture = new Texture("Circle_(transparent).png");
        this.position = position;
        this.radius = CIRCLE_RADIUS;
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
        batch.draw(texture,
                (position.x / MainGame.V_WIDTH) - (radius / MainGame.V_WIDTH)/2,
                (position.y / MainGame.V_HEIGHT) - (radius / MainGame.V_HEIGHT)/2,
                radius / MainGame.V_WIDTH,
                radius / MainGame.V_HEIGHT);
    }

    public void dispose(){
        texture.dispose();
    }
}