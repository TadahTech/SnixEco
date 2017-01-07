package com.tadahtech.pub.snixeco.command.sub;

import com.google.common.collect.Maps;
import com.tadahtech.pub.snixeco.command.SubCommand;
import com.tadahtech.pub.snixeco.config.Lang;
import com.tadahtech.pub.snixeco.player.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class LookupCommand implements SubCommand
{

    @Override
    public void execute(CommandSender player, String[] args)
    {
        if (args.length != 1)
        {
            sendUsage(player);
            return;
        }

        String targetRaw = args[0];

        Player target = Bukkit.getPlayer(targetRaw);

        if (target != null)
        {
            PlayerInfo info = PlayerInfo.getInfo(target);

            if (info == null)
            {
                //Shouldn't happen short of a reload
                getPlugin().getSqlManager().getPlayerAsync(target.getUniqueId(), target.getName(), info1 ->
                {
                    int snix = info1.getSnix();

                    Map<String, String> map = Maps.newHashMap();
                    map.put(Lang.AMOUNT, String.valueOf(snix));
                    map.put(Lang.PLAYER, target.getName());

                    Lang.LOOKUP_SUCCESS.sendMessage(player, map);
                });
                return;
            }

            int snix = info.getSnix();

            Map<String, String> map = Maps.newHashMap();
            map.put(Lang.AMOUNT, String.valueOf(snix));
            map.put(Lang.PLAYER, target.getName());

            Lang.LOOKUP_SUCCESS.sendMessage(player, map);
            return;
        }

        getPlugin().getSqlManager().getSnix(targetRaw, integer ->
        {
            Map<String, String> map = Maps.newHashMap();
            map.put(Lang.AMOUNT, String.valueOf(integer));
            map.put(Lang.PLAYER, target.getName());

            Lang.LOOKUP_SUCCESS.sendMessage(player, map);
        });

    }

    @Override
    public String getName()
    {
        return "lookup";
    }

    @Override
    public String getPermission()
    {
        return "snix.admin";
    }

    @Override
    public String getDescription()
    {
        return "Lookup a player's snix points.";
    }

    @Override
    public String getUsage()
    {
        return "/snix lookup <player>";
    }
}
