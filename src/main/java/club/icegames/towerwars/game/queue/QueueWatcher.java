package club.icegames.towerwars.game.queue;

import club.icegames.towerwars.TowerWarsPlugin;
import club.icegames.towerwars.core.exeptions.PlayerIsAlreadyInGameException;
import club.icegames.towerwars.game.Game;
import club.icegames.towerwars.game.GameBuilder;
import club.icegames.towerwars.game.SingleTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * @author Seailz
 */
public class QueueWatcher {

    public QueueWatcher(int delay) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(TowerWarsPlugin.getInstance(), () -> {
            if (TowerWarsPlugin.getInstance().getQueue().size() >= 2) {

                new GameBuilder(
                        TowerWarsPlugin.getInstance().getQueue().get(0),
                        TowerWarsPlugin.getInstance().getQueue().get(1)
                ).build();

                TowerWarsPlugin.getInstance().getQueue().remove(0);
                TowerWarsPlugin.getInstance().getQueue().remove(1);
            }
        }, delay, delay);
    }

}
