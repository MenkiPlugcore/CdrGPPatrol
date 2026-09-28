package id.menkiplugcore.cdrgppatrol.util;

import org.bukkit.ChatColor;

public final class Colors {
    private Colors() {
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
