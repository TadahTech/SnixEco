package com.tadahtech.pub.snixeco.command.sub;

import com.google.common.collect.Maps;
import com.tadahtech.pub.snixeco.command.SubCommand;
import com.tadahtech.pub.snixeco.config.Lang;
import com.tadahtech.pub.snixeco.player.PlayerInfo;
import com.tadahtech.pub.snixeco.sql.SQLManager.SnixUpdateType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

public class TakeCommand implements SubCommand
{

    @Override
    public void execute(CommandSender player, String[] args)
    {
        if (args.length != 2)
        {
            sendUsage(player);
            return;
        }

        String targetRaw = args[0];
        String amountRaw = args[1];

        int amount;
        try
        {
            amount = Integer.parseInt(amountRaw);
        } catch (NumberFormatException e)
        {
            Lang.NOT_VALID_NUMBER.sendMessage(player, Collections.singletonMap(Lang.VARIABLE, amountRaw));
            return;
        }

        Player target = Bukkit.getPlayer(targetRaw);

        if (target != null)
        {
            PlayerInfo info = PlayerInfo.getInfo(target);
            info.setSnix(info.getSnix() - amount);

            Map<String, String> map = Maps.newHashMap();
            map.put(Lang.PLAYER, target.getName());
            map.put(Lang.AMOUNT, amountRaw);

            Lang.TAKE_SUCCESS.sendMessage(player, map);

            return;
        }

        getPlugin().getSqlManager().updateSnix(targetRaw, amount, SnixUpdateType.SUBTRACT, integer ->
        {
            Map<String, String> map = Maps.newHashMap();
            map.put(Lang.PLAYER, targetRaw);
            map.put(Lang.AMOUNT, amountRaw);

            if (integer == -1)
            {
                map.put(Lang.VARIABLE, "taking");
                Lang.FAIL_GENERIC.sendMessage(player, map);
                return;
            }

            if (integer == -2)
            {
                Lang.FAIL_NOT_EXIST.sendMessage(player, null);
                return;
            }

            Lang.TAKE_SUCCESS.sendMessage(player, map);
        });

    }

    @Override
    public String getName()
    {
        return "take";
    }

    @Override
    public String getPermission()
    {
        return "snix.admin";
    }

    @Override
    public String getDescription()
    {
        return "Take from a player's snix points";
    }

    @Override
    public String getUsage()
    {
        return "/snix take <player> <amount>";
    }
}
