package com.tadahtech.pub.snixeco.command.sub;

import com.tadahtech.pub.snixeco.command.SubCommand;
import com.tadahtech.pub.snixeco.config.Lang;
import com.tadahtech.pub.snixeco.player.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ResetPlayer implements SubCommand
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

        if (target == null)
        {
            Lang.FAIL_PLAYER_NOT_ONLINE.sendMessage(player, Collections.singletonMap(Lang.PLAYER, targetRaw));
            return;
        }

        PlayerInfo info = PlayerInfo.getInfo(target);

        if (info == null)
        {
            getPlugin().getSqlManager().getPlayerAsync(target.getUniqueId(), target.getName(), tInfo ->
            {
                tInfo.setSnix(0);
            });
        }
        else
        {
            info.setSnix(0);
        }

        Lang.RESET_SUCCESS_EXECUTOR.sendMessage(player, Collections.singletonMap(Lang.PLAYER, target.getName()));
        Lang.RESET_SUCCESS_TARGET.sendMessage(target, null);

    }

    @Override
    public String getName()
    {
        return "reset";
    }

    @Override
    public String getPermission()
    {
        return "snix.admin";
    }

    @Override
    public String getDescription()
    {
        return "Reset a player's snix points to 0.";
    }

    @Override
    public String getUsage()
    {
        return "/snix reset <player>";
    }

}
