package com.tadahtech.pub.snixeco.command.sub;

import com.google.common.collect.Maps;
import com.tadahtech.pub.snixeco.command.SubCommand;
import com.tadahtech.pub.snixeco.config.Lang;
import com.tadahtech.pub.snixeco.player.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

public class PayCommand implements SubCommand
{

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!(sender instanceof Player))
        {
            return;
        }

        if (args.length != 2)
        {
            sendUsage(sender);
            return;
        }

        String targetRaw = args[0];

        Player target = Bukkit.getPlayer(targetRaw);
        Player player = (Player) sender;

        if (target == null)
        {
            Lang.FAIL_PLAYER_NOT_ONLINE.sendMessage(player, Collections.singletonMap(Lang.PLAYER, targetRaw));
            return;
        }

        PlayerInfo info = PlayerInfo.getInfo(player);
        PlayerInfo targetInfo = PlayerInfo.getInfo(target);
        int amountRaw;

        try
        {
            amountRaw = Integer.parseInt(args[1]);
        } catch (NumberFormatException e)
        {
            Lang.NOT_VALID_NUMBER.sendMessage(player, Collections.singletonMap(Lang.VARIABLE, args[1]));
            return;
        }

        int amount = amountRaw;

        Map<String, String> executorMap = Maps.newHashMap();
        executorMap.put(Lang.AMOUNT, args[1]);
        executorMap.put(Lang.PLAYER, target.getName());

        if (info == null)
        {
            getPlugin().getSqlManager().getPlayerAsync(player.getUniqueId(), player.getName(), info1 ->
            {
                int snix = info1.getSnix();

                if (amount > snix)
                {
                    Map<String, String> map = Maps.newHashMap();
                    map.put(Lang.AMOUNT, String.valueOf(snix));
                    map.put(Lang.VARIABLE, String.valueOf(amount - snix));

                    Lang.PAY_NOT_ENOUGH_SNIX.sendMessage(player, map);
                    return;
                }

                info1.setSnix(snix - amount);
                executorMap.put(Lang.VARIABLE, String.valueOf(snix));
            });
        }
        else
        {
            int snix = info.getSnix();

            if (amount > snix)
            {
                Map<String, String> map = Maps.newHashMap();
                map.put(Lang.AMOUNT, String.valueOf(snix));
                map.put(Lang.VARIABLE, String.valueOf(amount - snix));

                Lang.PAY_NOT_ENOUGH_SNIX.sendMessage(player, map);
                return;
            }

            executorMap.put(Lang.VARIABLE, String.valueOf(snix));

            info.setSnix(snix - amount);
        }

        Map<String, String> targetMap = Maps.newHashMap();
        targetMap.put(Lang.AMOUNT, args[1]);
        targetMap.put(Lang.PLAYER, player.getName());


        if (targetInfo == null)
        {
            getPlugin().getSqlManager().getPlayerAsync(target.getUniqueId(), target.getName(), tInfo ->
            {
                tInfo.setSnix(tInfo.getSnix() + amount);
                targetMap.put(Lang.VARIABLE, String.valueOf(tInfo.getSnix()));
            });
        }
        else
        {
            targetInfo.setSnix(targetInfo.getSnix() + amount);
            targetMap.put(Lang.VARIABLE, String.valueOf(targetInfo.getSnix()));
        }

        Lang.PAY_SUCCESS_EXECUTOR.sendMessage(player, executorMap);
        Lang.PAY_SUCCESS_TARGET.sendMessage(target, targetMap);
    }

    @Override
    public String getName()
    {
        return "pay";
    }

    @Override
    public String getPermission()
    {
        return "snix.pay";
    }

    @Override
    public String getDescription()
    {
        return "Pay a player out of your own pocket";
    }

    @Override
    public String getUsage()
    {
        return "/snix pay <player> <amount>";
    }
}
