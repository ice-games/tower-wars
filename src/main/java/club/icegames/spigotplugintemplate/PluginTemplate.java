package club.icegames.spigotplugintemplate;

import club.icegames.spigotplugintemplate.core.Locale;
import club.icegames.spigotplugintemplate.core.Logger;
import club.icegames.spigotplugintemplate.core.license.ULicense;
import club.icegames.spigotplugintemplate.core.utils.ConfigUtils;
import games.negative.framework.BasePlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public final class PluginTemplate extends BasePlugin {

    @Getter
    @Setter
    public static PluginTemplate instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        super.onEnable();
        long start = System.currentTimeMillis();

        setInstance(this);

        FileConfiguration license = new ConfigUtils("license").getConfig();

        if (!new ULicense(this, license.getString("license-key"), "https://licenses.seailz.com/api/client", "cac79c8e128cd775297fb454516b6c2c2b0cfdd9").verify()) {
            Bukkit.getPluginManager().disablePlugin(this);
            Bukkit.getScheduler().cancelTasks(this);
            return;
        }

        // Set details and register things
        register(RegisterType.COMMAND);
        register(RegisterType.LISTENER);

        Locale.init(this);
        saveDefaultConfig();

        long finish = System.currentTimeMillis() - start;
        Logger.log(Logger.LogLevel.SUCCESS, "Started in " + finish + "ms!");
    }

    public void register(RegisterType type) {
        switch (type) {
            case COMMAND:
                registerCommands(
                        // Insert commands
                );
                break;
            case LISTENER:
                registerListeners(
                        // Register Listeners
                );
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public enum RegisterType {COMMAND, LISTENER}
}
