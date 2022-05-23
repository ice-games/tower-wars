package club.icegames.towerwars.game;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Getter
public class Team {
    private final Player player;
    private final ChatColor color;

    public Team(@NotNull Player player, @NotNull ChatColor color) {
        this.player = player;
        this.color = color;
    }
}
