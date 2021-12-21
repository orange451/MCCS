package com.orange451.mccs;

import com.orange451.mccs.CSArena.Teams;
import com.orange451.mccs.item.InventoryHelper;
import com.orange451.opex.permissions.PermissionInterface;
import com.orange451.pvpgunplus.ParticleEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CSBombPoint
{
	public boolean planted;
	public int maxTime = 40;
	public int timer = this.maxTime;
	public int ticks;
	public CSArena arena;
	public Location loc;
	public int planting = 0;
	public boolean disabled = false;
	public int tickWaitTimer = 1;
	public String bomb = "A";
	public boolean anyoneOn = false;
	public boolean exploded;

	public CSBombPoint(CSArena arena, Location loc) {
		this.arena = arena;
		this.loc = loc;
	}

	public void tick() {
		if (this.disabled)
			return;
		this.ticks += 1;

		if ((this.ticks % this.tickWaitTimer == 0) && (this.planted) && (this.timer > 0)) {
			ding();
		}
		if ((this.ticks % 100 == 0) && (this.planted) && (this.timer >= 0)) {
			this.arena.broadcastMessage(ChatColor.GRAY + "Bomb blows up in " + ChatColor.GREEN + Integer.toString(this.timer) + ChatColor.GRAY + " seconds");
		}

		if (this.timer == -2) {
			((World)Bukkit.getWorlds().get(0)).playSound(this.loc, Sound.EXPLODE, 4.0F, 1.3F);
			Random rand = new Random();
			for (int i = 0; i < 7; i++) {
				int rx = rand.nextInt(8);
				int rz = rand.nextInt(8);
				int ry = rand.nextInt(4);
				//new CSExplosion(this.loc.clone().add(rx - 4, ry - 2, rz - 4)).explode();
				ParticleEffects.sendParticle(null, 64, "largeexplode", this.loc.clone().add(rx - 4, ry - 2, rz - 4), 0.3f, 0.3f, 0.3f, 0.2f, 1);
			}
			
			if (!exploded) {
				exploded = true;
				ArrayList<CSPlayer> check = this.arena.getActivePlayers();
				for (int ii = 0; ii < check.size(); ii++) {
					CSPlayer player = check.get(ii);
					float dist = (float) player.getPlayer().getLocation().distance(loc);
					if (dist < 16)
						player.damageFromWorld(999);
					else if (dist < 20)
						player.damageFromWorld(16);
					else if (dist < 24)
						player.damageFromWorld(12);
					else if (dist < 28)
						player.damageFromWorld(8);
					else if (dist < 32)
						player.damageFromWorld(4);
				}
			}
		}

		if (this.ticks % 20 == 0) {
			ArrayList check = this.arena.getActivePlayers();
			if ((this.planted) || (this.timer <= 0)) {
				this.timer -= 1;
				if (this.timer == -5) {
					this.arena.endGame(CSArena.Teams.TERRORIST, "Terrorists win!");
				}

				if (this.timer == 0) {
					((World)Bukkit.getWorlds().get(0)).playSound(this.loc, Sound.FUSE, 4.0F, 1.0F);
				}
			}

			if (this.planting > 3) {
				if (!this.planted) {
					this.planting = 10;
					this.arena.broadcastMessage(ChatColor.GREEN + "THE BOMB HAS BEEN PLANTED (" + this.bomb + ")");
					Item it = ((World)Bukkit.getWorlds().get(0)).dropItem(this.loc, new ItemStack(Material.MAGMA_CREAM, 1));
					it.setPickupDelay(3600);
					
					ArrayList<CSPlayer> players = arena.getActivePlayers();
					for (int i = 0; i < players.size(); i++) {
						if (players.get(i).getTeam().equals(Teams.TERRORIST)) {
							players.get(i).giveMoney(800);
							players.get(i).getPlayer().sendMessage(ChatColor.GRAY + "You have been given: " + ChatColor.GREEN + "$800");
						}
					}
				}
				this.planted = true;
			}
			if ((this.planting <= 0) && (this.planted)) {
				this.planted = false;
				this.disabled = true;
				this.arena.endGame(CSArena.Teams.COUNTER_TERRORIST, "Counter-Terrorists win!");
			}

			if (this.timer > -1)
				if (!this.planted) {
					int amt = 0;
					for (int i = 0; i < check.size(); i++) {
						if ((((CSPlayer)check.get(i)).getPlayer() != null) && (((CSPlayer)check.get(i)).getTeam().equals(CSArena.Teams.TERRORIST)) && (((CSPlayer)check.get(i)).getPlayer().getLocation().distance(this.loc) < 3.5D) && (((CSPlayer)check.get(i)).getPlayer().getItemInHand() != null)) {
							if (((CSPlayer)check.get(i)).getPlayer().getItemInHand().getType().equals(Material.MAGMA_CREAM)) {
								if (((CSPlayer)check.get(i)).getPlayer().isSneaking()) {
									amt = 1;
									((World)Bukkit.getWorlds().get(0)).playSound(this.loc, Sound.ORB_PICKUP, 1.8F, 2.0F);								
									this.anyoneOn = true;
									((CSPlayer)check.get(i)).getPlayer().sendMessage(ChatColor.GRAY + "PLANTING BOMB: " + ChatColor.AQUA + Integer.toString(3 - this.planting));
									this.planting += 1;
									if (this.planting > 3)
										InventoryHelper.removeItem(((CSPlayer)check.get(i)).getPlayer().getInventory(), Material.MAGMA_CREAM.getId(), (byte)-1, 64);
								}
								else {
									((CSPlayer)check.get(i)).getPlayer().sendMessage(ChatColor.GRAY + "Crouch to plant the bomb!");
								}
							}
						}

					}

					if (amt == 0) {
						this.planting = 0;
						this.anyoneOn = false;
					}
				} else {
					for (int i = 0; i < check.size(); i++) {
						if ((((CSPlayer)check.get(i)).getPlayer() != null) && (((CSPlayer)check.get(i)).getTeam().equals(CSArena.Teams.COUNTER_TERRORIST)) && (((CSPlayer)check.get(i)).getPlayer().getLocation().distance(this.loc) < 3.5D)) {
							if (((CSPlayer)check.get(i)).getPlayer().isSneaking()) {
								((CSPlayer)check.get(i)).getPlayer().sendMessage(ChatColor.GRAY + "DEFUSING: " + ChatColor.AQUA + Integer.toString(this.planting));
								//if ((!this.anyoneOn) && (PermissionInterface.hasPermission(((CSPlayer)check.get(i)).getPlayer(), "minestrike.mvp")))
									//this.planting = 4;
								this.planting -= 1;
								if (!this.anyoneOn) {
									((World)Bukkit.getWorlds().get(0)).playSound(this.loc, Sound.BAT_TAKEOFF, 1.4F, 1.0F);							
								}
								this.anyoneOn = true;
								return;
							}
							((CSPlayer)check.get(i)).getPlayer().sendMessage(ChatColor.GRAY + "Crouch to defuse!");
						}

					}

					this.planting = 10;
					this.anyoneOn = false;
				}
		}
	}

	private void ding() {
		((World)Bukkit.getWorlds().get(0)).playSound(this.loc, Sound.NOTE_PLING, 1.0F, 2.0F);
		((World)Bukkit.getWorlds().get(0)).playSound(this.loc, Sound.NOTE_SNARE_DRUM, 1.0F, 1.0F);
		((World)Bukkit.getWorlds().get(0)).playSound(this.loc, Sound.NOTE_STICKS, 2.0F, 2.0F);

		this.tickWaitTimer = 30;
		if (this.timer <= 30) {
			this.tickWaitTimer = 20;
		}
		if (this.timer <= 20) {
			this.tickWaitTimer = 15;
		}
		if (this.timer <= 15) {
			this.tickWaitTimer = 10;
		}
		if (this.timer <= 10) {
			this.tickWaitTimer = 5;
		}
		if (this.timer <= 5) {
			this.tickWaitTimer = 3;
		}
		if (this.timer <= 2)
			this.tickWaitTimer = 1;
	}
}