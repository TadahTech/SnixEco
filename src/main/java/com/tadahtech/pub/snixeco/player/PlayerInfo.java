package com.tadahtech.pub.snixeco.player;

import com.google.common.collect.Maps;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class PlayerInfo
{

	private static final Map<UUID, PlayerInfo> INFO = Maps.newHashMap();

	private UUID uuid;
	private String lastKnownName;
	private int snix;

	public PlayerInfo(UUID uuid, String lastKnownName, int snix)
	{
		this.uuid = uuid;
		this.lastKnownName = lastKnownName;
		this.snix = snix;
		INFO.put(uuid, this);
	}

	public static PlayerInfo getInfo(UUID uuid)
	{
		return INFO.get(uuid);
	}

	public static PlayerInfo getInfo(Player player)
	{
		return getInfo(player.getUniqueId());
	}

	public UUID getUuid()
	{
		return uuid;
	}

	public String getLastKnownName()
	{
		return lastKnownName;
	}

	public void setLastKnownName(String lastKnownName)
	{
		this.lastKnownName = lastKnownName;
	}

	public int getSnix()
	{
		return snix;
	}

	public void setSnix(int snix)
	{
		this.snix = snix;
	}

	public void scrub()
	{
		INFO.remove(this.uuid);
	}
}
