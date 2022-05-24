package club.icegames.towerwars.core;

import games.negative.framework.message.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

/**
 * @author joeecodes
 */
@RequiredArgsConstructor
    @Getter
    public enum Locale {

        ERROR_INVALID_PLAYER("error.invalidplayer", Collections.singletonList("&c&lERROR&f That player is invalid!")),
        GENERATING_WORLD("WORLD_GENERATING", listFromLines(
                "&c&l(!) &cGame has been initialized, generating world..."
        )),
        WORLD_GENERATED("WORLD_GENERATED", listFromLines(
                "&c&l(!) &cWorld generated! Pasting schematic..."
        )),
        SCHEMATIC_PASTED("SCHEMATIC_PASTED", listFromLines(
                "&c&l(!) &cSchematic pasted! Teleporting..."
        )),
        TELEPORTED("TELEPORTED", listFromLines(
                "&c&l(!) &cTeleported!"
        )),
        INTRO("INTRO", listFromLines(
                "&c&l                   TOWER WARS",
                "&7Capture all the towers by standing on their &egold block&7 for 7 seconds!",
                "         &7First one to capture all the towers wins!",
                " &7Beware of the big towers, once captured they will start attacking the other",
                "&7             team with arrows! Good luck..."
        )),
        PLAYER_DEATH("PLAYER_DEATH", listFromLines(
                "&c&l(!) &c%player% just died!"
        ));

        private final String id;
        private final List<String> defaultMessage;
        private Message message;

        /**
         * Register the messages.yml file
         *
         * @param plugin The main class
         */
        @SneakyThrows
        public static void init(JavaPlugin plugin) {
            File configFile = new File(plugin.getDataFolder(), "messages.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            if (!configFile.exists()) {
                Arrays.stream(values()).forEach(locale -> {
                    String id = locale.getId();
                    List<String> defaultMessage = locale.getDefaultMessage();

                    config.set(id, defaultMessage);
                });

            } else {
                Arrays.stream(values()).filter(locale -> {
                    String id = locale.getId();
                    return (config.get(id, null) == null);
                }).forEach(locale -> config.set(locale.getId(), locale.getDefaultMessage()));

            }
            config.save(configFile);

            // Creates the message objects
            Arrays.stream(values()).forEach(locale ->
                    locale.message = new Message(config.getStringList(locale.getId())
                            .toArray(new String[0])));
        }

        public void send(CommandSender sender) {
            message.send(sender);
        }

        public void send(List<Player> players) {
            message.send((CommandSender) players);
        }

        public void broadcast() {
            message.broadcast();
        }

        public Message replace(Object o1, Object o2) {
            return message.replace((String) o1, (String) o2);
        }

        public static List<String> listFromLines(String... s) {
            return new ArrayList<>(List.of(s));
        }

    }
