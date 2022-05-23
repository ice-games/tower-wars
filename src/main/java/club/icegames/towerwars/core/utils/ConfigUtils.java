package club.icegames.towerwars.core.utils;

import club.icegames.towerwars.TowerWarsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigUtils {

    private final String name;

    public ConfigUtils(String fileName) {
        this.name = fileName;
    }

    public FileConfiguration getConfig() {
        File file = new File(TowerWarsPlugin.getInstance().getDataFolder(), this.name + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }
}
