package com.orange451.mccs;

import com.orange451.mccs.CSArena.Teams;
import com.orange451.mccs.item.CSItem;
import com.orange451.mccs.item.InventoryHelper;
import com.orange451.opex.permissions.PermissionInterface;
import com.orange451.pvpgunplus.PVPGunPlus;
import com.orange451.pvpgunplus.gun.Gun;
import com.orange451.pvpgunplus.gun.GunPlayer;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;
import org.kitteh.tag.TagAPI;

public class CSPlayer
{
	private boolean canMove = true;
	private boolean isInBuyMenu;
	private int lastSpawnTicks = 20;
	private int money = 1000;
	private int gunRank = 0;
	private int aliveTicks;
	private int deaths;
	private int kills;
	private int ticksDead;
	private Entity lastAttacker;
	private ItemStack secondary;
	private ItemStack primary;
	private Location returnTo;
	private Location spawnLoc;
	private Location lastLoc;
	private Player player;
	private String[] chat = new String[7];
	private SpecialMessage specialMessage;
	private CSArena arena;
	private CSArena.Teams team;
	private CSArena.Teams teamReturnTo;
	public Inventory buyInventory;
	public int loseStreak;
	public Teams forceTeamTo;
	
	private Scoreboard board;
	private Objective scoreboard;
	
	public CSPlayer(Player player) {
		this.player = player;
		this.player.getInventory().clear();
		this.returnTo = player.getLocation();
		for (int i = 0; i < this.chat.length; i++)
			this.chat[i] = "";
		
		scoreboard_init("scoreboard");
	}

	public CSPlayer(CSArena arena, Player player) {
		this(player);
		this.arena = arena;
		this.team = arena.getTeam(forceTeamTo);
	}

	public void setArena(CSArena arena) {
		this.arena = arena;
	}
	
	public void scoreboard_init(String name) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		this.board = manager.getNewScoreboard();

		this.scoreboard = this.board.registerNewObjective(name, "dummy");
		this.scoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);

		this.player.setScoreboard(this.board);
	}
	
	public String getMaxString(String string) {
		if (string.length() > 16) {
			return string.substring(0, 16);
		}
		return string;
	}
	
	public void scoreboard_update() {
		String scorename = "" + ChatColor.BLUE + ChatColor.BOLD + "Counter-T";
		if (this.team.equals(CSArena.Teams.TERRORIST))
			scorename = "" + ChatColor.RED + ChatColor.BOLD + "Terrorist";
		
		scoreboard_init(scorename);
		this.scoreboard.setDisplayName(scorename);
		
		// SCOREBOARD
		Score mapScore = this.scoreboard.getScore(Bukkit.getOfflinePlayer(getMaxString("Map: " + this.arena.getName().replace("de_",""))));
		mapScore.setScore(11);
		
		Score modeScore = this.scoreboard.getScore(Bukkit.getOfflinePlayer(getMaxString("Mode: " + arena.getGameModeModifier().toString().toLowerCase().replace("_", " "))));
		modeScore.setScore(10);
		
		Score blankScore02 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + " "));
		blankScore02.setScore(9);
		
		Score killScore = this.scoreboard.getScore(Bukkit.getOfflinePlayer("" + kills + ChatColor.GREEN + " Kills"));
		killScore.setScore(8);
		
		Score deathScore = this.scoreboard.getScore(Bukkit.getOfflinePlayer("" + deaths + ChatColor.RED + " Deaths"));
		deathScore.setScore(7);
		
		Score blankScore01 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + " "));
		blankScore01.setScore(6);
		
		int wins = this.arena.winsCounterTerrorist;
		int losses = this.arena.winsTerrorist;
		if (this.team.equals(CSArena.Teams.TERRORIST)) {
			wins = this.arena.winsTerrorist;
			losses = this.arena.winsCounterTerrorist;
		}
		
		Score winScore = this.scoreboard.getScore(Bukkit.getOfflinePlayer("" + wins + ChatColor.GREEN + " Wins"));
		winScore.setScore(5);
		
		Score lossScore = this.scoreboard.getScore(Bukkit.getOfflinePlayer("" + losses + ChatColor.RED + " Losses"));
		lossScore.setScore(4);
		
		Score blankScore03 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + " "));
		blankScore03.setScore(3);
		
		Score roundScore = this.scoreboard.getScore(Bukkit.getOfflinePlayer(getMaxString(ChatColor.GOLD + "Round: " + ChatColor.WHITE + this.arena.getRound() + "/" + this.arena.getMaxRound())));
		roundScore.setScore(2);
		
		//Score timeScore = this.scoreboard.getScore(Bukkit.getOfflinePlayer(getMaxString(ChatColor.GOLD + "Time: " + ChatColor.WHITE + this.arena.getTimeLeft() + "s")));
		//timeScore.setScore(1);
		
		this.scoreboard.setDisplayName(scorename);
	}

	public Player getPlayer() {
		return this.player;
	}

	public void init() {
		clear();
		setMoney(1000);
		this.kills = 0;
		this.deaths = 0;
		this.lastAttacker = null;
		this.teamReturnTo = null;
	}

	public void clear() {
		this.gunRank = 0;
		this.primary = null;
		this.secondary = null;
		if (arena != null) {
			if (!arena.knifeRound) {
				String gun = "P2000";
				//if (PermissionInterface.hasPermission(this.player, "minestrike.mvp"))
					//gun = "Beratta";
				this.secondary = MCCS.getCSPlugin().getItem(gun).getItemStack();
				this.secondary.setAmount(1);
			}
		}
		getPlayer().getInventory().setHelmet(null);
		getPlayer().getInventory().setChestplate(null);
		getPlayer().getInventory().setLeggings(null);
		getPlayer().getInventory().setBoots(null);
		getPlayer().getInventory().clear();
	}

	public void reloadGuns() {
		Plugin temp = Bukkit.getPluginManager().getPlugin("PVPGunPlus");
		if (temp != null) {
			PVPGunPlus pvpgun = (PVPGunPlus)temp;
			GunPlayer gp = pvpgun.getGunPlayer(this.player);
			if (gp != null)
				gp.reloadAllGuns();
		}
	}

	public void start() {
		applyNewGun(this.primary);
		applyNewGun(this.secondary);
		InventoryHelper.removeItem(this.player.getInventory(), Material.IRON_SWORD.getId(), (byte)-1, 64);
		this.player.getInventory().setItem(2, new ItemStack(Material.IRON_SWORD, 1));

		getPlayer().setFireTicks(0);

		InventoryHelper.removeItem(this.player.getInventory(), Material.MAGMA_CREAM.getId(), (byte)-1, 99);
		this.player.setItemOnCursor(null);

		reloadGuns();
	}

	public void tick() {
		if (this.player == null)
			return;
		if (!this.player.isOnline())
			return;
		this.aliveTicks += 1;
		this.lastSpawnTicks += 1;

		if (this.lastSpawnTicks == 4) {
			spawn();
		}
		if (MCCS.getCSPlugin().bombLoc != null) {
			this.player.setCompassTarget(MCCS.getCSPlugin().bombLoc);
		}
		float amt = this.arena.getTimeLeft() / (float)this.arena.getMaxTime();
		if (amt > 1.0F) {
			amt = 1.0F;
		}
		if (this.lastLoc == null) {
			this.lastLoc = this.player.getLocation();
		}
		if (this.player.isDead()) {
			this.ticksDead += 1;
		}

		if (this.ticksDead > 320) {
			this.player.kickPlayer(ChatColor.RED + "KICKED FOR AFK");
		}
		
		if (aliveTicks % 20 == 0)
			scoreboard_update();
		
		// TEMP /////////////////////////
		this.player.setSneaking(true); //
		/////////////////////////////////
		
		this.player.setLevel(this.money);
		this.player.setExp(amt);
		this.player.setSprinting(false);

		if ((!this.player.isDead()) && (!this.canMove) && (getSpawnLoc() != null)) {
			//this.player.setVelocity(new Vector(0, 0, 0).setY(this.player.getVelocity().getY()));
			double tx = getSpawnLoc().getX();
			double tz = getSpawnLoc().getZ();
			double mx = this.player.getLocation().getX();
			double mz = this.player.getLocation().getZ();

			if ((tx != mx) || (tz != mz)) {
				this.player.teleport(getSpawnLoc());
			}
		}

		if (this.aliveTicks % 20 == 0) {
			//resendChat();
			int amtBomb = InventoryHelper.amtItem(getPlayer().getInventory(), Material.MAGMA_CREAM.getId(), (byte)-1);
			if (amtBomb >= 1) {
				MCCS.getCSPlugin().bombLoc = getPlayer().getLocation();
			}
		}

		if ((this.teamReturnTo != null) && (this.teamReturnTo.equals(CSArena.Teams.NEUTRAL)))
			this.teamReturnTo = null;
	}

	public void onClickItem(InventoryClickEvent event) {
		ItemStack cursor = event.getCurrentItem();
		if (cursor != null && cursor.getType().equals(Material.COMPASS)) {
			event.setCancelled(true);
		}
		if (this.isInBuyMenu) {
			CSItem item = MCCS.getCSPlugin().getItem(cursor);
			if (item != null) {
				if (item.canBuy(this.money)) {
					this.money -= item.getCost();
					buyItem(item);
					event.setCurrentItem(null);
					this.player.playSound(this.player.getLocation(), Sound.ITEM_PICKUP, 1.0F, 1.0F);
				} else {
					event.setCancelled(true);
					this.player.playSound(this.player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
				}
			}
		}
	}

	private void buyItem(CSItem item)
	{
		boolean isSpecial = item.isSpecial();
		if (!isSpecial) {
			this.player.getInventory().addItem(new ItemStack[] { item.getItemStack() });
		} else {
			if (item.getType().contains("gun_")) {
				ItemStack gun = item.getItemStack();
				gun.setAmount(1);
				applyNewGun(gun, item);
			}
			if (item.getType().contains("gear")) {
				ItemStack gear = item.getItemStack();
				if (item.getName().equals("Kevlar Vest")) {
					gear = setColor(gear, Color.fromRGB(Color.BLACK.asRGB()));
					this.player.getInventory().setChestplate(gear);
				}
				if (item.getName().equals("Kevlar Helmet")) {
					gear = setColor(gear, Color.fromRGB(Color.BLACK.asRGB()));
					this.player.getInventory().setHelmet(gear);
				}
			}
		}
	}
	
	public void removeGun(ItemStack gun) {
		//System.out.println("REMOVING " + gun.getItemMeta().getDisplayName() + " / " + gun.getTypeId());
		InventoryHelper.removeItem(this.player.getInventory(), gun.getTypeId(), (byte)-1, 999);
		InventoryHelper.removeItem(this.player.getInventory(), getAmmoTypeId(gun), (byte)-1, 999);
	}
	
	public int getAmountGunAmmo(ItemStack gun) {
		return InventoryHelper.amtItem(this.player.getInventory(), getAmmoTypeId(gun), (byte)-1);
	}

	private void applyNewGun(ItemStack gun, CSItem item) {
		if (item.getType().contains("pistol")) {
			if (this.secondary != null) {
				InventoryHelper.removeItem(this.player.getInventory(), this.secondary.getTypeId(), (byte)-1, 999);
				InventoryHelper.removeItem(this.player.getInventory(), Material.ENDER_PEARL.getId(), (byte)-1, 999);
			}
			this.secondary = gun;
		} else {
			if (this.primary != null) {
				InventoryHelper.removeItem(this.player.getInventory(), this.primary.getTypeId(), (byte)-1, 999);
				InventoryHelper.removeItem(this.player.getInventory(), Material.SEEDS.getId(), (byte)-1, 999);
				InventoryHelper.removeItem(this.player.getInventory(), Material.CLAY_BALL.getId(), (byte)-1, 999);
				InventoryHelper.removeItem(this.player.getInventory(), Material.FLINT.getId(), (byte)-1, 999);
			}
			this.primary = gun;
		}

		giveGun(gun, item);
	}
	
	public ItemStack getPrimary() {
		return this.primary;
	}
	
	public ItemStack getSecondary() {
		return this.secondary;
	}

	public boolean canGiveGun(CSItem item) {
		if (item.getType().contains("pistol") && secondary != null) {
			return false;
		}
		if (!item.getType().contains("pistol") && primary != null) {
			return false;
		}
		return true;
	}

	private void applyNewGun(ItemStack gun) {
		CSItem csitem = MCCS.getCSPlugin().getItem(gun);
		if (csitem != null)
			applyNewGun(gun, csitem);
	}
	
	public int getAmmoTypeId(ItemStack gun) {
		PVPGunPlus pvpgunplus = (PVPGunPlus)Bukkit.getPluginManager().getPlugin("PVPGunPlus");
		return pvpgunplus.getGun(gun.getTypeId()).getAmmoType();
	}

	public void giveGun(ItemStack gun, CSItem item) {
		if (gun == null)
			return;
		try {
			PVPGunPlus pvpgunplus = (PVPGunPlus)Bukkit.getPluginManager().getPlugin("PVPGunPlus");
			if (item.getType().contains("pistol")) {
				System.out.println("GIVING PISTOL");
				this.secondary = gun;
				if (this.player.getInventory().getItem(1) == null)
					this.player.getInventory().setItem(1, gun);
				else
					this.player.getInventory().addItem(new ItemStack[] { gun });
			} else if (item.getType().contains("rifle") || item.getType().contains("smg") || item.getType().contains("heavy")) {
				System.out.println("GIVING RIFLE");
				this.primary = gun;
				if (this.player.getInventory().getItem(0) == null)
					this.player.getInventory().setItem(0, gun);
				else
					this.player.getInventory().addItem(new ItemStack[] { gun });
			} else {
				System.out.println("GIVING ITEM");
				for (int i = 3; i < 20; i++) {
					if (this.player.getInventory().getItem(i) == null) {
						this.player.getInventory().setItem(i, gun);
						break;
					}
				}
			}
			int ammo = pvpgunplus.getGun(gun.getTypeId()).getAmmoType();
			if (ammo > 0) {
				ItemStack gunAmmo = new ItemStack(ammo, item.getAmount());
				boolean found = false;
				for (int i = 10; i < this.player.getInventory().getSize(); i++) {
					if ((!found) && (this.player.getInventory().getItem(i) == null)) {
						found = true;
						this.player.getInventory().setItem(i, gunAmmo);
					}
				}
			}
		}
		catch (Exception localException)
		{
		}
	}

	public void setSpawnLoc(Location loc)
	{
		this.spawnLoc = loc;
	}

	public void setCanMove(boolean b) {
		this.canMove = b;
		if (!this.player.isDead()) {
			//this.player.setWalkSpeed(0.0F);
			if (b)
				this.player.setWalkSpeed(getWalkSpeed());
		}
	}

	public float getWalkSpeed()
	{
		float ret = 0.19F;
		if (this.player != null) {
			ItemStack hold = this.player.getItemInHand();
			if (hold != null) {
				if (hold.toString().toLowerCase().contains("sword"))
					ret += 0.025F;
			}
			else {
				ret += 0.025F;
			}
		}
		return ret;
	}

	public void setLastSpawnTicks(int i) {
		this.lastSpawnTicks = i;
	}

	public void spawn() {
		if (!this.player.isOnline())
			return;
		start();
		ArrayList<CSPlayer> players = this.arena.getActivePlayers();
		
		/*for (int i = 0; i < players.size(); i++) {
			Player forWhom = ((CSPlayer)players.get(i)).getPlayer();
			if (!forWhom.getName().equals(this.player.getName())) {
				TagAPI.refreshPlayer(this.player, forWhom);
			}
		}*/

		giveArmor();

		if (this.lastSpawnTicks > 16) {
			CSSpawn spawn = this.arena.getSpawn(this);
			if (spawn != null) {
				spawn.spawn(this);
			}
		}

		if (!this.player.isDead()) {
			this.ticksDead = 0;
		}

		if (!this.team.equals(CSArena.Teams.TERRORIST)) {
			InventoryHelper.removeItem(this.player.getInventory(), Material.COMPASS.getId(), (byte)-1, 64);
		}
		else if (this.arena.getGameModeModifier().equals(CSArena.GameModeModifier.DEMOLITION))
			this.player.getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
		else
			InventoryHelper.removeItem(this.player.getInventory(), Material.COMPASS.getId(), (byte)-1, 64);
	}

	public void onDamage(Entity attacker)
	{
		this.lastAttacker = attacker;
	}

	public Entity getLastAttacker() {
		return this.lastAttacker;
	}

	private int getWoolColor() {
		int arenacolor = this.arena.getWoolColor(this);
		if (arenacolor == 999)
			return -1;
		if (arenacolor == -1) {
			int ret = 14;
			if (this.team.equals(CSArena.Teams.COUNTER_TERRORIST))
				ret = 11;
			return ret;
		}
		return arenacolor;
	}

	private ItemStack setColor(ItemStack is, Color color) {
		LeatherArmorMeta lam = (LeatherArmorMeta)is.getItemMeta();
		lam.setColor(color);
		is.setItemMeta(lam);
		return is;
	}

	private void giveArmor() {
		int teamcolor = getWoolColor();
		int hexColor = 255;
		if (this.team.equals(CSArena.Teams.TERRORIST)) {
			hexColor = 16711680;
		}
		Color color = Color.fromRGB(hexColor);

		ItemStack c0 = setColor(new ItemStack(Material.LEATHER_BOOTS, 1), color);
		ItemStack c1 = setColor(new ItemStack(Material.LEATHER_LEGGINGS, 1), color);

		if (teamcolor != -1) {
			this.player.getInventory().setBoots(c0);
			this.player.getInventory().setLeggings(c1);
		}

		for (PotionEffect effect : this.player.getActivePotionEffects())
			this.player.removePotionEffect(effect.getType());
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 96000, 0));
	}

	public ChatColor getTeamColor() {
		if (this.team.equals(CSArena.Teams.COUNTER_TERRORIST))
			return ChatColor.BLUE;
		if (this.team.equals(CSArena.Teams.TERRORIST))
			return ChatColor.RED;
		if (this.team.equals(CSArena.Teams.NEUTRAL)) {
			return ChatColor.GRAY;
		}
		return ChatColor.LIGHT_PURPLE;
	}

	public CSArena getArena() {
		return this.arena;
	}

	public CSArena.Teams getTeam() {
		return this.team;
	}

	public int getStringLength(String str) {
		if (str == null) {
			return 0;
		}
		return ChatColor.stripColor(str).length();
	}

	public void sendMessage(Player from, String msg) {
		String start = "";
		if (from != null) {
			CSPlayer csplayer = MCCS.getCSPlugin().getCSPlayer(from);
			if (csplayer != null) {
				start = "<" + csplayer.getSymbol() + csplayer.getTeamColor() + csplayer.getPlayer().getName() + ChatColor.WHITE + "> ";
			}
		}
		
		player.sendMessage(start + msg);
		
		/*int maxLength = 50;
		int extracolorlength = getColorLength(msg);
		ArrayList<String> messages = new ArrayList<String>();
		if (getStringLength(msg) > maxLength) {
			messages.add(msg.substring(0, maxLength + extracolorlength) + "--");
			msg = msg.substring(maxLength + extracolorlength - 1, msg.length());
			if (msg.length() > maxLength - 1)
				messages.add("  " + msg.substring(1, maxLength));
			else
				messages.add("  " + msg.substring(1, msg.length()));
		}
		else {
			messages.add(msg);
		}
		for (int i = 0; i < messages.size(); i++)
			doMessage((String)messages.get(i));*/
	}

	private int getColorLength(String str)
	{
		int ret = 0;
		if (str == null) {
			return ret;
		}
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '§') {
				ret += 2;
			}
		}

		return ret;
	}

	private String getSymbol() {
		if (this.player.isOp())
			return ChatColor.GOLD + "☆";
		if (PermissionInterface.hasPermission(this.player, "minestrike.mvp")) {
			return ChatColor.DARK_PURPLE + "❤";
		}
		return "";
	}

	/*public void doMessage(String msg) {
		for (int i = this.chat.length - 2; i >= 0; i--) {
			this.chat[(i + 1)] = this.chat[i];
		}
		this.chat[0] = msg;
		resendChat();
	}*/

	/*private void resendChat() {
		if ((this.player != null) && (this.player.isOnline())) {
			String message = "";
			int amt = 10;
			for (int c = 0; c < amt; c++) {
				message = message + "\n ";
			}
			this.player.sendMessage(message);
			String teamInfo = ChatColor.DARK_GRAY + "Team:" + getTeamColor() + this.team.toString().toLowerCase().replace("_", " ");
			String mapInfo = ChatColor.DARK_GRAY + "Map:" + ChatColor.AQUA + this.arena.getName();
			String roundInfo = ChatColor.DARK_GRAY + "Round:" + ChatColor.YELLOW + Integer.toString(this.arena.getRound()) + ChatColor.DARK_GRAY + "/" + ChatColor.YELLOW + Integer.toString(this.arena.getMaxRound());
			if (this.arena.getGameType().equals(CSArena.GameType.LOBBY)) {
				mapInfo = ChatColor.DARK_GRAY + "Next map:" + ChatColor.AQUA + this.arena.getNextMap().getName();
			}
			String mapType = ChatColor.DARK_GRAY + "Mode:" + ChatColor.AQUA + this.arena.getGameType().toString().toLowerCase().replace("_", " ");
			CSArena check = this.arena;
			if (this.arena.getGameType().equals(CSArena.GameType.LOBBY))
				check = this.arena.getNextMap();
			if (!check.getGameModeModifier().equals(CSArena.GameModeModifier.NONE)) {
				mapType = ChatColor.DARK_GRAY + "Mode:" + ChatColor.AQUA + check.getGameModeModifier().toString().toLowerCase().replace("_", " ");
			}

			String kills = ChatColor.DARK_GRAY + "Kills:" + ChatColor.GREEN + Integer.toString(this.kills) + ChatColor.DARK_GRAY + " Deaths:" + ChatColor.RED + Integer.toString(this.deaths);
			String kdr = getKDR();
			String wins = ChatColor.DARK_GRAY + "Wins:" + ChatColor.GREEN + Integer.toString(this.arena.winsCounterTerrorist);
			String losses = ChatColor.DARK_GRAY + "Losses:" + ChatColor.RED + Integer.toString(this.arena.winsTerrorist);
			if (this.team.equals(CSArena.Teams.TERRORIST)) {
				wins = ChatColor.DARK_GRAY + "Wins:" + ChatColor.GREEN + Integer.toString(this.arena.winsTerrorist);
				losses = ChatColor.DARK_GRAY + "Losses:" + ChatColor.RED + Integer.toString(this.arena.winsCounterTerrorist);
			}
			this.player.sendMessage(ChatColor.GRAY + "║ " + teamInfo + "  " + mapInfo + "  " + mapType);
			this.player.sendMessage(ChatColor.GRAY + "║ " + kills + "  " + kdr + "  " + wins + "  " + losses + "  " + roundInfo);
			this.player.sendMessage(ChatColor.GRAY + "╚═══════════════════════════════");

			if (this.specialMessage == null) {
				for (int i = this.chat.length - 1; i >= 0; i--)
					this.player.sendMessage(this.chat[i]);
			}
			else {
				this.specialMessage.draw();
				if (this.specialMessage.getTicks() > this.specialMessage.getTicks()) {
					this.specialMessage.clear();
					this.specialMessage = null;
				}
			}
		}
	}*/

	public void clearChat() {
		for (int i = 1; i <= 32; i++) {
			this.player.sendMessage("");
		}
		for (int i = this.chat.length - 1; i >= 0; i--)
			this.chat[i] = "";
	}

	public String getKDR() {
		if (deaths == 0)
			return ChatColor.DARK_GRAY + "KDR: " + ChatColor.YELLOW + Double.toString(kills);
		double ratio = kills/(double)deaths;
		ratio *= 100;
		int intrat = (int)ratio;
		if (deaths == 0)
			intrat = kills * 100;
		return ChatColor.DARK_GRAY + "KDR: " + ChatColor.YELLOW + Double.toString(intrat/100d);
	}

	public void setTeam(CSArena.Teams team) {
		if (team != null)
			this.team = team;
	}

	public void onKill(CSPlayer cskilled) {
		int amt = 300;
		if (PermissionInterface.hasPermission(getPlayer(), "minestrike.mvp"))
			amt += 100;
		this.money += amt;
		this.kills += 1;
		
		PacketUtils.displayTextBar(ChatColor.GRAY + "Killed " + ChatColor.WHITE + cskilled.getPlayer().getName() + ChatColor.GREEN + "  +$" + amt, this.player, 60);
	}

	public void onDie() {
		this.deaths += 1;
		this.teamReturnTo = this.team;
		if ((this.teamReturnTo != null) && (this.teamReturnTo.equals(CSArena.Teams.NEUTRAL)))
			this.teamReturnTo = null;
	}

	public CSArena.Teams getTeamToReturnTo() {
		return this.teamReturnTo;
	}

	public void giveMoney(int givemoney) {
		this.money += givemoney;
	}

	public void setInBuyMenu(boolean b) {
		this.isInBuyMenu = b;
	}

	public boolean isInBuyMenu() {
		return this.isInBuyMenu;
	}

	public void setMoney(int i) {
		if (i < 0)
			i = 0;
		this.money = i;
	}

	public int getMoney() {
		return this.money;
	}

	public Location getReturnLocation() {
		return this.returnTo;
	}

	public void rankUpGun() {
		this.gunRank += 1;
		if (this.gunRank < this.arena.guns.size()) {
			String str = (String)this.arena.guns.get(this.gunRank);
		} else {
			this.arena.endGame(this, getPlayer().getName() + " has won!");
		}
	}

	public void setTeamReturnTo(CSArena.Teams t) {
		this.teamReturnTo = t;
		if ((this.teamReturnTo != null) && (t.equals(CSArena.Teams.NEUTRAL)))
			this.teamReturnTo = null;
	}

	public Location getSpawnLoc() {
		return this.spawnLoc;
	}

	public boolean dropGun(ItemStack itemStack, float speed) {
		if (itemStack == null)
			return false;
		
		CSItem csitem = MCCS.getCSPlugin().getCSItemFromItemStack(itemStack);
		if (csitem != null) {
			//System.out.println("  Found gun on sever... " + csitem.getType());
			if (csitem.getType().contains("pistol") || csitem.getType().contains("rifle") || csitem.getType().contains("heavy") || csitem.getType().contains("smg")) { // only pistols/rifles can be dropped
				//System.out.println("   DROPPING EITHER PISTOL OR RIFLE");
				if ((this.getPrimary() != null && this.getPrimary().getTypeId() == itemStack.getTypeId()) || (this.getSecondary() != null && this.getSecondary().getTypeId() == itemStack.getTypeId())) {
					if (this.getPrimary() != null && this.getPrimary().getTypeId() == itemStack.getTypeId())
						this.primary = null;
					if (this.getSecondary() != null && this.getSecondary().getTypeId() == itemStack.getTypeId())
						this.secondary = null;
					int ammo = this.getAmountGunAmmo(itemStack);
					this.removeGun(itemStack);
					Item item = player.getLocation().getWorld().dropItem(player.getEyeLocation(), csitem.getItemStack());
					if (speed > -1) {
						Location ploc = player.getEyeLocation();
						double dir = -ploc.getYaw() - 90.0F;
						double pitch = -ploc.getPitch();
						double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
						double yd = Math.sin(Math.toRadians(pitch));
						double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
						Vector vec = new Vector(xd, yd, zd).multiply(speed);
						item.setVelocity(vec);
					}
					CSGun gun = new CSGun(item, ammo);
					MCCS.getCSPlugin().gunsOnGround.add(gun);
					//System.out.println("    DROPPING GUN");
					return true;
				}
			}
		}
		return false;
	}

	public void damageFromWorld(int i) {
		double current = ((Damageable)player).getHealth();
		current -= i;
		if (current < 0)
			current = 0;
		if (current > 20)
			current = 20;
		
		player.damage(0);
		player.setHealth(current);
		player.damage(0);
	}
	
	public void setKills(int kills) {
		this.kills = kills;
	}
	
	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}
}