package com.tadahtech.pub.snixeco.command;

import com.tadahtech.pub.snixeco.Snix;
import com.tadahtech.pub.snixeco.config.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 *
 */
public interface SubCommand
{

    void execute(CommandSender player, String[] args);

    String getName();

    String getPermission();

    String getDescription();

    String getUsage();

    default void sendUsage(CommandSender player)
    {
        player.sendMessage(Lang.PREFIX + " " + ChatColor.RED + "Incorrect usage. Proper usage - " + ChatColor.GRAY + getUsage());
    }

    default boolean canUse(CommandSender player)
    {
        return getPermission() != null && player.hasPermission(getPermission()) || (player.isOp() || player instanceof ConsoleCommandSender);
    }

    default Snix getPlugin()
    {
        return Snix.getInstance();
    }

}
