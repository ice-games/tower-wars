package club.icegames.towerwars.game;

import club.icegames.towerwars.TowerWarsPlugin;
import club.icegames.towerwars.core.exeptions.PlayerIsAlreadyInGameException;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;

public class GameBuilder {

    @Getter
    private Game game;

    public GameBuilder(Player player1, Player player2) {

        SingleTeam blue = new SingleTeam(player1, ChatColor.BLUE);
        SingleTeam red = new SingleTeam(player2, ChatColor.RED);

        try {
            game = new Game(
                    blue, red
            );
        } catch (PlayerIsAlreadyInGameException e) {
            e.printStackTrace();
        }
    }

    public void build() throws IOException { game.start(); }

}
