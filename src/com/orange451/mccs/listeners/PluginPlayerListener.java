package com.orange451.mccs.listeners;

import com.orange451.mccs.CSArena;
import com.orange451.mccs.CSArena.Teams;
import com.orange451.mccs.CSHostage;
import com.orange451.mccs.CSPlayer;
import com.orange451.mccs.CSSpawn;
import com.orange451.mccs.KilledPlayer;
import com.orange451.mccs.KitArenaCreator;
import com.orange451.mccs.MCCS;
import com.orange451.mccs.CSArena.GameType;
import com.orange451.mccs.Util;
import com.orange451.mccs.item.InventoryHelper;
import com.orange451.pvpgunplus.events.PVPGunPlusBulletCollideEvent;
import com.orange451.pvpgunplus.events.PVPGunPlusGunDamageEntityEvent;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EnumClientCommand;
import net.minecraft.server.v1_7_R4.PacketPlayInClientCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PluginPlayerListener implements Listener {
	private MCCS plugin;

	public PluginPlayerListener(MCCS plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onBulletHit(PVPGunPlusBulletCollideEvent event) {
		if (event.getBlockHit().getType().equals(Material.THIN_GLASS)) {
			MCCS.getCSPlugin().glassThinReplace.add(event.getBlockHit().getLocation());
			event.getBlockHit().setType(Material.AIR);
		}
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onGunDamageEntity(PVPGunPlusGunDamageEntityEvent event) {
		if (event.isHeadshot()) {
			event.setDamage((int) (event.getDamage() * 2.5f));
		}
	}

	/*
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerReceiveNameTag(PlayerReceiveNameTagEvent event) {
		CSPlayer pl1 = this.plugin.getCSPlayer(event.getPlayer());
		CSPlayer pl2 = this.plugin.getCSPlayer(event.getNamedPlayer());
		if ((pl1 != null) && (pl2 != null))
			if (pl1.getTeam().equals(pl2.getTeam()))
				event.setTag(pl2.getTeamColor() + pl2.getPlayer().getName());
			else
				event.setTag(" ");
	}*/

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onRegainHealth(EntityRegainHealthEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ItemStack iteminhand = player.getItemInHand();
		if (iteminhand != null) {
			if (iteminhand.getType().equals(Material.ENDER_PEARL)) {
				event.setCancelled(true);
			}
			if ((event.hasBlock()) && (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) && 
					(iteminhand.getType().toString().toLowerCase().contains("sword")) && 
					(event.getClickedBlock().getType().equals(Material.THIN_GLASS))) {
				MCCS.getCSPlugin().glassThinReplace.add(event.getClickedBlock().getLocation());
				event.getClickedBlock().setType(Material.AIR);
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();

        if ((entity instanceof ItemFrame || entity instanceof Painting)) {
            event.setCancelled(true);
            return;
        }
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if ((event.getEntityType() == EntityType.ITEM_FRAME) || (event.getEntityType() == EntityType.PAINTING)) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onHangBreakByEntity(HangingBreakByEntityEvent event) {
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onHangBreak(HangingBreakByEntityEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onHangPlace(HangingPlaceEvent event) {
		Player p = event.getPlayer();
		if (!p.isOp())
			event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onInventoryOpen(InventoryOpenEvent event)
	{
		String name = event.getInventory().getTitle();
		if (!name.contains("Buy Menu")) {
			event.setCancelled(true);
			MCCS.getCSPlugin().openBuyMenu(event.getPlayer());
		}
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent event) {
		Player who = (Player)event.getWhoClicked();
		if (who != null) {
			CSPlayer csplayer = MCCS.getCSPlugin().getCSPlayer(who);
			if (csplayer != null)
				csplayer.onClickItem(event);
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onCraftItem(CraftItemEvent event)
	{
		if (!event.getWhoClicked().isOp())
			event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onInventoryClose(InventoryCloseEvent event)
	{
		String name = event.getInventory().getTitle();
		if (name.equals("Buy Menu")) {
			CSPlayer player = MCCS.getCSPlugin().getCSPlayer((Player)event.getPlayer());
			if (player != null)
				player.setInBuyMenu(false);
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player pl = event.getPlayer();
		if (event.isCancelled()) {
			return;
		}
		if (pl != null) {
			//System.out.println("Attempting to drop item...");
			Material mat = event.getItemDrop().getItemStack().getType();
			if (!mat.equals(Material.MAGMA_CREAM)) {
				// Check for gun drop
				Item item = event.getItemDrop();
				CSPlayer csplayer = MCCS.getCSPlugin().getCSPlayer(pl);
				if (!csplayer.dropGun(item.getItemStack(), 0.5f)) {
					event.setCancelled(true);
				}
				event.getItemDrop().remove();
			} else {
				CSPlayer csplayer = MCCS.getCSPlugin().getCSPlayer(pl);
				if (csplayer != null)
					MCCS.getCSPlugin().sendMessage(null, csplayer.getTeamColor() + csplayer.getPlayer().getName() + ChatColor.GRAY + " has dropped the bomb!");
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player pl = event.getPlayer();
		if (pl != null) {
			CSPlayer csplayer = MCCS.getCSPlugin().getCSPlayer(pl);
			if (csplayer != null &&  (event.getItem().getItemStack().getType().equals(Material.MAGMA_CREAM)) && (csplayer.getTeam().equals(CSArena.Teams.TERRORIST))) {
				MCCS.getCSPlugin().sendMessage(null, csplayer.getTeamColor() + csplayer.getPlayer().getName() + ChatColor.GRAY + " has picked up the bomb!");
				InventoryHelper.removeItem(pl.getInventory(), Material.MAGMA_CREAM.getId(), (byte)-1, 64);
				boolean found = false;
				for (int i = 7; i < pl.getInventory().getSize(); i++) {
					if ((!found) && (pl.getInventory().getItem(i) == null)) {
						found = true;
						pl.getInventory().setItem(i, new ItemStack(Material.MAGMA_CREAM));
						event.getItem().remove();
					}
				}
			}
			
			// gun picking up
			for (int i = 0; i < MCCS.getCSPlugin().gunsOnGround.size(); i++) {
				if (event.getItem().equals(MCCS.getCSPlugin().gunsOnGround.get(i).getWorldItem())) {
					if (MCCS.getCSPlugin().gunsOnGround.get(i).onPickup(csplayer)) {
						event.setCancelled(true);
						return;
					}
				}
			}

		}
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		boolean canLog = false;

		if (player.isOp()) {
			canLog = true;
		}
		if (!canLog) {
			Player[] players = Bukkit.getOnlinePlayers();
			int amt = players.length;
			if (amt + 1 <= Bukkit.getMaxPlayers()) {
				canLog = true;
			}
		}
		String kickMSG = "Server is full! \nJoin: " + ChatColor.BLUE + Integer.toString(this.plugin.getServerNumber() + 1) + ".minestrike.com";
		if (player.isBanned()) {
			canLog = false;
			kickMSG = "You are banned from this server";
		}
		if (canLog)
			event.allow();
		else
			event.disallow(PlayerLoginEvent.Result.KICK_FULL, kickMSG);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		this.plugin.onJoin(event.getPlayer());
		this.plugin.sendMessage(null, event.getJoinMessage());
		event.setJoinMessage(null);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerLeave(PlayerQuitEvent event) {
		this.plugin.onLeave(event.getPlayer());
		this.plugin.sendMessage(null, event.getQuitMessage());
		event.setQuitMessage(null);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDeath(final PlayerDeathEvent event) {
		try {
			event.setDroppedExp(0);
			List items = event.getDrops();
			CSPlayer csplayer = MCCS.getCSPlugin().getCSPlayer(event.getEntity());
			if (csplayer != null) {
				for (int i = items.size() - 1; i >= 0; i--) {
					if (((ItemStack)items.get(i)).getType().equals(Material.MAGMA_CREAM)) {
						if (csplayer != null) {
							csplayer.getArena().dropBomb(csplayer.getPlayer().getLocation().add(0.0D, 1.0D, 0.0D));
							csplayer.getArena().broadcastMessage(csplayer.getTeamColor() + csplayer.getPlayer().getName() + ChatColor.GRAY + " has dropped the bomb!");
						}
					}
				}
				
				if (!csplayer.dropGun(csplayer.getPrimary(), -1)) {
					csplayer.dropGun(csplayer.getSecondary(), -1);
				}
				event.getDrops().clear();
				event.setDeathMessage("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Entity e = event.getEntity();
			if ((e != null) && ((e instanceof Player))) {
				CSPlayer csplayer = MCCS.getCSPlugin().getCSPlayer((Player)e);
				if (csplayer != null) {
					Player killed = (Player)e;
					Entity damager = csplayer.getLastAttacker();
					if (damager != null) {
						new KilledPlayer().execute(killed, damager);
					} else {
						csplayer.getArena().onDeath(csplayer);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		MCCS.getCSPlugin().getServer().getScheduler().scheduleSyncDelayedTask(MCCS.getCSPlugin(), new Runnable() {
			public void run() {
				PacketPlayInClientCommand in = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
		        EntityPlayer cPlayer = ((CraftPlayer)event.getEntity()).getHandle();
		        cPlayer.playerConnection.a(in);
			}
		}, 2L);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		try {
			Player pl = event.getPlayer();
			if (pl != null) {
				CSPlayer kp = this.plugin.getCSPlayer(pl);
				if (kp != null) {
					CSSpawn spawn = kp.getArena().getSpawn(kp);
					if (spawn != null) {
						event.setRespawnLocation(spawn.getLocation());
						kp.setLastSpawnTicks(0);
						kp.setSpawnLoc(spawn.getLocation());
						if (!kp.getArena().getGameType().equals(CSArena.GameType.LOBBY))
							kp.getArena().openBuyMenu(kp);
					}
					else {
						System.out.println("NO SPAWN");
					}
				}
			}
		}
		catch (Exception localException)
		{
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerChat(PlayerChatEvent event) {
		Player p = event.getPlayer();
		if (this.plugin.getCSPlayer(p) != null)
			this.plugin.sendMessage(null, event.getPlayer(), event.getMessage());
		else {
			this.plugin.sendNonCSMessage(event.getPlayer(), event.getMessage());
		}
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String[] split = event.getMessage().split(" ");
		split[0] = split[0].substring(1);
		String label = split[0];
		String[] args = new String[split.length - 1];

		for (int pl = 1; pl < split.length; pl++) {
			args[(pl - 1)] = split[pl];
		}
		
		if (label.equalsIgnoreCase("texture")) {
			event.setCancelled(true);
			this.plugin.sendMessage(player, "" + ChatColor.BOLD + ChatColor.GREEN + "Texturepack: " + ChatColor.RESET + ChatColor.RED + "www.mcbrawl.com/texture/mccs.zip");
		}

		if (label.equalsIgnoreCase("help")) {
			event.setCancelled(true);
			this.plugin.sendMessage(player, ChatColor.YELLOW + "--------MCCS HELP--------");
			this.plugin.sendMessage(player, "/cs join" + ChatColor.GRAY + "To join");
			this.plugin.sendMessage(player, "/cs leave" + ChatColor.GRAY + "To leave");
		}

		if (label.equalsIgnoreCase("buy")) {
			event.setCancelled(true);
			this.plugin.sendMessage(player, ChatColor.BLUE + "To Donate: \n" + ChatColor.RED + "http://multiplayerservers.com/donation");
		}
		
		if (label.equalsIgnoreCase("bot")) {
			event.setCancelled(true);
			new CSHostage(null, "cs_hostage", player.getLocation().add(0, 10, 0));
		}

		if (label.equalsIgnoreCase("spawn")) {
			event.setCancelled(true);
			if (this.plugin.getCSPlayer(player) != null) {
				this.plugin.onLeave(player);
				this.plugin.onJoin(player);
			}
			player.teleport(((World)Bukkit.getWorlds().get(0)).getSpawnLocation().clone().add(0.5D, 1.0D, 0.5D));
		}
		if (label.equalsIgnoreCase("fp")) {
			event.setCancelled(true);
			CSPlayer csplayer = this.plugin.getCSPlayer(player);
			if ((csplayer != null) &&  (player.isOp())) {
				CSArena newarr = MCCS.getCSPlugin().getArena(args[0]);
				if (newarr != null) {
					csplayer.getArena().setNextMap(newarr);
				}
			}
		}
		
		if (label.equalsIgnoreCase("pause")) {
			event.setCancelled(true);
			if (player.isOp()) {
				ArrayList<CSArena> arenas = plugin.getArenas();
				for (int i = 0; i < arenas.size(); i++) {
					if (!arenas.get(i).getGameType().equals(GameType.LOBBY)) {
						arenas.get(i).paused = !arenas.get(i).paused;
						if (arenas.get(i).paused) {
							arenas.get(i).broadcastMessage(ChatColor.RED + "ARENA PAUSED...");
						}else{
							arenas.get(i).broadcastMessage(ChatColor.GREEN + "ARENA RESUMED...");
						}
					}
				}
			}
		}
		
		if (label.equalsIgnoreCase("team")) {
			event.setCancelled(true);
			if (player.isOp()) {
				if (args.length == 2) {
					String playername = args[0];
					String tempTeam = args[1];
					int team = Integer.parseInt(tempTeam);
					Teams newTeam = Teams.COUNTER_TERRORIST;
					if (team == 1)
						newTeam = Teams.TERRORIST;
					
					Player mplayer = Util.MatchPlayer(playername);
					if (mplayer != null) {
						CSPlayer csplayer = plugin.getCSPlayer(mplayer);
						csplayer.setTeam(newTeam);
						csplayer.getPlayer().sendMessage("You have been put on: " + newTeam.toString());
					}
				}
			}
		}
		
		if (label.equalsIgnoreCase("test")) {
			event.setCancelled(true);
			ArrayList<CSArena> arenas = MCCS.getCSPlugin().getArenas();
			player.sendMessage(ChatColor.DARK_GRAY + "Current MCCS Arenas:");
			for (int i = 0; i < arenas.size(); i++) {
				String status = ChatColor.YELLOW + "[idling]";
				if (arenas.get(i).getActivePlayers().size() > 0)
					status = ChatColor.GREEN + "[active]";
				String name = ChatColor.GRAY + arenas.get(i).getName();
				player.sendMessage("  " + status + " " + name + " ");
			}
		}
		
		if (label.equalsIgnoreCase("restart")) {
			event.setCancelled(true);
			if (player.isOp()) {
				ArrayList<CSArena> arenas = plugin.getArenas();
				for (int i = 0; i < arenas.size(); i++) {
					CSArena arena = arenas.get(i);
					if (!arena.getGameType().equals(GameType.LOBBY)) {
						if (arena.isRunning() == true) {
							arena.winsTerrorist = 0;
							arena.winsCounterTerrorist = 0;
							
							arena.setPlays(1);
							arena.restart();
							arena.paused = false;
							
							MCCS.broadcastMessage(ChatColor.GREEN + "ARENA RESTARTED");
						}
					}
				}
			}
		}
		
		if (label.equalsIgnoreCase("autobalance")) {
			event.setCancelled(true);
			if (player.isOp()) {
				MCCS.getCSPlugin().autoBalance = !MCCS.getCSPlugin().autoBalance;
				if (MCCS.getCSPlugin().autoBalance)
					MCCS.broadcastMessage("Autobalance turned on");
				else
					MCCS.broadcastMessage("Autobalance turned off");
			}
		}
		
		if (label.equalsIgnoreCase("Knife")) {
			event.setCancelled(true);
			if (player.isOp()) {
				ArrayList<CSArena> arenas = plugin.getArenas();
				for (int i = 0; i < arenas.size(); i++) {
					CSArena arena = arenas.get(i);
					if (!arena.getGameType().equals(GameType.LOBBY)) { // Force restarts round, and turns on knife round
						if (arena.isRunning() == true) {
							arena.winsTerrorist = 0;
							arena.winsCounterTerrorist = 0;
							arena.knifeRound = true;
							arena.setPlays(1);
							arena.restart();
							arena.paused = false;
							
							MCCS.broadcastMessage(ChatColor.GREEN + "KNIFE ROUND!!!!");
						}
					}
				}
			}
		}

		if (label.equalsIgnoreCase("cs")) {
			event.setCancelled(true);
			if ((args[0].equals("create")) && 
					(player.isOp())) {
				String arenaName = args[1];
				String arenaType = args[2];
				if (this.plugin.getArena(arenaName) == null) {
					KitArenaCreator kac = this.plugin.getKitArenaCreator(player);
					if (kac == null)
						this.plugin.creatingArena.add(new KitArenaCreator(player, arenaName, arenaType));
					else
						player.sendMessage("You are already creating an arena!");
				}
				else {
					player.sendMessage("A Kit Arena with this name already exists!");
				}

			}

			if ((args[0].equals("setpoint")) && 
					(player.isOp())) {
				KitArenaCreator kac = this.plugin.getKitArenaCreator(player);
				if (kac != null) {
					kac.setPoint();
				}

			}

			if ((args[0].equals("addspawn")) && 
					(player.isOp())) {
				KitArenaCreator kac = this.plugin.getKitArenaCreator(player);
				if (kac != null) {
					kac.addSpawn();
				}

			}

			if ((args[0].equals("done")) && 
					(player.isOp())) {
				KitArenaCreator kac = this.plugin.getKitArenaCreator(player);
				if (kac != null) {
					kac.finish();
				}

			}

			if (args[0].equals("join")) {
				CSPlayer csplayer = this.plugin.getCSPlayer(player);
				if (csplayer == null)
					this.plugin.onJoin(player);
				else {
					this.plugin.sendMessage(player, "You cannot join at this time");
				}
			}

			if (args[0].equals("leave")) {
				CSPlayer csplayer = this.plugin.getCSPlayer(player);
				if (csplayer != null)
					this.plugin.onLeave(player);
				else
					this.plugin.sendMessage(player, "You cannot leave at this time");
			}
		}
	}
}