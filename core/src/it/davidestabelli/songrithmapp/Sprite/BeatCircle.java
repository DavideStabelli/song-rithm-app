package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import it.davidestabelli.songrithmapp.MainGame;

public class BeatCircle{
    public static final float START_ANIMATION_CIRCLE_RADIUS = Gdx.graphics.getHeight() / 2;
    public static final float ANIMATION_RADIUS_DELTA = 200;

    private Texture outerTexture;
    private Texture innerTexture;
    private Vector2 position;
    private ParticleEffect effect;

    private boolean activateEffect;
    private List<BeatCircleAnimation> circleAnimations;
    private float animationDuration;

    private boolean active;

    private float radius;

    public BeatCircle(Vector2 position, float radius){
        this.outerTexture = new Texture("outer_circle.png");
        this.innerTexture = new Texture("inner_circle.png");
        this.position = position;
        this.radius = radius;
        this.active = false;
        this.effect = new ParticleEffect();
        this.effect.load(Gdx.files.internal("circle_hit_particles"), Gdx.files.internal(""));
        this.effect.getEmitters().first().setPosition(position.x,position.y);

        this.circleAnimations = new ArrayList<BeatCircleAnimation>();
        this.animationDuration = BeatCircleAnimation.animationDuration(radius);
    }

    public void update(float dt){
        effect.update(dt);

        List<BeatCircleAnimation> toRemove = new ArrayList<>();

        for (BeatCircleAnimation animation : circleAnimations)
            if (animation.update(dt))
                toRemove.add(animation);
        circleAnimations.removeAll(toRemove);
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
        if(activateEffect) {
            effect.draw(batch);
            for(BeatCircleAnimation animation : circleAnimations)
                animation.draw(batch);
        }
    }

    public void dispose(){
        innerTexture.dispose();
        outerTexture.dispose();
    }

    public void setActive(boolean active) {
        if (active && !this.active) {
            effect.start();
        }
        this.active = active;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
        effect.getEmitters().first().setPosition(position.x,position.y);
    }

    public void setActivateEffect(boolean activateEffect) {
        this.activateEffect = activateEffect;
    }

    public void addCircleAnimation(long id){
        boolean exist = false;
        for(BeatCircleAnimation animation : circleAnimations) {
            if (animation.getAnimationId() == id) {
                exist = true;
                break;
            }
        }
        if(!exist)
            circleAnimations.add(new BeatCircleAnimation(position, radius, id));
    }

    public float getAnimationDuration() {
        return animationDuration;
    }
}