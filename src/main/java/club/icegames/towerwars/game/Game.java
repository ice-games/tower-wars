package club.icegames.towerwars.game;

import club.icegames.towerwars.TowerWarsPlugin;
import club.icegames.towerwars.core.Locale;
import club.icegames.towerwars.core.exeptions.NoOneLostException;
import club.icegames.towerwars.core.utils.Schematic;
import club.icegames.towerwars.core.utils.database.DB;
import club.icegames.towerwars.core.utils.database.Row;
import club.icegames.towerwars.core.utils.database.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import club.icegames.towerwars.core.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public Game(@NotNull ArrayList<Player> players, @NotNull Team teamOne, @NotNull Team teamTwo) {
        this.players = players;
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        uuid = UUID.randomUUID();
        state = GameState.ENDED;
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
                    switch (world.getBlockAt(x, y, z).getType()) {
                        case LAPIS_BLOCK:
                            getTeamOne().getPlayer().teleport(new Location(world, x, y + 1, z));
                            break;
                        case REDSTONE_BLOCK:
                            getTeamTwo().getPlayer().teleport(new Location(world, x, y + 1, z));
                            break;
                    }
    }

    // World Utils
    private void createWorld() {
        WorldCreator wc = new WorldCreator(getUuid().toString());
        wc.type(WorldType.FLAT);
        wc.generatorSettings("2;0;1;");
        setWorld(wc.createWorld());
    }
    private void deleteWorld() throws IOException {
        FileUtils.deleteDirectory(new File(getWorld().getName()));
    }

    // State changers
    public void start() {
        Locale.GENERATING_WORLD.send(players);
        setState(GameState.GENERATING_WORLD);
        createWorld();
        Locale.WORLD_GENERATED.send(players);
        setState(GameState.PASTING);
        spawnSchemtatic();
        setState(GameState.STARTING);
        Locale.SCHEMATIC_PASTED.send(players);
        teleport();
        Locale.INTRO.send(players);

        // TODO: Countdown
    }
    public void end(Player winner) throws NoOneLostException {
        setState(GameState.ENDED);
        try {
            deleteWorld();
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "Couldn't delete world " + getUuid());
            Logger.log(Logger.LogLevel.ERROR, "Couldn't delete world " + getUuid());
            Logger.log(Logger.LogLevel.ERROR, "Couldn't delete world " + getUuid());
        }

        // MYSQL stuff
        if (TowerWarsPlugin.getInstance().getConfig().getBoolean("mysql.enabled")) {
            DB db = new DB(
                        TowerWarsPlugin.getInstance().getConfig().getString("mysql.ip")
                        + TowerWarsPlugin.getInstance().getConfig().getString("mysql.port"),
                        TowerWarsPlugin.getInstance().getConfig().getString("mysql.username"),
                        TowerWarsPlugin.getInstance().getConfig().getString("mysql.password"),
                        TowerWarsPlugin.getInstance().getConfig().getString("mysql.name")
                    );

            AtomicBoolean tableExists = new AtomicBoolean(false);
            db.getTables().forEach(table -> {
                if (table.equals("WEB_TRACK")) tableExists.set(true);
            });

            if (!tableExists.get()) {
                db.addTable(new Table("WEB_TRACK")
                        .idColumn()
                        .column("id", String.class)
                        .column("winner", String.class)
                        .column("lost", String.class));
            }

            Table table = new Table("WEB_TRACK");
            Player lost = null;

            if (getTeamTwo().getPlayer().equals(winner)) lost = getTeamOne().getPlayer();
            if (getTeamOne().getPlayer().equals(winner)) lost = getTeamTwo().getPlayer();

            if (lost == null) throw new NoOneLostException("This is weird, no one lost!");

            db.insert("WEB_TRACK",
                    new Row()
                            .with("id", getUuid().toString())
                            .with("winner", winner.getName())
                            .with("lost", lost.getName())
                        );
        }
        // TODO: Teleport players to spawn
    }

    // Schematic Utils
    private void spawnSchemtatic() {
        File folder = new File(TowerWarsPlugin.getInstance().getDataFolder() + "/schematics");
        ArrayList<File> schematics = new ArrayList<>();

        Arrays.stream(folder.listFiles()).forEach(file -> {
            if (isSchematic(file)) schematics.add(file);
        });

        File schematic = (File) getRandomFromArray(schematics);
        Location loc = new Location(world, 0, 0, 0);

        Schematic.paste(removeExtension(schematic), loc);
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
