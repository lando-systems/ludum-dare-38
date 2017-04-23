package lando.systems.ld38.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import lando.systems.ld38.managers.ActionManager;
import lando.systems.ld38.turns.ActionTypeMove;
import lando.systems.ld38.turns.TurnAction;
import lando.systems.ld38.ui.Button;
import lando.systems.ld38.turns.*;
import lando.systems.ld38.ui.EndTurnButton;
import lando.systems.ld38.ui.PlayerSelectionHud;
import lando.systems.ld38.utils.Assets;
import lando.systems.ld38.utils.Config;
import lando.systems.ld38.utils.SoundManager;
import lando.systems.ld38.world.*;

//import static com.sun.glass.ui.gtk.GtkApplication.screen;

/**
 * Created by Brian on 4/16/2017
 */
public class GameScreen extends BaseScreen {

    public TextureRegion debugTex;
    public World world;
    public UserResources resources;
    public TurnCounter turnCounter;
    public Array<Tile> adjacentTiles;
    public EndTurnButton endTurnButton;
    public PlayerSelectionHud playerSelection;
    public Player selectedPlayer;

    public FrameBuffer pickBuffer;
    public TextureRegion pickRegion;
    public Pixmap pickPixmap;
    public Color pickColor;
    public int pickMapScale = 8;

    public boolean alternate = true;
    public int turn;
    public Array<TurnAction> turnActions;
    float time;

    public Vector3 cameraTouchStart;
    public Vector3 touchStart;
    public static float zoomScale = 0.15f;
    public static float maxZoom = 1.6f;
    public static float minZoom = 0.2f;

    public boolean cancelTouchUp = false;

    public Button testingButton;

    public ActionManager actionManager = new ActionManager();

    public GameScreen() {
        super();
        debugTex = Assets.whitePixel;
        time = 0;
        world = new World();
        resources = new UserResources();
        turnCounter = new TurnCounter(hudCamera);
        adjacentTiles = new Array<Tile>();
        turn = 0;
        turnActions = new Array<TurnAction>();
        pickBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Config.gameWidth / pickMapScale, Config.gameHeight / pickMapScale, false, false);
        pickRegion = new TextureRegion(pickBuffer.getColorBufferTexture());
        pickRegion.flip(false, true);
        pickPixmap = null;
        pickColor = new Color();

        endTurnButton = new EndTurnButton(new Rectangle(hudCamera.viewportWidth - 100 - 10, 10, 100, 30), hudCamera);
        playerSelection = new PlayerSelectionHud(this);
//        testingButton = new Button(Assets.whitePixel, new Rectangle(50,50,50,50), hudCamera, "Too much Text!", "Tooltip");
        cameraTouchStart = new Vector3();
        touchStart = new Vector3();
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        SoundManager.update(dt);

        time += dt;
        world.update(dt);
        endTurnButton.update(dt);
        playerSelection.update(dt);

//        if (Gdx.input.justTouched()) {
//            Player character = world.players.first();
//            int toCol = character.col + (alternate ? 1 : 0);
//            int toRow = character.row + (alternate ? 0 : 1);
//            TurnAction turnAction = new TurnAction();
//            turnAction.character = character;
//            turnAction.action = new ActionTypeMove(turnAction, toCol, toRow);
//            turnActions.add(turnAction);
//            alternate = !alternate;
//        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            endTurn();
//            for (Tile tile : adjacentTiles) {
//                tile.isHighlighted = false;
//            }
//            adjacentTiles.clear();
//            adjacentTiles.addAll(world.getNeighbors(world.players.first().row, world.players.first().col));
//            for (Tile tile : adjacentTiles) {
//                tile.isHighlighted = true;
//            }
        }

        if (pickPixmap != null) {
            pickPixmap.dispose();
        }

        actionManager.update(dt);
        updateCamera();

        // NOTE: Sound DEBUG
//        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) SoundManager.playSound(SoundManager.SoundOptions.button_select);
//        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) SoundManager.playSound(SoundManager.SoundOptions.ladder);
//        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) SoundManager.playSound(SoundManager.SoundOptions.ocean_waves);
//        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) SoundManager.playSound(SoundManager.SoundOptions.player_move);
//        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) SoundManager.playSound(SoundManager.SoundOptions.resource_collected);
//        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) SoundManager.playSound(SoundManager.SoundOptions.seagull);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        cameraTouchStart.set(camera.position);
        touchStart.set(screenX, screenY, 0);

        return true;
    }

    public GridPoint2 getGridPosition(int screenX, int screenY) {
        int x = screenX;
        int y = Gdx.graphics.getHeight() - screenY;
        pickColor.set(pickPixmap.getPixel(x / pickMapScale, y / pickMapScale));
        int col = (int) (pickColor.r * (255f / 5f));
        int row = (int) (pickColor.g * (255f / 5f));
        return new GridPoint2(col, row);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        camera.position.x = cameraTouchStart.x + (touchStart.x - screenX) * camera.zoom;
        camera.position.y = cameraTouchStart.y + (screenY - touchStart.y) * camera.zoom;
        cancelTouchUp = true;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (cancelTouchUp) {
            cancelTouchUp = false;
            return false;
        }

        if (endTurnButton.checkForTouch(screenX, screenY)) {
            endTurnButton.handleTouch();
            endTurn();
        } else {
            GridPoint2 location =  getGridPosition(screenX, screenY);
            if (!handleMove(location)) {
                clearMovement();
                PendingAction action = actionManager.handleTouch(world, location, screenX, screenY, camera);
                handleAction(action);
            }
        }

        return false;
    }



    private boolean handleMove(GridPoint2 location) {
        Tile tile = world.getTile(location);
        if (adjacentTiles.contains(tile, true)) {
            TurnAction turnAction = new TurnAction();
            turnAction.character = selectedPlayer;
            turnAction.action = new ActionTypeMove(turnAction, tile.col, tile.row);
            turnActions.add(turnAction);
            clearMovement();
            return true;
        }
        return false;
    }

    private void handleAction(PendingAction action) {
        selectedPlayer = action.player;

        switch (action.action) {
            case displayMoves:
                showMovement(selectedPlayer);
                break;
        }

    }

    private void showMovement(Player player) {
        adjacentTiles.addAll(world.getNeighbors(player.row, player.col));
        for (Tile tile : adjacentTiles) {
            tile.isHighlighted = true;
        }
    }

    private void clearMovement() {
        for (Tile tile : adjacentTiles) {
            tile.isHighlighted = false;
        }
        adjacentTiles.clear();
    }


    Vector3 tp = new Vector3();
    @Override
    public boolean scrolled (int change) {
        camera.unproject(tp.set(Gdx.input.getX(), Gdx.input.getY(), 0 ));
        float px = tp.x;
        float py = tp.y;
        camera.zoom += change * camera.zoom * zoomScale;
        updateCamera();

        camera.unproject(tp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        camera.position.add(px - tp.x, py- tp.y, 0);
        camera.update();
        return true;
    }

    private void updateCamera(){
        camera.zoom = MathUtils.clamp(camera.zoom, minZoom, maxZoom);
        float minY = world.bounds.y + camera.viewportHeight/2 * camera.zoom;
        float maxY = world.bounds.height - camera.viewportHeight/2 * camera.zoom;

        float minX = world.bounds.x + camera.viewportWidth/2 * camera.zoom;
        float maxX = world.bounds.x + world.bounds.width - camera.viewportWidth/2 * camera.zoom;

        if (camera.viewportHeight * camera.zoom > world.bounds.height){
            camera.position.y = world.bounds.height/2;
        } else {
            camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
        }


        if (camera.viewportWidth * camera.zoom > world.bounds.width){
            camera.position.x = world.bounds.x + world.bounds.width/2;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
        }

        camera.update();
    }


    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(Config.bgColor.r, Config.bgColor.g, Config.bgColor.b, Config.bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw world
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        {
            world.render(batch);
            actionManager.render(batch);
        }
        batch.end();

        // Draw picking frame buffer
        pickBuffer.begin();
        {
            Gdx.gl.glClearColor(0f, 0f, 1f, 0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            world.renderPickBuffer(batch);
            batch.end();
        }
        pickPixmap = ScreenUtils.getFrameBufferPixmap(0, 0, pickBuffer.getWidth(), pickBuffer.getHeight());
        pickBuffer.end();

        // Draw HUD
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        {
            playerSelection.render(batch);
            resources.render(batch);
            turnCounter.render(batch, turn);
            endTurnButton.render(batch);
            testingButton.render(batch);
            Assets.font.draw(batch, String.valueOf(Gdx.graphics.getFramesPerSecond()), 3, 16);

            // Draw pick region stuff
//            batch.setColor(pickColor);
//            batch.draw(Assets.whitePixel, hudCamera.viewportWidth - 100 - 50, 0, 50, 50);
//            batch.setColor(Color.WHITE);
//            batch.draw(pickRegion, hudCamera.viewportWidth - 100, 0, 100, 100);
        }
        batch.end();
    }

    private void endTurn() {
        for (TurnAction turnAction : turnActions) {
            turnAction.doAction();
        }
        turnActions.clear();
        ++turn;
        world.endTurn();
    }
}
