package lando.systems.ld38.managers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld38.turns.ActionType;
import lando.systems.ld38.turns.ActionTypeMove;
import lando.systems.ld38.turns.Actions;
import lando.systems.ld38.turns.PendingAction;
import lando.systems.ld38.ui.ActionMenu;
import lando.systems.ld38.ui.OptionButton;
import lando.systems.ld38.world.Player;
import lando.systems.ld38.world.Tile;
import lando.systems.ld38.world.World;

/**
 * Created by Brian on 4/23/2017.
 */

public class ActionManager {

    private Array<ActionMenu> playerOptions = new Array<ActionMenu>();

    public void update(float dt) {
        for (int i = playerOptions.size - 1; i >= 0; i--) {
            playerOptions.get(i).update(dt);
            if (playerOptions.get(i).isComplete()) {
                playerOptions.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (ActionMenu menu : playerOptions) {
            menu.render(batch);
        }
    }
    
    public void showOptions(Player player, OrthographicCamera camera) {
        for (ActionMenu menu : playerOptions) {
            if (menu.player == player) return;
        }

        for (ActionMenu menu : playerOptions) {
            menu.hide();
        }

        if (player == null) return;

        // determine available options from tile - for now, use all three
        Array<OptionButton> optionButtons = new Array<OptionButton>(3);

        Tile tile = player.world.getTile(player.row, player.col);
        Vector3 position = tile.position;

        float x = position.x + (player.tileWidth / 2);
        float y = position.y + player.tileHeight * 2;

        Rectangle buttonBounds = new Rectangle(x, y, 20, 20);
        optionButtons.add(new OptionButton("Move", buttonBounds, Actions.displayMoves, camera));
        if (tile != null) {
            optionButtons.add(new OptionButton("Build", buttonBounds, Actions.displayBuild, camera));
            optionButtons.add(new OptionButton("Harvest", buttonBounds, Actions.harvest, camera));
        }

        playerOptions.add(new ActionMenu(player, optionButtons));
    }

    public PendingAction handleTouch(World world, GridPoint2 location, int screenX, int screenY, OrthographicCamera camera) {

        for (ActionMenu menu : playerOptions) {
            if (menu.handleTouch(screenX, screenY)) {
                menu.hide();
                if (menu.pendingAction.action == Actions.displayBuild) {
                    // show build options
                }
                return menu.pendingAction;
            }
        }

        Array<Player> players = world.getPlayers(location);
        if (players.size == 0) {
            showOptions(null, camera);
        } else {
            // will have to z order players on moveTo and grab top player - when going to the player from the character
            // menu, reorder that player on top
            Player player = players.get(0);

            showOptions(player, camera);
        }
        return new PendingAction();
    }
}
