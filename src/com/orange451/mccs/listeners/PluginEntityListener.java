package com.orange451.mccs.listeners;

import com.orange451.mccs.CSArena;
import com.orange451.mccs.CSArena.Teams;
import com.orange451.mccs.CSHostage;
import com.orange451.mccs.CSPlayer;
import com.orange451.mccs.MCCS;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PluginEntityListener implements Listener {
	private MCCS plugin;

	public PluginEntityListener(MCCS plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity edamager = event.getDamager();
		Entity eattacked = event.getEntity();
		if ((eattacked instanceof Player)) {
			Player attacked = (Player)eattacked;
			Player damager = null;
			if ((edamager instanceof Player)) {
				damager = (Player)edamager;
			} else if ((edamager instanceof Projectile)) {
				LivingEntity shooter = ((Projectile)edamager).getShooter();
				if ((shooter instanceof Player)) {
					damager = (Player)shooter;
				}
			}

			if ((attacked != null) && (damager != null)) {
				CSPlayer player = this.plugin.getCSPlayer(attacked);
				CSPlayer player2 = this.plugin.getCSPlayer(damager);
				if ((player != null) && (player2 != null)) {
					player.getArena().onDamage(event, player, player2);
				}
				else if (!attacked.getName().equals("cs_hostage")) {
					event.setCancelled(true);
				} else if (player2 != null) {
					player2.setMoney(player2.getMoney() - 800);
					player2.getPlayer().sendMessage(ChatColor.RED + "You have lost " + ChatColor.GREEN + "$800 " + ChatColor.RED + "for attacking a hostage");
					if (player2.getTeam().equals(CSArena.Teams.TERRORIST)) {
						event.setCancelled(true);
						CSHostage hostage = player2.getArena().getHostage(attacked);
						if ((hostage != null) && (hostage.getFollowing() != null))
							event.setCancelled(false);
					}
				}
			}
		}
	}
}