package club.icegames.towerwars;

import club.icegames.towerwars.core.Locale;
import club.icegames.towerwars.core.Logger;
import club.icegames.towerwars.game.Game;
import club.icegames.towerwars.game.queue.QueueWatcher;
import games.negative.framework.BasePlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public final class TowerWarsPlugin extends BasePlugin {

    @Getter
    @Setter
    public static TowerWarsPlugin instance;
    @Getter
    private final HashMap<Player, Game> inGame = new HashMap<>();
    @Getter
    private final ArrayList<Player> queue = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        super.onEnable();
        long start = System.currentTimeMillis();

        setInstance(this);

        // Set details and register things
        register(RegisterType.COMMAND);
        register(RegisterType.LISTENER);

        // Queue Watcher
        new QueueWatcher(10);

        // Create schematic folder
        if (!new File(getDataFolder() + "/schematics").exists()) new File(getDataFolder() + "/schematics").mkdir();

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