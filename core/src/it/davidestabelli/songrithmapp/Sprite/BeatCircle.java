package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import it.davidestabelli.songrithmapp.MainGame;

public class BeatCircle{
    private Texture outerTexture;
    private Texture innerTexture;
    private ShapeRenderer circleRenderer;
    private Vector2 position;

    //private ParticleEffect effect;
    //private boolean isEffectRunning;
    private boolean activateEffect;    
    private List<ParticleEffect> particleEffects;
    private List<BeatCircleAnimation> circleAnimations;
    private float animationDuration;

    private boolean active;
    private boolean hidden;

    private boolean left;
    private float radius;

    public BeatCircle(Vector2 position, float radius, boolean left){
        this.outerTexture = new Texture("outer_circle.png");
        this.innerTexture = new Texture("inner_circle.png");
        this.circleRenderer = new ShapeRenderer();
        circleRenderer.setColor(Color.WHITE);
        this.position = position;
        this.radius = radius;
        this.active = false;

        this.particleEffects = new ArrayList<ParticleEffect>();
        this.circleAnimations = new ArrayList<BeatCircleAnimation>();
        this.animationDuration = BeatCircleAnimation.animationDuration(radius);
        this.hidden = false;
        this.left = left;
    }

    public void update(float dt){
        List<ParticleEffect> toRemoveEffect = new ArrayList<>();
        for (ParticleEffect effect : particleEffects) {
            effect.update(dt);
            if(effect.isComplete()) {
                toRemoveEffect.add(effect);
            }
        }
        for (ParticleEffect effect : toRemoveEffect) {
            effect.dispose();
            particleEffects.remove(effect);
        }

        List<BeatCircleAnimation> toRemoveCircle = new ArrayList<>();

        for (BeatCircleAnimation animation : circleAnimations)
            if (animation.update(dt))
                toRemoveCircle.add(animation);
        circleAnimations.removeAll(toRemoveCircle);
    }

    public void draw(SpriteBatch batch){
        if(!hidden){
            batch.end();
            if(active){
                circleRenderer.begin(ShapeType.Filled);
            } else {
                circleRenderer.begin(ShapeType.Line);
                
            }
            circleRenderer.circle(position.x, position.y, radius/2);
            circleRenderer.end();
            batch.begin();

            if(activateEffect) {
                for (ParticleEffect effect : particleEffects)
                    effect.draw(batch);
                for(BeatCircleAnimation animation : circleAnimations)
                    animation.draw(batch);
            }
        }
    }

    public void dispose(){
        innerTexture.dispose();
        outerTexture.dispose();
    }

    public void setActive(boolean active) {
        if (active && !this.active) {
            addParticleEffect();
        }
        this.active = active;
    }

    public float getRadius() {
        return radius;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
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
        if(!exist) {
            BeatCircleAnimation animation = new BeatCircleAnimation(position, radius, id, left);
            animation.setColor(circleRenderer.getColor());
            circleAnimations.add(animation);
        }
    }

    public void addParticleEffect(){
        ParticleEffect effect = new ParticleEffect();
        effect.load(Gdx.files.internal("circle_hit_particles"), Gdx.files.internal(""));
        effect.setPosition(position.x,position.y);
        effect.start();
        particleEffects.add(effect);
    }

    public float getAnimationDuration() {
        return animationDuration;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setColor(Color color){
        circleRenderer.setColor(color);

    }
}