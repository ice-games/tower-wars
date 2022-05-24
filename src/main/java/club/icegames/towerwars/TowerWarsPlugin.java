package club.icegames.towerwars;

import club.icegames.towerwars.core.Locale;
import club.icegames.towerwars.core.Logger;
import club.icegames.towerwars.game.Game;
import club.icegames.towerwars.game.queue.QueueWatcher;
import club.icegames.towerwars.listeners.MoveListener;
import games.negative.framework.BasePlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Seailz
 */
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

    /**
     * Registers things
     * @param type Which thing you want to register
     */
    public void register(RegisterType type) {
        switch (type) {
            case COMMAND:
                registerCommands(
                        // Insert commands
                );
                break;
            case LISTENER:
                registerListeners(
                        new MoveListener()
                );
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Checks if a player is in a game
     * @param p The player you wish to check
     * @return A boolean defining whether the player is in a game.
     */
    public boolean inGame(Player p) {
        return inGame.containsKey(p);
    }
    private enum RegisterType {COMMAND, LISTENER}
}
