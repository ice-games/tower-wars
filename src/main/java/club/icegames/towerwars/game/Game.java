package club.icegames.towerwars.game;

import club.icegames.towerwars.TowerWarsPlugin;
import club.icegames.towerwars.core.Locale;
import club.icegames.towerwars.core.exeptions.NoOneLostException;
import club.icegames.towerwars.core.exeptions.PlayerIsAlreadyInGameException;
import club.icegames.towerwars.core.utils.Countdown;
import club.icegames.towerwars.core.utils.Schematic;
import club.icegames.towerwars.core.utils.database.DB;
import club.icegames.towerwars.core.utils.database.Row;
import club.icegames.towerwars.core.utils.database.Table;
import club.icegames.towerwars.core.world.EmptyChunkGenerator;
import games.negative.framework.util.Task;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import club.icegames.towerwars.core.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.channels.NetworkChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Seailz
 */
@Getter
public class Game {

    private final ArrayList<Player> players = new ArrayList<>();
    private final SingleTeam teamOne;
    private final SingleTeam teamTwo;
    @Setter
    private World world;
    private final UUID uuid;
    @Setter
    private GameState state;

    public Game(@NotNull SingleTeam teamOne, @NotNull SingleTeam teamTwo) throws PlayerIsAlreadyInGameException {

        AtomicBoolean moveOn = new AtomicBoolean(true);

        players.add(teamOne.getPlayer());
        players.add(teamTwo.getPlayer());

        if (!moveOn.get())
            throw new PlayerIsAlreadyInGameException("One of the current players is already in a game!");


        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        uuid = UUID.randomUUID();
        state = GameState.ENDED;
    }

    /**
     * Teleports all players to the correct location
     * @author Seailz
     */
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

    /**
     * Creates the world for the game
     * @author Seailz
     * @param use118 Whether we should use the 1.18 world creation
     */
    private void createWorld(boolean use118) {
        if (!use118) {
            WorldCreator wc = new WorldCreator(getUuid().toString());
            wc.type(WorldType.FLAT);
            wc.generatorSettings("2;0;1;");
            setWorld(wc.createWorld());
            return;
        }
        WorldCreator wc = new WorldCreator(getUuid().toString());
        wc.generator(new EmptyChunkGenerator()); //The chunk generator from step 1
        setWorld(wc.createWorld());
    }

    /**
     * Deletes the world after the game is finished
     * @author Seailz
     * @throws IOException if the world folder doesn't exist
     */
    private void deleteWorld() throws IOException {
        FileUtils.deleteDirectory(new File(getWorld().getName()));
    }

    // State changers

    /**
     * Starts the game
     * @author Seailz
     */
    public void start() throws IOException {
        AtomicBoolean moveOn = new AtomicBoolean(true);
        players.forEach(player -> {
            if (TowerWarsPlugin.getInstance().getInGame().containsKey(player)) moveOn.set(false);
        });
        if (!moveOn.get()) return;

        sendMessageToAll(Locale.LOADING);
        setState(GameState.GENERATING_WORLD);
        createWorld(TowerWarsPlugin.getInstance().getConfig().getBoolean("use118"));
        setState(GameState.PASTING);
        spawnSchemtatic();
        setState(GameState.STARTING);
        teleport();
        sendMessageToAll(Locale.LOADED);
        Locale.INTRO.send(players);

        new Countdown(teamOne.getPlayer());
        new Countdown(teamTwo.getPlayer());

        // After Countdown:
        Task.asyncDelayed(TowerWarsPlugin.getInstance(), 100, () -> setState(GameState.RUNNING));
    }

    /**
     * Ends the game
     * @author Seailz
     * @param winner the winner of the game
     * @throws NoOneLostException if no-one lost
     */
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

    // TODO: Claiming Towers
    // TODO: Arrow shooting

    // Schematic Utils

    /**
     * Pastes a random schematic
     * @author Seailz
     */
    private void spawnSchemtatic() throws IOException {
        File folder = new File(TowerWarsPlugin.getInstance().getDataFolder() + "/schematics");
        ArrayList<File> schematics = new ArrayList<>();

        if (folder.listFiles() == null) {
            Logger.log(Logger.LogLevel.ERROR, "No schematics detected!");
            return;
        }

        Arrays.stream(folder.listFiles()).forEach(file -> {
            if (isSchematic(file)) schematics.add(file);
        });

        if (schematics.isEmpty()) {
            sendMessageToAll(Locale.NO_SCHEMS_FOUND);
            deleteWorld();
            return;
        }

        File schematic = (File) getRandomFromArray(schematics);
        Location loc = new Location(world, 0, 0, 0);

        Schematic.paste(removeExtension(schematic), loc);
    }

    /**
     * Gets the file extension
     * @param filename the name of the file
     * @return the file extension
     * @author Seailz
     */
    private Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    /**
     * Checks if a file is a schematic
     * @param file the file you want to check
     * @return if the file is a schematic
     * @author Seailz
     */
    private boolean isSchematic(File file) {
        return (getFileExtension(file.getName()).get().equalsIgnoreCase("schem") || getFileExtension(file.getName()).get().equalsIgnoreCase("schematic"));
    }

    /**
     * Picks a random object from an Array
     * @param array the arraylist
     * @return a random object from inside that array
     * @author Seailz
     */
    private Object getRandomFromArray(ArrayList<?> array) {
        int rnd = new Random().nextInt(array.size());
        return array.get(rnd);
    }

    /**
     * Removes the .schem or .schematic extension from a file
     * @param file the file
     * @return a string without the extension
     * @author Seailz
     */
    private String removeExtension(File file) {
        return file.getName().replaceAll(".schem", "").replaceAll(".schematic", "");
    }

    public void kill(Team team) {
        boolean teamOne = team.equals(Team.ONE);
        Player p = teamOne ? getTeamOne().getPlayer() : getTeamTwo().getPlayer();
        SingleTeam t = teamOne ? getTeamOne() : getTeamTwo();

        t.setLives(t.getLives() - 1);

        Locale.PLAYER_DEATH.replace("%player%", p.getName()).send(this.teamOne.getPlayer());
        Locale.PLAYER_DEATH.replace("%player%", p.getName()).send(this.teamTwo.getPlayer());

        p.setGameMode(GameMode.CREATIVE);
    }

    public enum Team {ONE, TWO;}
    private void sendMessageToAll(Locale locale) {
        players.forEach(locale::send);
    }
}
