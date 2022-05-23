package club.icegames.towerwars.game;

import club.icegames.towerwars.TowerWarsPlugin;
import club.icegames.towerwars.core.Locale;
import club.icegames.towerwars.core.utils.Schematic;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import club.icegames.towerwars.core.*;

import java.io.File;
import java.util.*;

@Getter
public class Game {

    private final ArrayList<Player> players;
    private final Team teamOne;
    private final Team teamTwo;
    @Setter
    private World world;
    private final UUID uuid;
    @Setter
    private GameState state;

    public Game(ArrayList<Player> players, Team teamOne, Team teamTwo) {
        this.players = players;
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        uuid = UUID.randomUUID();
        state = GameState.ENDED;
    }

    public void start() {
        Locale.GENERATING_WORLD.send(players);
        setState(GameState.GENERATING_WORLD);
        createWorld();
        Locale.WORLD_GENERATED.send(players);
        setState(GameState.PASTING);
        spawnSchemtatic();
        setState(GameState.STARTING);
        Locale.SCHEMATIC_PASTED.send(players);
        Locale.INTRO.send(players);

        // TODO: Countdown
    }

    private void spawnSchemtatic() {
        File folder = new File(TowerWarsPlugin.getInstance().getDataFolder() + "/schematics");
        ArrayList<File> schematics = new ArrayList<>();

        Arrays.stream(folder.listFiles()).forEach(file -> {
            if (isSchematic(file)) schematics.add(file);
        });

        File schem = (File) getRandomFromArray(schematics);
        Location loc = new Location(world, 0, 0, 0);

        Schematic.paste(removeExtension(schem), loc);
    }

    private void teleport() {
        Location loc = new Location(getWorld(), 0, 0, 0);
        int
                radius = 100,
                minX = loc.getBlockX() - radius,
                minY = loc.getBlockY() - radius,
                minZ = loc.getBlockZ() - radius,
                maxX = loc.getBlockX() + radius,
                maxY = loc.getBlockY() + radius,
                maxZ = loc.getBlockZ() + radius;

        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++)
                    if (world.getBlockAt(x, y, z).getType() == Material.RED_WOOL) {
                        // teleport player1
                    }
    }

    private void createWorld() {
        WorldCreator wc = new WorldCreator(getUuid().toString());
        wc.type(WorldType.FLAT);
        wc.generatorSettings("2;0;1;");
        setWorld(wc.createWorld());
    }

    public void end() {
        // TODO: Enter world deletion stuff
        // TODO: Enter MySQL stuff (web tracker)
        // TODO: Teleport players to spawn
    }

    private Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    private boolean isSchematic(File file) {
        return (getFileExtension(file.getName()).get().equalsIgnoreCase("schem") || getFileExtension(file.getName()).get().equalsIgnoreCase("schematic"));
    }

    private Object getRandomFromArray(ArrayList<?> array) {
        int rnd = new Random().nextInt(array.size());
        return array.get(rnd);
    }

    private String removeExtension(File file) {
        return file.getName().replaceAll(".schem", "").replaceAll(".schematic", "");
    }

}
