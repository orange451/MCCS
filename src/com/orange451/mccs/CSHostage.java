package com.orange451.mccs;

import com.orange451.SpawnHuman.human.HumanNPC;
import com.orange451.SpawnHuman.human.NPC;
import com.orange451.SpawnHuman.util.Util;
import com.orange451.opex.permissions.PermissionInterface;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CSHostage {
	private HumanNPC npc;
	private CSPlayer following;
	private boolean alive = true;
	private int ticks;
	private Location startingLocation;
	private int noAliveTicks = 0;
	private CSArena arena;
	private int untieTimer = 5;

	public CSHostage(CSArena arena, String name, Location loc) {
		NPC npc = MCCS.getCSPlugin().npcRegistry.createNPC(name);
		npc.spawn(loc);
		this.arena = arena;
		this.startingLocation = loc;

		this.npc = ((HumanNPC)npc);
		tick();

		onSpawn();
	}

	public void delete() {
		this.npc.destroy();
		this.alive = false;
		this.noAliveTicks = 999;
	}

	public void tick() {
		if (!this.alive)
			return;
		this.ticks += 1;

		if (this.npc.getBukkitEntity() != null) {
			if (((Damageable)this.npc.getBukkitEntity()).getHealth() <= 0) {
				this.alive = false;
				this.arena.broadcastMessage(ChatColor.RED + "Hostage down!");
				this.following = null;
				System.out.println("HOSTAGE DOWN");
			}

			List nearbyE = this.npc.getBukkitEntity().getNearbyEntities(1.5D, 1.5D, 1.5D);
			for (int i = nearbyE.size() - 1; i >= 0; i--) {
				if ((nearbyE.get(i) instanceof Player)) {
					Player near = (Player)nearbyE.get(i);
					if (this.following == null) {
						follow(near);
					}
				}
			}

			if (this.following != null) {
				if (this.following.getPlayer().isDead()) {
					this.following = null;
					this.untieTimer = 5;
					return;
				}

				Location to = this.following.getPlayer().getLocation();
				double d2p = to.distance(this.npc.getBukkitEntity().getLocation());

				this.npc.isHoldingSpace = false;
				if (((to.getY() > this.npc.getBukkitEntity().getLocation().getY()) || (this.following.getPlayer().getVelocity().getY() > 0.05D)) && 
						(!this.npc.isInWater) && 
						(this.npc.isOnGround)) {
					this.npc.jump();
				}

				Util.faceLocation(this.npc.getBukkitEntity(), this.following.getPlayer().getLocation());

				if (d2p >= 3.0D) {
					walkTo(to);
				}
				double dist = ((CSSpawn)this.following.getArena().getSpawns().get(1)).getLocation().distance(this.npc.getBukkitEntity().getLocation());
				if (dist <= 6.0D) {
					this.alive = false;
					this.following.getArena().broadcastMessage(ChatColor.AQUA + "HOSTAGE RESCUED!");
					this.following.getArena().endGame(CSArena.Teams.COUNTER_TERRORIST, "Counter-terrorists win!");
					this.npc.destroy();
					this.following = null;
				}
			} else {
				this.npc.getBukkitEntity().setVelocity(new Vector(0.0D, this.npc.getBukkitEntity().getVelocity().getY(), 0.0D));
			}
		} else {
			this.noAliveTicks += 1;
			if (this.noAliveTicks == 25) {
				this.alive = false;
				this.arena.broadcastMessage(ChatColor.RED + "Hostage down!");
			}
		}
	}

	private void onSpawn() {
		this.npc.onSpawn();
	}

	private void follow(Player near) {
		CSPlayer player = MCCS.getCSPlugin().getCSPlayer(near);
		if ((player != null) && 
				(player.getTeam().equals(CSArena.Teams.COUNTER_TERRORIST)))
			if (near.isSneaking()) {
				if (this.untieTimer <= 0) {
					player.getArena().broadcastMessage(ChatColor.GREEN + "A hostage has been taken!");
					player.getPlayer().sendMessage(ChatColor.DARK_GRAY + "<HOSTAGE>" + ChatColor.GRAY + " I am following you!");
					this.following = player;
				}
				if (this.ticks % 20 == 0) {
					if (PermissionInterface.hasPermission(near, "minestrike.mvp"))
						this.untieTimer = 0;
					if (this.untieTimer >= 0)
						player.getPlayer().sendMessage(ChatColor.GRAY + "untying hostage...");
					this.untieTimer -= 1;
				}
			}
			else if (this.ticks % 20 == 0) {
				player.getPlayer().sendMessage(ChatColor.GRAY + "Sneak to let me know it's safe!");
			}
	}

	private void walkTo(Location loc)
	{
		Vector to = Util.getDirection(this.npc.getBukkitEntity().getLocation(), loc).multiply(0.29D).setY(0);
		if (this.npc.isSprinting)
			to.multiply(1.5D);
		this.npc.getBukkitEntity().setVelocity(to.multiply(1.5D).setY(this.npc.getBukkitEntity().getVelocity().getY()));
		Util.faceLocation(this.npc.getBukkitEntity(), loc);
	}

	public HumanNPC getNPC() {
		return this.npc;
	}

	public CSPlayer getFollowing() {
		return this.following;
	}
}