package club.icegames.towerwars.commands.main.sub;


import club.icegames.towerwars.game.GameBuilder;
import club.icegames.towerwars.gui.settings.SettingsMainGUI;
import games.negative.framework.command.SubCommand;
import games.negative.framework.command.annotation.CommandInfo;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        name = "creategame",
        permission = "towerwars.settings",
        playerOnly = true,
        shortCommands = {"creategame"},
        args = {"playerOne", "playerTwo"}
)
public class SubCreateGame extends SubCommand {
    @SneakyThrows
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player p1 = getPlayer(args[0]);
        Player p2 = getPlayer(args[1]);

        new GameBuilder(
                p1, p2
        ).build();
    }
}
