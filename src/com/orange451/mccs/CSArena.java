package com.orange451.mccs;

import com.orange451.SpawnHuman.NPCManager;
import com.orange451.SpawnHuman.human.HumanNPC;
import com.orange451.mccs.item.CSItem;
import com.orange451.mccs.item.InventoryHelper;
import com.orange451.mccs.music.Song;
import com.orange451.mccs.music.SongBuy;
import com.orange451.mccs.music.SongStart;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class CSArena
{
	private ArrayList<CSPlayer> players = new ArrayList<CSPlayer>();
	private ArrayList<CSSpawn> spawns = new ArrayList<CSSpawn>();
	private ArrayList<CSBombPoint> bombs = new ArrayList<CSBombPoint>();
	private ArrayList<CSSpawn> hostageSpawn = new ArrayList<CSSpawn>();
	private ArrayList<CSHostage> hostages = new ArrayList<CSHostage>();
	private Field field;
	private CSArena nextMap;
	private CSArena lastMap;
	private String name;
	private GameType gameType;
	private GameModeModifier gameModifier = GameModeModifier.NONE;
	private boolean isRunning = false;
	private int aliveTicks;
	private int timer;
	private int ticksStart;
	private int timerMax;
	private int redScore;
	private int blueScore;
	private int plays = 1;
	private int maxPlays = 10;
	private boolean canPlay = true;
	private Song currentSong;
	private Song buySong;
	private Song startSong;
	public ArrayList<String> guns = new ArrayList<String>();
	public int winsTerrorist = 0;
	public int winsCounterTerrorist = 0;
	public boolean knifeRound = false;
	
	public boolean paused;

	public CSArena(String name) {
		this.name = name;
		this.nextMap = this;
		this.lastMap = this;
		this.field = new Field();

		loadArena();

		if (this.gameModifier.equals(GameModeModifier.DEMOLITION)) {
			this.bombs.add(new CSBombPoint(this, ((CSSpawn)this.spawns.get(2)).getLocation()));
			this.bombs.add(new CSBombPoint(this, ((CSSpawn)this.spawns.get(3)).getLocation()));
			((CSBombPoint)this.bombs.get(1)).bomb = "B";
			this.spawns.remove(3);
			this.spawns.remove(2);
		}

		if (this.gameModifier.equals(GameModeModifier.HOSTAGE_RESCUE)) {
			for (int i = this.spawns.size() - 1; i >= 2; i--) {
				this.hostageSpawn.add(new CSSpawn(((CSSpawn)this.spawns.get(i)).getLocation()));
				this.spawns.remove(i);
			}
		}

		this.buySong = new SongBuy();
		this.startSong = new SongStart();

		this.guns.add("P2000");
		this.guns.add("P250");

		this.guns.add("M4A4");
		this.guns.add("Famas");
		this.guns.add("SSG_08");
		this.guns.add("AWP");
		this.guns.add("AUG");

		this.guns.add("MP7");
		this.guns.add("PP_Bizon");
		this.guns.add("P90");
		this.guns.add("Ump-45");

		this.guns.add("XM1014");
		this.guns.add("Negev");
		this.guns.add("Sawed_Off");
	}

	public void tick() {
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if (this.players.get(i) != null) {
				try {
					((CSPlayer)this.players.get(i)).tick();
					if (this.ticksStart > 140)
						((CSPlayer)this.players.get(i)).setCanMove(true);
					if (this.paused)
						((CSPlayer)this.players.get(i)).setCanMove(false);
					
					
					if (this.ticksStart == 20) {
						((CSPlayer)this.players.get(i)).getPlayer().teleport(((CSPlayer)this.players.get(i)).getSpawnLoc());
						if (!((CSPlayer)this.players.get(i)).isInBuyMenu()) {
							openBuyMenu((CSPlayer)this.players.get(i));
						}
					}
					
					
					if (this.ticksStart > 800) {
						((CSPlayer)this.players.get(i)).setInBuyMenu(false);
						if (((CSPlayer)this.players.get(i)).buyInventory != null) {
							((CSPlayer)this.players.get(i)).buyInventory.clear();
						}
					}

					String name = ((CSPlayer)this.players.get(i)).getPlayer().getName();
					if (name.length() + 2 >= 16)
						name = name.substring(0, 14);
					((CSPlayer)this.players.get(i)).getPlayer().setPlayerListName(((CSPlayer)this.players.get(i)).getTeamColor() + name);
				}
				catch (Exception localException)
				{
				}
			}
		}
		if (!this.isRunning() || this.paused) {
			return;
		}
		this.aliveTicks += 1;
		this.ticksStart += 1;
		for (int i = 0; i < this.bombs.size(); i++) {
			((CSBombPoint)this.bombs.get(i)).tick();
		}
		for (int i = 0; i < this.hostages.size(); i++) {
			((CSHostage)this.hostages.get(i)).tick();
		}

		if (this.ticksStart == 20) {
			Item ii = spawnBomb();
			if (ii != null) {
				ii.setPickupDelay(120);
			}
			if (this.gameModifier.equals(GameModeModifier.HOSTAGE_RESCUE)) {
				for (int i = 0; i < this.hostageSpawn.size(); i++) {
					this.hostages.add(new CSHostage(this, "cs_hostage", ((CSSpawn)this.hostageSpawn.get(i)).getLocation()));
				}
			}
		}

		if (this.gameType.equals(GameType.LOBBY)) {
			if (this.nextMap != null) {
				this.winsTerrorist = this.nextMap.winsTerrorist;
				this.winsCounterTerrorist = this.nextMap.winsCounterTerrorist;
				this.plays = this.nextMap.getRound();
				if (this.ticksStart % 201 == 0)
					announceMap();
			}
		}
		else if (this.ticksStart == 161) {
			setSong(this.startSong);
		}

		if ((this.aliveTicks % 20 == 0) && (this.canPlay)) {
			this.timer -= 1;
			boolean canRunOutOfTime = true;
			if (this.gameModifier.equals(GameModeModifier.DEMOLITION)) {
				for (int i = 0; i < bombs.size(); i++) {
					if (bombs.get(i).planted)
						canRunOutOfTime = false;
				}
			}
			if (this.timer <= 0 && canRunOutOfTime) {
				onOutOfTime();
				return;
			}
			if (!this.gameType.equals(GameType.LOBBY)) {
				checkScores();
				checkTeams();
			}
		}

		if (this.currentSong != null) {
			this.currentSong.tick();
			if (this.currentSong.finished)
				this.currentSong = null;
		}
	}

	public int getTeamScore(Teams team) {
		if (team.equals(Teams.TERRORIST))
			return this.redScore;
		if (team.equals(Teams.COUNTER_TERRORIST)) {
			return this.blueScore;
		}
		return 0;
	}

	public void endGame(String reason) {
		endGame(Teams.NEUTRAL, reason);
	}

	public void endGame(CSPlayer winner, String reason)
	{
	}

	public void endGame(Teams team, String reason) {
		clearNearbyEntities();
		
		for (int i = 0; i < this.bombs.size(); i++) {
			((CSBombPoint)this.bombs.get(i)).disabled = false;
			((CSBombPoint)this.bombs.get(i)).planted = false;
			((CSBombPoint)this.bombs.get(i)).planting = 0;
			((CSBombPoint)this.bombs.get(i)).exploded = false;
			((CSBombPoint)this.bombs.get(i)).timer = ((CSBombPoint)this.bombs.get(i)).maxTime;
		}
		
		MCCS.getCSPlugin().replaceGlass();
		if (this.gameType.equals(GameType.LOBBY)) {
			if ((this.nextMap != null) && (this.canPlay)) {
				if (this.players.size() <= 1) {
					start();
				} else {
					mergePlayers(this.nextMap, true, ChatColor.RED + "START! " + ChatColor.YELLOW + this.nextMap.getName());
					this.nextMap.restart();
					this.timer = 999999999;
					this.timerMax = 99999999;
					this.canPlay = false;
					this.nextMap.winsTerrorist = 0;
					this.nextMap.winsCounterTerrorist = 0;
				}
			} else {
				broadcastMessage("COULD NOT FIND MAP! RESTARTING LOBBY");
				start();
			}
		} else {
			for (int i = 0; i < this.hostages.size(); i++) {
				((CSHostage)this.hostages.get(i)).delete();
			}
			MCCS.getCSPlugin().npcRegistry.deregisterAll();
			CSArena lobby = MCCS.getCSPlugin().getArenaByType("lobby");
			if (lobby != null) {
				this.plays += 1;
				
				if (team.equals(Teams.COUNTER_TERRORIST))
					this.winsCounterTerrorist += 1;
				if (team.equals(Teams.TERRORIST)) {
					this.winsTerrorist += 1;
				}
				
				if (this.knifeRound) {
					logFinish();
					MCCS.broadcastMessage(team.toString() + " won the knife round!");
					this.setRunning(false);
					this.plays = 1;
					//csPlayer players = this.getActivePlayers();
					mergePlayers(lobby, false, team.toString() + " won the knife round!");
					lobby.start();
					lobby.setNextMap(this);
					this.knifeRound = false;
					return;
				}
				
				if (this.plays <= this.maxPlays) {
					String message = "";
					if (team.equals(Teams.NEUTRAL)) {
						message = "NO WINNER";
					} else {
						message = reason;
					}

					MCCS.broadcastMessage(message + "  " + ChatColor.BLUE + Integer.toString(this.winsCounterTerrorist) + ChatColor.GRAY + " to " + ChatColor.RED + Integer.toString(this.winsTerrorist));
					
					for (int i = 0; i < this.players.size(); i++) {
						((CSPlayer)this.players.get(i)).setTeamReturnTo(((CSPlayer)this.players.get(i)).getTeam());
					}

					lobby.clearPlayerInventory();
					lobby.mergePlayers(this, false, "You have joined: " + ChatColor.RED + this.name);
					for (int i = 0; i < this.players.size(); i++) {
						int givemoney = 1600;
						if ((team != null) && (((CSPlayer)this.players.get(i)).getTeam().equals(team))) {
							givemoney = 3250;
						}
						givemoney += ((CSPlayer)this.players.get(i)).loseStreak * 300;
						if (givemoney > 3200)
							givemoney = 3200;
						if (!players.get(i).getTeam().equals(team)) {
							players.get(i).loseStreak++;
						} else {
							players.get(i).loseStreak = 0;
						}
						((CSPlayer)this.players.get(i)).giveMoney(givemoney);
						((CSPlayer)this.players.get(i)).getPlayer().sendMessage(ChatColor.GRAY + "You have been awarded " + ChatColor.GREEN + Integer.toString(givemoney) + "$");
					}

					for (int i = 0; i < this.players.size(); i++) {
						((CSPlayer)this.players.get(i)).setTeam(((CSPlayer)this.players.get(i)).getTeamToReturnTo());
					}

					balanceTeams(false);
					balanceTeams(true);

					if (this.players.size() <= 1) {
						this.setRunning(false);
						this.plays = 1;
						mergePlayers(lobby, false, "Game over!");
						lobby.start();
					} else {
						lobby.canPlay = false;
						restart();
					}
				} else {
					MCCS.broadcastMessage("GAME OVER!  " + ChatColor.BLUE + Integer.toString(this.winsCounterTerrorist) + ChatColor.GRAY + " to " + ChatColor.RED + Integer.toString(this.winsTerrorist));
					logFinish();
					this.setRunning(false);
					this.plays = 1;
					mergePlayers(lobby, false, "Game over!");
					lobby.start();
				}
			}
		}
		this.knifeRound = false;
	}
	
	private void logFinish() {
		System.out.println("---------GAME OVER---------");
		System.out.println(" - Team Terrorist: " + this.winsTerrorist);
		for (int i = 0; i < this.players.size(); i++) {
			if (players.get(i).getTeam().equals(Teams.TERRORIST)) {
				System.out.println("   - " + players.get(i).getPlayer().getName());
			}
		}
		System.out.println(" - Team Counter-Terrorist: " + this.winsCounterTerrorist);
		for (int i = 0; i < this.players.size(); i++) {
			if (players.get(i).getTeam().equals(Teams.COUNTER_TERRORIST)) {
				System.out.println("   - " + players.get(i).getPlayer().getName());
			}
		}
		System.out.println("---------------------------");
	}

	private void clearPlayerInventory() {
		for (int i = 0; i < this.players.size(); i++)
			if (((CSPlayer)this.players.get(i)).getPlayer() != null)
				((CSPlayer)this.players.get(i)).clear();
	}

	public void broadcastMessage(String message)
	{
		for (int i = 0; i < this.players.size(); i++)
			((CSPlayer)this.players.get(i)).getPlayer().sendMessage(message);
	}

	private void checkTeams()
	{
		int amtTerr = getPlayersOnTeam(Teams.TERRORIST).size();
		int amtCTer = getPlayersOnTeam(Teams.COUNTER_TERRORIST).size();
		if (amtTerr == 0) {
			boolean can = true;
			if (this.gameModifier.equals(GameModeModifier.DEMOLITION)) {
				for (int i = 0; i < this.bombs.size(); i++) {
					if (((CSBombPoint)this.bombs.get(i)).planted) {
						can = false;
					}
				}
			}
			if (can)
				endGame(Teams.COUNTER_TERRORIST, "Counter terrorists win!");
		}
		if (amtCTer == 0)
			endGame(Teams.TERRORIST, "Terrorists win!");
	}

	private void checkScores() {
		if (this.gameType.equals(GameType.TDM)) {
			int redKills = getTeamScore(Teams.TERRORIST);
			int blueKills = getTeamScore(Teams.COUNTER_TERRORIST);
			if (redKills > 90)
				endGame(Teams.TERRORIST, "Terrorists WIN");
			else if (blueKills > 90)
				endGame(Teams.COUNTER_TERRORIST, "Counter-Terrorists WIN");
		} else {
			this.gameType.equals(GameType.FFA);
		}
	}

	private void onOutOfTime() {
		if (gameModifier.equals(GameModeModifier.DEMOLITION)) {
			endGame(Teams.COUNTER_TERRORIST, "Counter Terrorists win!");
		}else if (gameModifier.equals(GameModeModifier.HOSTAGE_RESCUE)) {
			endGame(Teams.TERRORIST, "Terrorists win!");
		}else{
			endGame("NO-TIME");
		}
	}

	public void changeArena(CSPlayer csPlayer, CSArena arena, String message) {
		leave(csPlayer);
		arena.join(csPlayer);
		csPlayer.getPlayer().sendMessage(message);
	}

	private void mergePlayers(CSArena arena, boolean kill, String message) {
		ArrayList<CSPlayer> temp = new ArrayList<CSPlayer>();
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if (this.players.get(i) != null) {
				temp.add((CSPlayer)this.players.get(i));
			}
		}
		for (int i = 0; i < temp.size(); i++) {
			if (temp.get(i) != null) {
				CSPlayer player = (CSPlayer)temp.get(i);
				leave(player);
				arena.join(player);
				player.getPlayer().sendMessage(message);
			}
		}
		this.players.clear();
		temp.clear();
	}

	private Item spawnBomb() {
		System.out.println("SPAWNING BOMB");
		clearNearbyEntities();
		if (this.gameModifier.equals(GameModeModifier.DEMOLITION)) {
			return dropBomb(((CSSpawn)this.spawns.get(0)).getLocation());
		}
		return null;
	}
	
	public void clearNearbyEntities() {
		List<?> entities = ((World)Bukkit.getWorlds().get(0)).getEntities();
		for (int i = entities.size() - 1; i >= 0; i--) {
			Entity e = (Entity)entities.get(i);
			if (((e instanceof Item)) && (e.getLocation().distance(((CSSpawn)this.spawns.get(0)).getLocation()) < 450.0D)) {
				e.remove();
			}
		}
	}

	public Item dropBomb(Location location) {
		Item ret = location.getWorld().dropItem(location, new ItemStack(Material.MAGMA_CREAM, 1));
		ItemStack itm = ret.getItemStack();
		ItemMeta im = itm.getItemMeta();
		ArrayList<String> arr = new ArrayList<String>();
		im.setDisplayName(ChatColor.RED + "Bomb");
		arr.add(ChatColor.GRAY + "GEAR");
		arr.add(ChatColor.BLUE + "Hold + Crouch to plant");
		im.setLore(arr);
		itm.setItemMeta(im);
		ret.setItemStack(itm);
		return ret;
	}

	private void removeMultiplePlayers() {
		for (int i = this.players.size() - 1; i >= 0; i--) {
			int amt = getAmountOfPlayer(((CSPlayer)this.players.get(i)).getPlayer().getName());
			if (amt > 1)
				this.players.remove(i);
		}
	}

	private int getAmountOfPlayer(String string)
	{
		int ret = 0;
		for (int i = 0; i < this.players.size(); i++) {
			if (((CSPlayer)this.players.get(i)).getPlayer().getName().equals(string))
				ret++;
		}
		return ret;
	}

	public void start() {
		this.setRunning(true);
		this.canPlay = true;

		MCCS.getCSPlugin().npcRegistry.deregisterAll();

		this.timerMax = 30;
		if (!this.gameType.equals(GameType.LOBBY)) {
			this.timerMax = 180;
		} else {
			this.plays = 1;
			chooseNextMap();
			for (int i = 0; i < this.players.size(); i++) {
				((CSPlayer)this.players.get(i)).init();
			}
		}
		try
		{
			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "weather");
			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "day");
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < this.players.size(); i++) {
			Inventory inv = getBuyInventory((CSPlayer)this.players.get(i));
			((CSPlayer)this.players.get(i)).buyInventory = inv;
			((CSPlayer)this.players.get(i)).reloadGuns();
			if (!((CSPlayer)this.players.get(i)).getPlayer().isDead()) {
				((CSPlayer)this.players.get(i)).getPlayer().setHealth(20);
			}
		}

		this.timer = this.timerMax;
		removeMultiplePlayers();
	}

	public void restart() {
		if (this.plays == this.maxPlays / 2 + 1) { // HALF TIME
			int t = this.winsTerrorist;
			this.winsTerrorist = this.winsCounterTerrorist;
			this.winsCounterTerrorist = t;
			for (int i = 0; i < this.players.size(); i++) {
				Teams team = Teams.COUNTER_TERRORIST;
				if (((CSPlayer)this.players.get(i)).getTeam().equals(Teams.COUNTER_TERRORIST))
					team = Teams.TERRORIST;
				((CSPlayer)this.players.get(i)).setTeam(team);
				((CSPlayer)this.players.get(i)).clearChat();
				((CSPlayer)this.players.get(i)).setMoney(1000);
				((CSPlayer)this.players.get(i)).loseStreak = 0;
				clearPlayerInventory();
				((CSPlayer)this.players.get(i)).getPlayer().sendMessage("HALF TIME! " + ChatColor.GRAY + "teams switching");
			}
		}
		
		if (plays == 1) { // Start of arena
			paused = false;
			for (int i = 0; i < this.players.size(); i++) {
				if (knifeRound) {
					players.get(i).setMoney(0);
				}else{
					players.get(i).setMoney(1000);	
				}
				players.get(i).clear();
				players.get(i).setKills(0);
				players.get(i).setDeaths(0);
			}
			
			CSArena lobby = MCCS.getCSPlugin().getArenaByType("lobby");
			if (lobby != null) {
				lobby.mergePlayers(this, false, "START!");
			}
			
			if (knifeRound) {
				for (int i = 0; i < this.players.size(); i++) {
					players.get(i).forceTeamTo = players.get(i).getTeam();
				}
			}
		}

		start();

		setSong(this.buySong);

		this.ticksStart = 0;
		for (int i = 0; i < this.players.size(); i++) {
			InventoryHelper.removeItem(((CSPlayer)this.players.get(i)).getPlayer().getInventory(), Material.MAGMA_CREAM.getId(), (byte)-1, 99);
			((CSPlayer)this.players.get(i)).getPlayer().setItemOnCursor(null);
			int rx = MCCS.getCSPlugin().getRandom().nextInt(5) - 2;
			int rz = MCCS.getCSPlugin().getRandom().nextInt(5) - 2;
			if (((CSPlayer)this.players.get(i)).getTeam().equals(Teams.COUNTER_TERRORIST))
				((CSPlayer)this.players.get(i)).getPlayer().teleport(getSpawn((CSPlayer)this.players.get(i)).getLocation().add(rx, 0.0D, rz));
			else {
				((CSPlayer)this.players.get(i)).getPlayer().teleport(((CSSpawn)this.spawns.get(0)).getLocation().add(rx, 0.0D, rz));
			}

			((CSPlayer)this.players.get(i)).start();
			((CSPlayer)this.players.get(i)).setSpawnLoc(((CSPlayer)this.players.get(i)).getPlayer().getLocation());
			((CSPlayer)this.players.get(i)).setCanMove(false);
			((CSPlayer)this.players.get(i)).setLastSpawnTicks(0);
		}
	}

	private void balanceTeams(boolean ignoreProperTeam) {
		int amtCT = getPlayersOnTeam(Teams.COUNTER_TERRORIST).size();
		int amtTE = getPlayersOnTeam(Teams.TERRORIST).size();
		Teams switchTo = Teams.TERRORIST;
		int switched = 0;
		int amtSwitch = 0;
		if (amtCT > amtTE + 1) {
			switchTo = Teams.TERRORIST;
			amtSwitch = (int)Math.floor((amtCT - amtTE) / 2.0D);
		} else if (amtTE > amtCT + 1) {
			switchTo = Teams.COUNTER_TERRORIST;
			amtSwitch = (int)Math.floor((amtTE - amtCT) / 2.0D);
		}
		for (int i = 0; i < this.players.size(); i++) {
			if ((amtSwitch > 0) && (switched < amtSwitch) && 
					(!((CSPlayer)this.players.get(i)).getTeam().equals(switchTo)) && (
							(((CSPlayer)this.players.get(i)).getTeamToReturnTo() == null) || (ignoreProperTeam))) {
				((CSPlayer)this.players.get(i)).setTeam(switchTo);
				switched++;
			}

			((CSPlayer)this.players.get(i)).setTeamReturnTo(null);
		}
	}

	public void setSong(Song song) {
		this.currentSong = song;
		this.currentSong.reset();
	}

	public void chooseNextMap() {
		this.nextMap = MCCS.getCSPlugin().getRandomMapExcludingType(GameType.LOBBY);
	}

	public void announceMap() {
		if (this.nextMap == null)
			return;
		String mapName = this.nextMap.getName();
		String mapType = this.nextMap.getGameType().toString().toLowerCase().replace("_", " ");
		if (!this.nextMap.getGameModeModifier().equals(GameModeModifier.NONE)) {
			mapType = this.nextMap.getGameModeModifier().toString().toLowerCase().replace("_", " ");
		}
		broadcastMessage("NEXT MAP: " + ChatColor.RED + mapName + ChatColor.WHITE + "  MODE: " + ChatColor.RED + mapType);
	}

	public CSArena getNextMap() {
		return this.nextMap;
	}

	public int getWoolColor(CSPlayer player) {
		return -1;
	}

	public int getTimeLeft() {
		return this.timer;
	}

	public int getMaxTime() {
		return this.timerMax;
	}

	public CSSpawn getSpawn(CSPlayer check) {
		CSSpawn curspawn = null;
		if (this.spawns.size() == 1) {
			return (CSSpawn)this.spawns.get(0);
		}
		if (this.gameType.equals(GameType.TDM)) {
			if (check.getTeam().equals(Teams.TERRORIST)) {
				return (CSSpawn)this.spawns.get(0);
			}
			return (CSSpawn)this.spawns.get(1);
		}

		if (this.gameType.equals(GameType.FFA)) {
			int farthest = 0;
			for (int i = 0; i < this.spawns.size(); i++) {
				CSSpawn spawn = (CSSpawn)this.spawns.get(i);
				int nearest = 9999999;
				for (int ii = this.players.size(); ii >= 0; ii--) {
					CSPlayer player = (CSPlayer)this.players.get(ii);
					if (!player.equals(check)) {
						double distance = Util.point_distance(spawn.getLocation(), player.getPlayer().getLocation());
						if (distance < nearest) {
							nearest = (int)distance;
						}
					}
				}

				if (nearest > farthest) {
					farthest = nearest;
					curspawn = spawn;
				}
			}
		} else {
			curspawn = (CSSpawn)this.spawns.get(0);
		}

		return curspawn;
	}

	public CSPlayer getPlayer(Player player) {
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if (((CSPlayer)this.players.get(i)).getPlayer().equals(player)) {
				return (CSPlayer)this.players.get(i);
			}
		}
		return null;
	}

	public void onDamage(EntityDamageByEntityEvent event, CSPlayer attacked, CSPlayer damager) {
		if (this.gameType.equals(GameType.LOBBY)) {
			event.setCancelled(true);
		}
		if ((!this.gameType.equals(GameType.FFA)) && 
				(attacked.getTeam().equals(damager.getTeam()))) {
			event.setCancelled(true);
		}
		
		if (this.paused) {
			event.setCancelled(true);
		}

		if (!event.isCancelled()) {
			boolean hasKevlar = attacked.getPlayer().getInventory().getChestplate() != null;
			boolean hasHelm = attacked.getPlayer().getInventory().getHelmet() != null;
			if (hasKevlar) {
				event.setDamage((int)Math.ceil(event.getDamage() * 0.7D));
			}
			if (hasHelm)
				event.setDamage((int)Math.ceil(event.getDamage() * 0.9D));
			attacked.onDamage(damager.getPlayer());
		}
	}

	public void onKill(CSPlayer cskiller, CSPlayer cskilled) {
		cskiller.onKill(cskilled);
		if (this.gameModifier.equals(GameModeModifier.ARMS_RACE))
			cskiller.rankUpGun();
	}

	public void onDeath(CSPlayer cskilled) {
		cskilled.onDie();
		changeArena(cskilled, MCCS.getCSPlugin().getArenaByType("lobby"), ChatColor.RED + "YOU ARE OUT");
	}

	public void join(CSPlayer player) {
		this.players.add(player);
		player.setTeam(getTeam(player.forceTeamTo));
		player.setArena(this);
		player.spawn();
		
		if (MCCS.getCSPlugin().autoBalance && !this.gameType.equals(GameType.LOBBY))
			player.forceTeamTo = null;

		Inventory inv = getBuyInventory(player);
		player.buyInventory = inv;

		removeMultiplePlayers();
	}

	public void leave(CSPlayer pl) {
		if (!pl.getPlayer().isDead()) {
			int amtBomb = InventoryHelper.amtItem(pl.getPlayer().getInventory(), Material.MAGMA_CREAM.getId(), (byte)-1);
			if (amtBomb >= 1) {
				dropBomb(pl.getPlayer().getLocation().add(0.0D, 1.0D, 0.0D));
			}
		}
		for (int i = this.players.size() - 1; i >= 0; i--)
			if (((CSPlayer)this.players.get(i)).getPlayer().getName().equals(pl.getPlayer().getName()))
				this.players.remove(i);
	}

	public String getName()
	{
		return this.name;
	}

	private void loadArena() {
		String path = MCCS.getCSPlugin().getDataFolder().getAbsolutePath() + "/arenas/" + this.name;
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try {
			fstream = new FileInputStream(path);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			Location loc1 = getLocationFromString(br.readLine());
			Location loc2 = getLocationFromString(br.readLine());
			this.field.setParam(loc1.getX(), loc1.getZ(), loc2.getX(), loc2.getZ());
		} catch (Exception e) {
			System.err.print("ERROR READING KITPVP ARENA");
			e.printStackTrace();
		}
		loadConfig(br);
		try { br.close(); } catch (Exception localException1) {
		}try { in.close(); } catch (Exception localException2) {
		}try { fstream.close(); } catch (Exception localException3) {
		}
	}

	private void loadConfig(BufferedReader br) { ArrayList<String> file = new ArrayList<String>();
	try
	{
		String str = br.readLine();
		if ((str != null) && 
				(str.equals("--config--")))
		{
			String strLine;
			while ((strLine = br.readLine()) != null) {
				file.add(strLine);
			}
			for (int i = 0; i < file.size(); i++)
				computeConfigData((String)file.get(i));
		}
	}
	catch (IOException e)
	{
		e.printStackTrace();
	} }

	private void computeConfigData(String str)
	{
		if (str.indexOf("=") > 0) {
			String str2 = str.substring(0, str.indexOf("="));
			if (str2.equalsIgnoreCase("type")) {
				String check = str.substring(str.indexOf("=") + 1).toUpperCase();
				if (check.length() > 1) {
					this.gameType = GameType.valueOf(check);
				}
			}
			if (str2.equalsIgnoreCase("modifier")) {
				String check = str.substring(str.indexOf("=") + 1).toUpperCase();
				if (check.length() > 1) {
					this.gameModifier = GameModeModifier.valueOf(check);
				}
			}
			if (str2.equalsIgnoreCase("addspawn")) {
				Location spawnloc = getLocationFromString(str.substring(str.indexOf("=") + 1));
				if (spawnloc != null) {
					CSSpawn ks = new CSSpawn(spawnloc);
					this.spawns.add(ks);
				}
			}
		}
	}

	private Location getLocationFromString(String str) {
		String[] arr = str.split(",");
		if (arr.length == 2)
			return new Location((World)MCCS.getCSPlugin().getServer().getWorlds().get(0), Integer.parseInt(arr[0]), 0.0D, Integer.parseInt(arr[1]));
		if (arr.length == 3) {
			return new Location((World)MCCS.getCSPlugin().getServer().getWorlds().get(0), Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
		}
		return null;
	}

	public GameType getGameType() {
		return this.gameType;
	}

	public GameModeModifier getGameModeModifier() {
		return this.gameModifier;
	}

	public Teams getTeam(Teams forceTeamTo) {
		if (this.gameType.equals(GameType.LOBBY)) {
			return Teams.NEUTRAL;
		}
		if (this.gameType.equals(GameType.TDM)) {
			
			if (forceTeamTo != null) {
				return forceTeamTo;
			} else {
				int amtTerr = getPlayersOnTeam(Teams.TERRORIST).size();
				int amtCTer = getPlayersOnTeam(Teams.COUNTER_TERRORIST).size();
				if (amtTerr > amtCTer)
					return Teams.COUNTER_TERRORIST;
			}
			
			return Teams.TERRORIST;
		}

		return Teams.FFA;
	}

	public ArrayList<CSPlayer> getPlayersOnTeam(Teams team) {
		ArrayList<CSPlayer> ret = new ArrayList<CSPlayer>();
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if (((CSPlayer)this.players.get(i)).getTeam().equals(team)) {
				ret.add((CSPlayer)this.players.get(i));
			}
		}
		return ret;
	}

	public Inventory getBuyInventory(CSPlayer csplayer) {
		Inventory retInv = Bukkit.createInventory(csplayer.getPlayer(), 54, "Buy Menu");
		ArrayList<CSItem> pistols = MCCS.getCSPlugin().getItemsByType("gun_pistol");
		ArrayList<CSItem> heavy = MCCS.getCSPlugin().getItemsByType("gun_heavy");
		ArrayList<CSItem> smg = MCCS.getCSPlugin().getItemsByType("gun_smg");
		ArrayList<CSItem> rifle = MCCS.getCSPlugin().getItemsByType("gun_rifle");
		ArrayList<CSItem> gear = MCCS.getCSPlugin().getItemsByType("gear");

		for (int i = rifle.size() - 1; i >= 0; i--) {
			CSItem csitem = (CSItem)rifle.get(i);
			if ((csplayer.getTeam().equals(Teams.TERRORIST)) && (csitem.getItemStack().getType().equals(Material.STONE_HOE))) {
				rifle.remove(i);
			}
			if ((csplayer.getTeam().equals(Teams.COUNTER_TERRORIST)) && (csitem.getItemStack().getType().equals(Material.BONE))) {
				rifle.remove(i);
			}
		}

		for (int i = gear.size() - 1; i >= 0; i--) {
			boolean hasThisItem = false;
			int amt = InventoryHelper.amtItem(csplayer.getPlayer().getInventory(), ((CSItem)gear.get(i)).getItemStack().getTypeId(), (byte)-1);
			if (amt > 0)
				hasThisItem = true;
			if (hasThisItem) {
				gear.remove(i);
			}
		}

		int offset = 0;
		addItemsToBuyMenu(offset, pistols, csplayer, retInv); offset += 2;
		addItemsToBuyMenu(offset, heavy, csplayer, retInv); offset += 2;
		addItemsToBuyMenu(offset, smg, csplayer, retInv); offset += 2;
		addItemsToBuyMenu(offset, rifle, csplayer, retInv); offset += 2;
		addItemsToBuyMenu(offset, gear, csplayer, retInv); offset += 2;
		return retInv;
	}
	
	

	public void addItemsToBuyMenu(int offset, ArrayList<CSItem> items, CSPlayer player, Inventory inv) {
		for (int a = 0; a < 8; a++) {
			for (int i = 0; i < items.size() - 1; i++) {
				CSItem current = (CSItem)items.get(i);
				CSItem next = (CSItem)items.get(i + 1);
				if (next.getCost() < current.getCost()) {
					items.set(i, next);
					items.set(i + 1, current);
				}
			}
		}
		for (int i = 0; i < items.size(); i++)
		{
			ItemStack set = ((CSItem)items.get(i)).getItemStack();
			set.setAmount(1);
			inv.setItem(offset + i * 9, set);
		}
	}

	public void openBuyMenu(CSPlayer csplayer) {
		if ((csplayer.getPlayer() != null) && (!this.gameModifier.equals(GameModeModifier.ARMS_RACE)) && (!csplayer.getPlayer().isDead()))
			if (this.ticksStart < 800) {
				if (csplayer.buyInventory != null) {
					csplayer.getPlayer().openInventory(csplayer.buyInventory);
					csplayer.setInBuyMenu(true);
				}
			} else {
				csplayer.getPlayer().sendMessage("The buy-time is over!");
			}
	}

	public ArrayList<CSPlayer> getActivePlayers()
	{
		ArrayList<CSPlayer> ret = new ArrayList<CSPlayer>();
		for (int i = 0; i < this.players.size(); i++) {
			ret.add((CSPlayer)this.players.get(i));
		}
		return ret;
	}

	public int getRound() {
		return this.plays;
	}

	public int getMaxRound() {
		return this.maxPlays;
	}

	public void setNextMap(CSArena newarr) {
		this.nextMap = newarr;
	}

	public List<CSSpawn> getSpawns() {
		return this.spawns;
	}

	public CSHostage getHostage(Player attacked) {
		for (int i = 0; i < this.hostages.size(); i++) {
			try {
				if (((CSHostage)this.hostages.get(i)).getNPC().getBukkitEntity().getEntityId() == attacked.getEntityId())
					return (CSHostage)this.hostages.get(i);
			}
			catch (Exception localException)
			{
			}
		}
		return null;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public static enum GameModeModifier
	{
		NONE, DEMOLITION, HOSTAGE_RESCUE, ARMS_RACE;
	}

	public static enum GameType {
		TDM, FFA, LOBBY;
	}

	public static enum Teams {
		TERRORIST, COUNTER_TERRORIST, FFA, NEUTRAL;
	}

	public void setPlays(int i) {
		this.plays = i;
	}
}