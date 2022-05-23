package club.icegames.towerwars.game.queue;

import club.icegames.towerwars.TowerWarsPlugin;
import club.icegames.towerwars.game.Game;
import club.icegames.towerwars.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class QueueWatcher {

    public QueueWatcher(int delay) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(TowerWarsPlugin.getInstance(), () -> {
            if (TowerWarsPlugin.getInstance().getQueue().size() >= 2) {
                ArrayList<Player> players = new ArrayList<>();
                players.add(TowerWarsPlugin.getInstance().getQueue().get(0));
                players.add(TowerWarsPlugin.getInstance().getQueue().get(1));

                Team blue = new Team(players.get(0), ChatColor.BLUE);
                Team red = new Team(players.get(0), ChatColor.RED);

                // Remove the players from the queue
                TowerWarsPlugin.getInstance().getQueue().remove(0);
                TowerWarsPlugin.getInstance().getQueue().remove(1);

                new Game(
                        players, blue, red
                ).start();
            }
        }, delay, delay);
    }

}
