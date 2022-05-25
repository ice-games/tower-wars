package club.icegames.towerwars.gui.settings;

import club.icegames.towerwars.TowerWarsPlugin;
import club.icegames.towerwars.core.Locale;
import games.negative.framework.gui.GUI;
import games.negative.framework.inputlistener.InputListener;
import games.negative.framework.skull.CustomSkull;
import games.negative.framework.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * @author Seailz
 */
public class SettingsGUI extends GUI {
    public SettingsGUI(@NotNull String title, int rows) {
        super(title, rows, false);

        ItemStack lives = CustomSkull.of("https://textures.minecraft.net/texture/76fdd4b13d54f6c91dd5fa765ec93dd9458b19f8aa34eeb5c80f455b119f278").getItemStack();
        ItemMeta meta = lives.getItemMeta();
        meta.setDisplayName(Utils.color("&c&lLives"));
        meta.setLore(
                Locale.listFromLines(
                        Utils.color(
                                "&7Current Value: &c&l" + TowerWarsPlugin.getInstance().getLives()
                        ),
                        Utils.color(
                                "&8Click to edit"
                        )
                )
        );
        lives.setItemMeta(meta);

        // TODO: change the texture
        ItemStack timeToClaim = CustomSkull.of("https://textures.minecraft.net/texture/76fdd4b13d54f6c91dd5fa765ec93dd9458b19f8aa34eeb5c80f455b119f278").getItemStack();
        ItemMeta timeToClaimMeta = timeToClaim.getItemMeta();
        timeToClaimMeta.setDisplayName(Utils.color("&a&lTower Capture Time"));
        timeToClaimMeta.setLore(
                Locale.listFromLines(
                        Utils.color(
                                "&7Current Value: &a&l" + TowerWarsPlugin.getInstance().getTowerCaptureTime() + "s"
                        ),
                        Utils.color(
                                "&8Click to edit"
                        )
                )
        );
        timeToClaim.setItemMeta(timeToClaimMeta);

        this.setItemClickEvent(12, player -> timeToClaim, (player, event) -> {
            player.closeInventory();
            player.sendTitle(Utils.color("&b&lEnter an amount of seconds"), Utils.color(
                    "&7Please enter &bexit&7 if you would like to exit"
            ));
            InputListener.listen(player.getUniqueId(), response -> {
                if (response.getMessage().equals("exit")) {
                    response.setCancelled(true);
                    return;
                }
                if (!isInteger(response.getMessage())) {
                    Locale.ENTER_VALID_NUMBER.send(response.getPlayer());
                    response.setCancelled(true);
                    return;
                }

                int newTimeToClaim = Integer.parseInt(response.getMessage());
                TowerWarsPlugin.getInstance().getConfig().set("game.tower-claim-time", newTimeToClaim);

                Locale.SET_TIME.send(response.getPlayer());
                TowerWarsPlugin.getInstance().refresh();

                response.setCancelled(true);
            });
        });

        this.setItemClickEvent(14, player -> lives, (player, event) -> {
            player.closeInventory();
            player.sendTitle(Utils.color("&b&lEnter a number"), Utils.color(
                    "&7Please enter &bexit&7 if you would like to exit"
            ));
            InputListener.listen(player.getUniqueId(), response -> {
                if (response.getMessage().equals("exit")) {
                    response.setCancelled(true);
                    return;
                }
                if (!isInteger(response.getMessage())) {
                    Locale.ENTER_VALID_NUMBER.send(response.getPlayer());
                    response.setCancelled(true);
                    return;
                }

                int newLives = Integer.parseInt(response.getMessage());
                TowerWarsPlugin.getInstance().getConfig().set("game.lives", newLives);

                Locale.SET_LIVES.send(response.getPlayer());
                TowerWarsPlugin.getInstance().refresh();

                response.setCancelled(true);
            });
        });
    }

    /**
     * @author corsiKa
     * @param s The string you would like to check
     * @return whether the string is an integer
     */
    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }
    private static void openInv(Player p) {
        new SettingsGUI("&7&lSettings", 3).open(p);
    }
}
