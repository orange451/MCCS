package com.orange451.mccs;

import com.orange451.SpawnHuman.NPCManager;
import com.orange451.mccs.item.CSItem;
import com.orange451.mccs.item.ItemLoader;
import com.orange451.mccs.listeners.PluginBlockListener;
import com.orange451.mccs.listeners.PluginEntityListener;
import com.orange451.mccs.listeners.PluginPlayerListener;
import java.io.File;
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
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class MCCS extends JavaPlugin
{
	private static MCCS plugin;
	private ArrayList<CSArena> arenas;
	private ArrayList<CSItem> items;
	private Random random;
	private int timer;
	private int ticks;
	private int secondsAlive;
	public NPCManager npcRegistry;
	public ArrayList<Location> glassThinReplace = new ArrayList();
	public Location bombLoc;
	public List<KitArenaCreator> creatingArena = new ArrayList();
	public ArrayList<CSGun> gunsOnGround = new ArrayList<CSGun>();
	public boolean autoBalance = true;

	public void onEnable()
	{
		System.out.println("[MCCS] Enabling");
		this.arenas = new ArrayList();
		this.items = new ArrayList();
		this.timer = getServer().getScheduler().scheduleSyncRepeatingTask(this, new ArenaUpdater(), 20L, 1L);

		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new PluginEntityListener(this), this);
		pm.registerEvents(new PluginPlayerListener(this), this);
		pm.registerEvents(new PluginBlockListener(this), this);

		this.npcRegistry = new NPCManager(this);

		plugin = this;
		Util.Initialize(this);
		this.random = new Random();

		loadArenas();
		new ItemLoader();

		CSArena arena = getArenaByType("lobby");
		if (arena != null) {
			arena.start();
		}

		this.ticks = 0;
	}

	public void onDisable()
	{
		System.out.println("[MCCS] Disabling");
		getServer().getScheduler().cancelTask(this.timer);
		this.npcRegistry.deregisterAll();
		replaceGlass();
	}

	public void loadArena(String name) {
		CSArena ka = new CSArena(name);
		this.arenas.add(ka);
		System.out.println("[MINESTRIKE] LOADED ARENA " + name);
	}

	public void loadArenas() {
		String path = getDataFolder().getAbsolutePath() + "/arenas";
		File dir = new File(path);
		String[] children = dir.list();
		if (children != null)
			for (int i = 0; i < children.length; i++) {
				String filename = children[i];
				loadArena(filename);
			}
	}

	public void replaceGlass()
	{
		for (int i = 0; i < this.glassThinReplace.size(); i++) {
			((World)Bukkit.getWorlds().get(0)).getBlockAt((Location)this.glassThinReplace.get(i)).setType(Material.THIN_GLASS);
		}
		this.glassThinReplace.clear();
	}

	public static MCCS getCSPlugin() {
		return plugin;
	}

	public CSPlayer getCSPlayer(Player player) {
		for (int i = this.arenas.size() - 1; i >= 0; i--) {
			CSPlayer ret = ((CSArena)this.arenas.get(i)).getPlayer(player);
			if (ret != null) {
				return ret;
			}
		}
		return null;
	}

	public CSArena getArena(String name) {
		for (int i = this.arenas.size() - 1; i >= 0; i--) {
			CSArena arena = (CSArena)this.arenas.get(i);
			if (arena.getName().equals(name)) {
				return arena;
			}
		}
		return null;
	}
	
	public ArrayList<CSArena> getArenas() {
		return this.arenas;
	}

	public CSArena getArenaByType(String name) {
		for (int i = this.arenas.size() - 1; i >= 0; i--) {
			CSArena arena = (CSArena)this.arenas.get(i);
			if (arena.getGameType().toString().toLowerCase().equals(name.toLowerCase())) {
				return arena;
			}
		}
		return null;
	}

	public void onJoin(Player player) {
		CSArena lobby = getArenaByType("lobby");
		if (lobby != null) {
			CSPlayer pl = new CSPlayer(player);
			pl.init();
			lobby.join(pl);
			pl.spawn();
		}
	}

	public void onLeave(Player player) {
		stopMakingArena(player);
		CSPlayer pl = getCSPlayer(player);
		if (pl != null) {
			pl.getArena().leave(pl);
			player.teleport(pl.getReturnLocation());
			player.getInventory().clear();
			player.setPlayerListName(player.getName());
		}
		player.setWalkSpeed(0.2F);
	}

	public void sendMessage(Player to, Player from, String string) {
		if (to == null) {
			ArrayList csplayers = getCSPlayers();
			for (int i = 0; i < csplayers.size(); i++)
				((CSPlayer)csplayers.get(i)).sendMessage(from, string);
		}
		else {
			CSPlayer pl = getCSPlayer(to);
			if (pl != null)
				pl.sendMessage(from, string);
			else
				to.sendMessage(string);
		}
	}

	public void sendMessage(Player to, String string)
	{
		sendMessage(to, null, string);
	}

	public void stopMakingArena(Player player) {
		for (int i = this.creatingArena.size() - 1; i >= 0; i--)
			if (((KitArenaCreator)this.creatingArena.get(i)).player.getName().equals(player.getName()))
				this.creatingArena.remove(i);
	}

	public KitArenaCreator getKitArenaCreator(Player player)
	{
		for (int i = this.creatingArena.size() - 1; i >= 0; i--) {
			if (((KitArenaCreator)this.creatingArena.get(i)).player.getName().equals(player.getName())) {
				return (KitArenaCreator)this.creatingArena.get(i);
			}
		}
		return null;
	}

	public CSArena getRandomMapExcludingType(CSArena.GameType lobby) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < this.arenas.size(); i++) {
			if (!((CSArena)this.arenas.get(i)).getGameType().equals(lobby)) {
				list.add((CSArena)this.arenas.get(i));
			}
		}
		if (list.size() > 0) {
			int pick = this.random.nextInt(list.size());
			CSArena ret = (CSArena)list.get(pick);
			list.clear();
			return ret;
		}
		return null;
	}

	public Random getRandom() {
		return this.random;
	}

	public void addItem(CSItem item) {
		this.items.add(item);
	}

	public ArrayList<CSItem> getItemsByType(String type) {
		ArrayList ret = new ArrayList();
		for (int i = 0; i < this.items.size(); i++) {
			if (((CSItem)this.items.get(i)).getType().toLowerCase().equals(type.toLowerCase())) {
				ret.add((CSItem)this.items.get(i));
			}
		}
		return ret;
	}

	public CSItem getItem(ItemStack check) {
		if (check != null) {
			for (int i = 0; i < this.items.size(); i++) {
				String compare = ((CSItem)this.items.get(i)).getDisplayName();
				if ((check.getItemMeta() != null) && (check.getItemMeta().getDisplayName() != null) && (check.getItemMeta().getDisplayName().equals(compare))) {
					return (CSItem)this.items.get(i);
				}
			}
		}

		return null;
	}
	
	public CSItem getCSItemFromItemStack(ItemStack check) {
		if (check != null) {
			for (int i = 0; i < this.items.size(); i++) {
				if (items.get(i).getItemStack().getTypeId() == check.getTypeId()) {
					return (CSItem)this.items.get(i);
				}
				/*String compare = ((CSItem)this.items.get(i)).getDisplayName();
				if ((check.getItemMeta() != null) && (check.getItemMeta().getDisplayName() != null) && (check.getItemMeta().getDisplayName().equals(compare))) {
					return (CSItem)this.items.get(i);
				}*/
			}
		}

		return null;
	}

	public CSItem getItem(String name) {
		for (int i = 0; i < this.items.size(); i++) {
			if (((CSItem)this.items.get(i)).getName().equals(name)) {
				return (CSItem)this.items.get(i);
			}
		}
		return null;
	}

	public void openBuyMenu(HumanEntity player) {
		CSPlayer csplayer = getCSPlayer((Player)player);
		if (csplayer != null)
			csplayer.getArena().openBuyMenu(csplayer);
	}

	public ArrayList<Player> getPlayers()
	{
		ArrayList ret = new ArrayList();
		for (int i = 0; i < this.arenas.size(); i++) {
			ArrayList csplayers = ((CSArena)this.arenas.get(i)).getActivePlayers();
			for (int ii = 0; ii < csplayers.size(); ii++) {
				ret.add(((CSPlayer)csplayers.get(ii)).getPlayer());
			}
		}
		return ret;
	}

	public ArrayList<CSPlayer> getCSPlayers() {
		ArrayList ret = new ArrayList();
		for (int i = 0; i < this.arenas.size(); i++) {
			ArrayList csplayers = ((CSArena)this.arenas.get(i)).getActivePlayers();
			for (int ii = 0; ii < csplayers.size(); ii++) {
				ret.add((CSPlayer)csplayers.get(ii));
			}
		}
		return ret;
	}

	public void sendNonCSMessage(Player player, String message) {
		Player[] players = Bukkit.getOnlinePlayers();
		for (int i = 0; i < players.length; i++) {
			CSPlayer csplayer = getCSPlayer(players[i]);
			if (csplayer == null)
				players[i].sendMessage("<" + player.getName() + "> " + message);
		}
	}

	public int getServerNumber()
	{
		int servernum = 1;
		try {
			String motd = Bukkit.getServer().getMotd();
			boolean has = motd.contains("#");
			if (has) {
				int ind = motd.indexOf("#");
				String t = motd.substring(ind + 1, ind + 3);
				if (t.contains(" "))
					t = t.replaceAll(" ", "");
				servernum = Integer.parseInt(t);
			}
		} catch (Exception localException) {
		}
		return servernum;
	}

	public class ArenaUpdater
	implements Runnable
	{
		public ArenaUpdater()
		{
		}

		public void run()
		{
			MCCS.this.ticks += 1;
			if (MCCS.this.ticks == 80) {
				Player[] players = Bukkit.getOnlinePlayers();
				for (int i = 0; i < players.length; i++) {
					MCCS.this.onJoin(players[i]);
				}
			}
			for (int i = 0; i < MCCS.this.arenas.size(); i++) {
				((CSArena)MCCS.this.arenas.get(i)).tick();
			}
			if (MCCS.this.ticks % 20 == 0) {
				MCCS.this.secondsAlive += 1;
				int restartTime = 21600;
				Player[] players = Bukkit.getOnlinePlayers();
				for (int i = 0; i < players.length; i++) {
					players[i].setFoodLevel(3);
				}

				if (MCCS.this.secondsAlive == restartTime) {
					Player[] pl = Bukkit.getOnlinePlayers();
					for (int i = pl.length - 1; i >= 0; i--) {
						pl[i].kickPlayer(ChatColor.LIGHT_PURPLE + "Server is Restarting! \n " + ChatColor.BLUE + "Reconnect in 10 seconds");
					}
					Bukkit.getServer().shutdown();
				}
			}
		}
	}

	public static void broadcastMessage(String string) {
		Player[] plyrs = Bukkit.getOnlinePlayers();
		for (int i = 0; i < plyrs.length; i++) {
			CSPlayer csplyr = plugin.getCSPlayer(plyrs[i]);
			if (csplyr != null) {
				csplyr.sendMessage(null, string);
			} else {
				plyrs[i].sendMessage(string);
			}
		}
	}
}