package club.icegames.towerwars.listeners;

import club.icegames.towerwars.TowerWarsPlugin;
import club.icegames.towerwars.game.Game;
import club.icegames.towerwars.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author Seailz
 */
public class MoveListener implements Listener {

    /**
     * Stops player movement in-case the game hasn't started yet.
     * @param e the event
     */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (TowerWarsPlugin.getInstance().getInGame().containsKey(e.getPlayer())) {
            Game g = TowerWarsPlugin.getInstance().getInGame().get(e.getPlayer());
            if (g.getState() == GameState.STARTING) {
                e.setCancelled(true);
            }
        }
    }

}
