package club.icegames.towerwars.listeners;

import club.icegames.towerwars.TowerWarsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * @author Seailz
 */
public class BlockListener implements Listener {
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (TowerWarsPlugin.getInstance().inGame(e.getPlayer())) e.setCancelled(true);
    }
}
