package it.davidestabelli.songrithmapp.Sprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import it.davidestabelli.songrithmapp.MainGame;

public class BeatCircle extends Sprite{
    public static final float CIRCLE_RADIOUS = 15;

    public World world;
    public Body b2body;

    public BeatCircle(World world, Vector2 position){
        this.world = world;

        Texture texture = new Texture("circle effect.png");
        setRegion(texture);

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(position.x / MainGame.PPM, position.y / MainGame.PPM);
        b2body = world.createBody(bdef);
        setBounds(0,0, CIRCLE_RADIOUS / MainGame.PPM, CIRCLE_RADIOUS / MainGame.PPM);
    }

    public void dispose(){
        world.destroyBody(b2body);
        getTexture().dispose();
    }
}