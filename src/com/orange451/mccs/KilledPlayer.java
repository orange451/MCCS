package com.orange451.mccs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class KilledPlayer {
	public boolean execute(Player killed, Entity damager) {
		Player killer = null;
		if ((damager instanceof Player))
			killer = (Player)damager;
		if (((damager instanceof Projectile)) && 
				((((Projectile)damager).getShooter() instanceof Player))) {
			killer = (Player)((Projectile)damager).getShooter();
		}

		if (killer != null) {
			execute_kill(killer, killed);
		}
		CSPlayer cskilled = MCCS.getCSPlugin().getCSPlayer(killed);
		if (cskilled != null) {
			cskilled.getArena().onDeath(cskilled);
			return true;
		}
		return false;
	}

	private void execute_kill(Player killer, Player killed)
	{
		CSPlayer cskiller = MCCS.getCSPlugin().getCSPlayer(killer);
		CSPlayer cskilled = MCCS.getCSPlugin().getCSPlayer(killed);
		if ((cskiller != null) && (cskilled != null)) {
			cskiller.getArena().broadcastMessage(
					cskiller.getTeamColor() + cskiller.getPlayer().getName() + 
					ChatColor.GRAY + " killed " + 
					cskilled.getTeamColor() + cskilled.getPlayer().getName());

			cskiller.getArena().onKill(cskiller, cskilled);
		}
	}
}