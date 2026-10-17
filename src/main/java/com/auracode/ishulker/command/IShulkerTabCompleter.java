package com.auracode.ishulker.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Handles tab completion for {@code /ishulker}.
 */
public final class IShulkerTabCompleter implements TabCompleter {

    private static final String PERMISSION = "ishulker.admin";
    private static final List<String> SUBCOMMANDS =
            List.of("on", "off", "toggle", "reload", "status", "help", "version");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION) || args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String subcommand : SUBCOMMANDS) {
            if (subcommand.startsWith(prefix)) {
                matches.add(subcommand);
            }
        }
        return matches;
    }
}
