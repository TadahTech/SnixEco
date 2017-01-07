package com.tadahtech.pub.snixeco.command;

import com.google.common.collect.Maps;
import com.tadahtech.pub.snixeco.Snix;
import com.tadahtech.pub.snixeco.command.sub.GiveCommand;
import com.tadahtech.pub.snixeco.command.sub.LookupCommand;
import com.tadahtech.pub.snixeco.command.sub.PayCommand;
import com.tadahtech.pub.snixeco.command.sub.ResetPlayer;
import com.tadahtech.pub.snixeco.command.sub.TakeCommand;
import com.tadahtech.pub.snixeco.config.Lang;
import com.tadahtech.pub.snixeco.player.PlayerInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

public class CommandHandler implements CommandExecutor
{

    private final Map<String, SubCommand> COMMANDS = Maps.newHashMap();

    public CommandHandler()
    {
        registerCommand(new GiveCommand());
        registerCommand(new LookupCommand());
        registerCommand(new PayCommand());
        registerCommand(new ResetPlayer());
        registerCommand(new TakeCommand());
    }

    public void registerCommand(SubCommand command)
    {
        Snix.getInstance().debug("Registered command " + command.getName() + " - " + command.getPermission() + " - " + command.getUsage());
        COMMANDS.put(command.getName().toLowerCase(), command);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] baseArgs)
    {
        if (baseArgs.length == 0)
        {
            if (!(commandSender instanceof Player))
            {
                commandSender.sendMessage("You must be a player!");
                return true;
            }

            Player player = (Player) commandSender;

            PlayerInfo info = PlayerInfo.getInfo(player);

            if (info == null)
            {
                Snix.getInstance().getSqlManager().getPlayerAsync(player.getUniqueId(), player.getName(), info1 ->
                {
                    Lang.CURRENT_AMOUNT.sendMessage(player, Collections.singletonMap(Lang.AMOUNT, String.valueOf(info1.getSnix())));
                });
                return true;
            }

            Lang.CURRENT_AMOUNT.sendMessage(player, Collections.singletonMap(Lang.AMOUNT, String.valueOf(info.getSnix())));
            return true;
        }

        String[] args = new String[baseArgs.length - 1];
        System.arraycopy(baseArgs, 1, args, 0, args.length);

        String command = baseArgs[0].toLowerCase();

        SubCommand subCommand = COMMANDS.get(command.toLowerCase());

        if (subCommand == null || command.equalsIgnoreCase("help"))
        {
            Lang.NOT_A_COMMAND.sendMessage(commandSender, Collections.singletonMap(Lang.VARIABLE, command));
            sendHelp(commandSender);
            return true;
        }

        if (!subCommand.canUse(commandSender))
        {
            Lang.NO_PERMISSION.sendMessage(commandSender, null);
            return true;
        }

        subCommand.execute(commandSender, args);
        return false;
    }

    private void sendHelp(CommandSender commandSender)
    {

    }

}
