package club.icegames.towerwars.commands.main.sub;

import club.icegames.towerwars.gui.settings.SettingsMainGUI;
import games.negative.framework.command.SubCommand;
import games.negative.framework.command.annotation.CommandInfo;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        name = "settings",
        permission = "towerwars.settings",
        playerOnly = true,
        shortCommands = {"twsettings", "settings"}
)
public class SubSettings extends SubCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        new SettingsMainGUI("&b&lTower Wars", 3).open((Player) sender);
    }
}
