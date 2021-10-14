package it.davidestabelli.songrithmapp.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import it.davidestabelli.songrithmapp.MainGame;
import it.davidestabelli.songrithmapp.Sprite.BeatCircle;

public class PlayScreen implements Screen{

    public MainGame game;

    private OrthographicCamera gamecam;
    private Viewport gamePort;

    private BeatCircle beatCircleSx;
    private BeatCircle beatCircleDx;

    private boolean isCircleAnimationActive;

    public PlayScreen (MainGame mainGame){
        this.game = mainGame;

        gamecam = new OrthographicCamera();
        gamePort = new FitViewport(MainGame.V_WIDTH / MainGame.PPM, MainGame.V_HEIGHT / MainGame.PPM, gamecam); 
        
        gamecam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        beatCircleSx = new BeatCircle(new Vector2(-MainGame.V_WIDTH/2, 0));
        beatCircleDx = new BeatCircle(new Vector2(MainGame.V_WIDTH/2, 0));

        isCircleAnimationActive = false;
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub

    }

    public void handleInput(float dt) {
        if(Gdx.input.justTouched()){
            isCircleAnimationActive = true;
        }
    }

    public void update(float dt) {
        handleInput(dt);

        if(isCircleAnimationActive){
            isCircleAnimationActive = !beatCircleSx.doAnimation(dt);
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();

        beatCircleSx.draw(game.batch);
        beatCircleDx.draw(game.batch);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        beatCircleDx.dispose();
        beatCircleSx.dispose();
    }
    
}