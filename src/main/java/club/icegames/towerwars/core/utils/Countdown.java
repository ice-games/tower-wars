package club.icegames.towerwars.core.utils;

import club.icegames.towerwars.TowerWarsPlugin;
import games.negative.framework.util.Task;
import games.negative.framework.util.Utils;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Countdown class
 * @author Seailz
 */
@Getter
public class Countdown {

    private final Player player;
    private int number = 5;

    public Countdown(Player player) {
        this.player = player;
        Task.taskRepeating(TowerWarsPlugin.getInstance(), 20, 20, this::sendCountdown);
    }

    private void sendCountdown() {
        if (number == 0) return;
        player.sendTitle(Utils.color("&d&l" + number), "", 0, 1, 0);
        number--;
    }
}
