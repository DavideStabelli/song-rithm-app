package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class BeatCircleAnimation {
    public static final float START_ANIMATION_CIRCLE_RADIUS = Gdx.graphics.getHeight() / 1.5f;
    public static final float ANIMATION_RADIUS_DELTA = 200;

    private Texture texture;

    private float radius;
    private float finalRadius;
    private Vector2 position;

    private long animationId;

    public BeatCircleAnimation(Vector2 position, float finalRadius, long id){
        this.texture = new Texture("outer_circle.png");
        this.position = position;
        this.radius = START_ANIMATION_CIRCLE_RADIUS;
        this.finalRadius = finalRadius;
        this.animationId = id;
    }

    public boolean update(float dt){
        radius -= ANIMATION_RADIUS_DELTA * dt;
        if(radius <= finalRadius){
            radius = finalRadius;
            return true;
        } else {
            return false;
        }
    }

    public void draw(SpriteBatch batch){
        batch.draw(texture,
                position.x - radius/2,
                position.y - radius/2,
                radius,
                radius);
    }

    public long getAnimationId() {
        return animationId;
    }

    public static float animationDuration(float finalRadius){
        return ((START_ANIMATION_CIRCLE_RADIUS - finalRadius) / ANIMATION_RADIUS_DELTA) * 1000;
    }
}
