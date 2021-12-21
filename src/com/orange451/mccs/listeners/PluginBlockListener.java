package com.orange451.mccs.listeners;

import com.orange451.mccs.CSArena;
import com.orange451.mccs.CSPlayer;
import com.orange451.mccs.MCCS;
import com.orange451.mccs.CSArena.GameType;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PluginBlockListener
implements Listener
{
	private MCCS plugin;

	public PluginBlockListener(MCCS plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.getPlayer().isOp()) {
			event.setCancelled(true);
			if (event.getBlock().getType().isSolid()) {
				CSPlayer player = plugin.getCSPlayer(event.getPlayer());
				if (player != null) {
					CSArena arena = player.getArena();
					if (!arena.getGameType().equals(GameType.LOBBY)) {
						event.getPlayer().setHealth(0);
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.getPlayer().isOp())
			event.setCancelled(true);
	}
}