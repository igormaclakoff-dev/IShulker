package com.auracode.ishulker.config;

import com.auracode.ishulker.IShulkerPlugin;
import com.auracode.ishulker.util.Text;
import org.bukkit.command.CommandSender;

/**
 * Sends configured MiniMessage messages to command senders.
 */
public final class Messages {

    private final IShulkerPlugin plugin;

    public Messages(IShulkerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a configured message to the recipient.
     *
     * @param recipient the receiver
     * @param path      message key without the {@code messages.} prefix
     * @param fallback  fallback text when the key is missing
     */
    public void send(CommandSender recipient, String path, String fallback) {
        recipient.sendMessage(Text.parse(plugin.getSettings().message(path, fallback)));
    }

    /**
     * Sends a configured message with simple token replacements.
     *
     * @param recipient the receiver
     * @param path      message key without the {@code messages.} prefix
     * @param fallback  fallback text when the key is missing
     * @param tokens    alternating search/replace pairs
     */
    public void send(CommandSender recipient, String path, String fallback, String... tokens) {
        String raw = plugin.getSettings().message(path, fallback);
        for (int index = 0; index + 1 < tokens.length; index += 2) {
            raw = raw.replace(tokens[index], tokens[index + 1]);
        }
        recipient.sendMessage(Text.parse(raw));
    }
}
