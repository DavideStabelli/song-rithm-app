package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

public class BeatCircleAnimation {
    public static final float START_ANIMATION_CIRCLE_DIAMETER_FRACTION = 2.5f;
    public static final float ANIMATION_DIAMETER_DELTA = 60;

    private Texture texture;
    private ShapeRenderer circleRenderer;

    private float diameter;
    private float finalDiameter;
    private float startDiameter;
    private Vector2 finalPosition;
    private Vector2 position;
    private boolean left;

    private long animationId;

    public BeatCircleAnimation(Vector2 position, float finalDiameter, long id, boolean left){
        this.texture = new Texture("outer_circle.png");

        this.circleRenderer = new ShapeRenderer();
        circleRenderer.setColor(Color.BLACK);
        Gdx.gl.glLineWidth(3);

        this.finalPosition = position;
        this.startDiameter = finalDiameter / START_ANIMATION_CIRCLE_DIAMETER_FRACTION;
        this.finalDiameter = finalDiameter;
        this.diameter = startDiameter;
        this.animationId = id;
        this.position = new Vector2(position.x, 0);
        this.left = left;
    }

    public boolean update(float dt){
        if(startDiameter > finalDiameter)
            diameter -= ANIMATION_DIAMETER_DELTA * dt;
        else
            diameter += ANIMATION_DIAMETER_DELTA * dt;
        float diameterDelta = Math.abs(this.diameter/2 - this.finalDiameter/2);
        float baseDelta = Math.abs(startDiameter/2 - this.finalDiameter/2);
        float multiplier = diameterDelta / baseDelta;
        //if(left)
            //this.position.x = this.finalPosition.x - (this.finalPosition.x) * multiplier;
        //else
            this.position.y = this.finalPosition.y + (Gdx.graphics.getHeight() - this.finalPosition.y) * multiplier;
        if(startDiameter >= finalDiameter && diameter <= finalDiameter){
            diameter = finalDiameter;
            return true;
        } else if(startDiameter < finalDiameter && diameter >= finalDiameter){
            diameter = finalDiameter;
            return true;
        } else {
            return false;
        }
    }

    public void draw(SpriteBatch batch){
        /*batch.draw(texture,
                position.x - diameter/2,
                position.y - diameter/2,
                diameter,
                diameter);*/
        batch.end();
        circleRenderer.begin(ShapeType.Line);
        circleRenderer.circle(position.x, position.y, diameter/2);
        circleRenderer.end();
        batch.begin();
    }

    public long getAnimationId() {
        return animationId;
    }

    public static float animationDuration(float finalRadius){
        return (Math.abs((finalRadius / START_ANIMATION_CIRCLE_DIAMETER_FRACTION) - finalRadius) / Math.abs(ANIMATION_DIAMETER_DELTA)) * 1000;
    }
}
