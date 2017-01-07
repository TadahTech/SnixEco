package com.tadahtech.pub.snixeco.listener;

import com.tadahtech.pub.snixeco.Snix;
import com.tadahtech.pub.snixeco.player.PlayerInfo;
import com.tadahtech.pub.snixeco.sql.SQLManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 */
public class PlayerListener implements Listener
{

	private SQLManager sqlManager;

	public PlayerListener(SQLManager sqlManager)
	{
		this.sqlManager = sqlManager;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		PlayerInfo info = PlayerInfo.getInfo(player);

		if(info == null)
		{
			sqlManager.getPlayerAsync(player.getUniqueId(), player.getName(), info1 -> {});
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		PlayerInfo info = PlayerInfo.getInfo(player);

		if(info == null)
		{
			Snix.getInstance().debug(player.getName() + " had no info!");
			//Shouldn't happen
			return;
		}

		sqlManager.savePlayer(info);
	}

}
