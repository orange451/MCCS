package com.orange451.mccs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SpecialMessage
{
  private CSPlayer player;
  private String title;
  private int ticks;
  private int maxTicks;
  public String[] lines;

  public SpecialMessage()
  {
    this.lines = new String[5];
    for (int i = 0; i < this.lines.length; i++) {
      this.lines[i] = "";
    }
    this.maxTicks = 6;
  }

  public int getTicks() {
    return this.ticks;
  }

  public CSPlayer getPlayer() {
    return this.player;
  }

  public void clear() {
    this.player = null;
    this.ticks = 0;
  }

  public void setPlayer(CSPlayer player) {
    this.player = player;
  }

  public void draw() {
    this.ticks += 1;
    String after = "";
    for (int i = 0; i < 46 - this.title.length(); i++) {
      after = after + "�?";
    }
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "╔�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?" + ChatColor.WHITE + "[" + this.title + ChatColor.WHITE + "]" + ChatColor.GRAY + after);
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "║ " + this.lines[0]);
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "║ " + this.lines[1]);
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "║ " + this.lines[2]);
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "║ " + this.lines[3]);
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "║ " + this.lines[4]);
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "║ " + this.lines[5]);
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "║ " + this.lines[6]);
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "║ " + this.lines[7]);
    getPlayer().getPlayer().sendMessage(ChatColor.GRAY + "╚�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?");
  }
}