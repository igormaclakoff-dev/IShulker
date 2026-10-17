package com.auracode.ishulker.command;

import com.auracode.ishulker.IShulkerPlugin;
import com.auracode.ishulker.manager.ShulkerService;
import com.auracode.ishulker.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Locale;

/**
 * Handles {@code /ishulker <on|off|toggle|reload|status|help|version>}.
 */
public final class IShulkerCommand implements CommandExecutor {

    private static final String PERMISSION = "ishulker.admin";
    private static final String PRIMARY = "<#8A5CF6>";
    private static final String MUTED = "<#9E9E9E>";
    private static final String TEXT = "<#E0E0E0>";
    private static final String USAGE = "<red>◆</red> <gray>Использование: "
            + "<white>/ishulker <on|off|toggle|reload|status|help|version></white></gray>";

    private final IShulkerPlugin plugin;

    public IShulkerCommand(IShulkerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            plugin.getMessages().send(sender, "no-permission",
                    "<red>◆</red> <gray>У вас нет доступа.</gray>");
            return true;
        }
        if (args.length == 0) {
            sendStatus(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "on" -> setState(sender, true);
            case "off" -> setState(sender, false);
            case "toggle" -> setState(sender, !plugin.getShulkerService().isEnabled());
            case "status" -> sendStatus(sender);
            case "reload" -> reload(sender);
            case "help" -> sendHelp(sender);
            case "version" -> sendVersion(sender);
            default -> plugin.getMessages().send(sender, "usage", USAGE);
        }
        return true;
    }

    private void setState(CommandSender sender, boolean value) {
        ShulkerService service = plugin.getShulkerService();
        service.setEnabled(value);
        plugin.getMessages().send(sender,
                value ? "enabled" : "disabled",
                value ? "<green>◆</green> <gray>IShulker <white>включён</white>.</gray>"
                        : "<red>◆</red> <gray>IShulker <white>выключен</white>.</gray>");
    }

    private void sendStatus(CommandSender sender) {
        boolean enabled = plugin.getShulkerService().isEnabled();
        plugin.getMessages().send(sender,
                enabled ? "status-enabled" : "status-disabled",
                enabled ? "<green>◆</green> <gray>IShulker сейчас <white>включён</white>.</gray>"
                        : "<red>◆</red> <gray>IShulker сейчас <white>выключен</white>.</gray>");
    }

    private void reload(CommandSender sender) {
        plugin.reloadAll();
        plugin.getMessages().send(sender, "reloaded",
                "<#8A5CF6>◆</#8A5CF6> <gray>Конфигурация IShulker перезагружена.</gray>");
    }

    private void sendVersion(CommandSender sender) {
        sender.sendMessage(Text.parse(PRIMARY + "<bold>IShulker</bold></#8A5CF6> "
                + "<dark_gray>│</dark_gray> " + MUTED + "от AuraCode</#9E9E9E>"));
        sender.sendMessage(Text.parse(MUTED + "Версия: " + TEXT
                + plugin.getPluginMeta().getVersion() + "</#E0E0E0>"));
    }

    private void sendHelp(CommandSender sender) {
        String version = plugin.getPluginMeta().getVersion();
        sender.sendMessage(Text.parse("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));
        sender.sendMessage(Text.parse("<gradient:#8A5CF6:#FF7000><bold>IShulker</bold></gradient> "
                + "<dark_gray>│</dark_gray> <gray>список команд</gray> <dark_gray>v" + version + "</dark_gray>"));
        sender.sendMessage(Text.parse("<gray>Зажмите <white>Shift</white> и нажмите "
                + "<white>ПКМ по воздуху</white>, держа шалкер в руке.</gray>"));
        helpLine(sender, "/ishulker status", "Показать текущее состояние", "/ishulker status");
        helpLine(sender, "/ishulker on|off", "Включить или выключить функцию", "/ishulker ");
        helpLine(sender, "/ishulker toggle", "Переключить функцию", "/ishulker toggle");
        helpLine(sender, "/ishulker reload", "Перезагрузить конфиг плагина", "/ishulker reload");
        helpLine(sender, "/ishulker version", "Показать версию плагина", "/ishulker version");
        helpLine(sender, "/ishulker help", "Показать этот список команд", "/ishulker help");
        sender.sendMessage(Text.parse("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));
    }

    private void helpLine(CommandSender sender, String usage, String description, String suggest) {
        String safeUsage = usage.replace("<", "\\<").replace(">", "\\>");
        Component hover = Text.parse("<gray>" + description + "</gray>")
                .append(Component.newline())
                .append(Text.parse("<dark_gray>нажмите, чтобы вставить команду</dark_gray>"));
        Component line = Text.parse("<dark_gray>▏ </dark_gray>" + PRIMARY + safeUsage + "</#8A5CF6> <dark_gray>—</dark_gray> <gray>" + description + "</gray>")
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.suggestCommand(suggest));
        sender.sendMessage(line);
    }
}
