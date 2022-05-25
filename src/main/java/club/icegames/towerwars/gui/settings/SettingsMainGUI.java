package club.icegames.towerwars.gui.settings;

import club.icegames.towerwars.core.Locale;
import games.negative.framework.gui.GUI;
import games.negative.framework.skull.CustomSkull;
import games.negative.framework.util.Utils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * @author Seailz
 */
public class SettingsMainGUI extends GUI {
    public SettingsMainGUI(@NotNull String title, int rows) {
        super(title, rows, false);

        ItemStack settings = CustomSkull.of("https://textures.minecraft.net/texture/ec2ff244dfc9dd3a2cef63112e7502dc6367b0d02132950347b2b479a72366dd").getItemStack();
        ItemMeta meta = settings.getItemMeta();
        meta.setDisplayName(Utils.color("&7&lSettings"));
        meta.setLore(
                Locale.listFromLines(
                        Utils.color(
                                "&8Edit the settings of the game"
                        )
                )
        );
        settings.setItemMeta(meta);

        this.setItemClickEvent(13, player -> settings, (player, event) -> {
            new SettingsGUI("&7&lSettings", 3).open(player);
        });


    }
}
