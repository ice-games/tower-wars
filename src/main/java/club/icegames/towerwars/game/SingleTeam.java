package club.icegames.towerwars.game;

import club.icegames.towerwars.TowerWarsPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author Seailz
 */
@Getter
public class SingleTeam {
    private final Player player;
    private final ChatColor color;
    @Setter
    private int lives = 0;

    public SingleTeam(@NotNull Player player, @NotNull ChatColor color) {
        this.player = player;
        this.color = color;

        TowerWarsPlugin.getInstance().refresh();
        setLives(TowerWarsPlugin.getInstance().getLives());
    }
}
