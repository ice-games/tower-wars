package club.icegames.towerwars.commands.main;

import club.icegames.towerwars.commands.main.sub.SubSettings;
import club.icegames.towerwars.core.Locale;
import games.negative.framework.command.Command;
import games.negative.framework.command.annotation.CommandInfo;
import org.bukkit.command.CommandSender;

@CommandInfo(
        name = "towerwars",
        aliases = {"tw"}
)
public class CommandTowerWars extends Command {

    public CommandTowerWars() {
        this.addSubCommands(
                 new SubSettings()
        );
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Locale.INTRO.send(sender);
    }
}
