package com.orange451.mccs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class KitArenaCreator
{
  public Player player;
  public Location corner1;
  public Location corner2;
  public String arenaName;
  public String arenaType;
  public String modifier;
  public ArrayList<Location> spawns = new ArrayList();

  public KitArenaCreator(Player player, String arenaName2, String arenaType) {
    this.player = player;
    this.arenaType = arenaType;
    this.arenaName = arenaName2;
    this.modifier = "";
    player.sendMessage(ChatColor.GRAY + "STARTING TO CREATE ARENA!");
    player.sendMessage(ChatColor.GRAY + "  PLEASE SET CORNER 1 LOCATION");
    player.sendMessage(ChatColor.LIGHT_PURPLE + "    /cs setpoint");
  }

  public void setPoint() {
    Location ploc = this.player.getLocation();
    if (this.corner1 == null) {
      this.corner1 = ploc;
      this.player.sendMessage(ChatColor.GRAY + "CORNER 1 LOCATION SET");
      this.player.sendMessage(ChatColor.GRAY + "  SET CORNER 2 LOCATION");
      return;
    }
    if (this.corner2 == null) {
      this.corner2 = ploc;
      this.player.sendMessage(ChatColor.GRAY + "CORNER 2 LOCATION SET");
      this.player.sendMessage(ChatColor.GRAY + "  SET SPAWN LOCATION (for red)");
      return;
    }
  }

  public void finish() {
    saveArena();
    MCCS.getCSPlugin().stopMakingArena(this.player);
    MCCS.getCSPlugin().loadArena(this.arenaName);
  }

  private void saveArena() {
    if (this.arenaType.equals("demolition")) {
      this.arenaType = "tdm";
      this.modifier = "demolition";
    }

    if (this.arenaType.equals("arms_race")) {
      this.arenaType = "ffa";
      this.modifier = "arms_race";
    }

    if (this.arenaType.equals("hostage")) {
      this.arenaType = "tdm";
      this.modifier = "hostage_rescue";
    }

    String basePath = MCCS.getCSPlugin().getDataFolder().getAbsolutePath() + "/arenas/";
    new File(basePath).mkdirs();
    String path = basePath + this.arenaName;
    FileWriter outFile = null;
    PrintWriter out = null;
    try {
      outFile = new FileWriter(path);
      out = new PrintWriter(outFile);
      out.println(this.corner1.getBlockX() + "," + this.corner1.getBlockZ());
      out.println(this.corner2.getBlockX() + "," + this.corner2.getBlockZ());

      out.println("--config--");
      out.println("type=" + this.arenaType);
      out.println("modifier=" + this.modifier);
      if (this.spawns.size() > 0) {
        for (int i = 0; i < this.spawns.size(); i++) {
          out.println("addspawn=" + ((Location)this.spawns.get(i)).getBlockX() + "," + ((Location)this.spawns.get(i)).getBlockY() + "," + ((Location)this.spawns.get(i)).getBlockZ());
        }
      }

      System.out.println("KITARENA: " + this.arenaName + " SUCCESFULLY SAVED!");
      this.player.sendMessage(ChatColor.YELLOW + "KitArena Saved!");
    }
    catch (IOException localIOException)
    {
    }
    try {
      out.close();
      outFile.close();
    }
    catch (Exception localException) {
    }
  }

  public void addSpawn() {
    this.spawns.add(this.player.getLocation().clone());
    this.player.sendMessage("spawn added");
  }
}