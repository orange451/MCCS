package com.orange451.mccs;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CSSpawn
{
  private Location location;
  private CSArena.Teams team;
  private boolean canSpawnAt = true;

  public CSSpawn(Location location) {
    this.location = location;
  }

  public void setCanSpawnAt(boolean b) {
    this.canSpawnAt = b;
  }

  public void spawn(CSPlayer csPlayer) {
    csPlayer.getPlayer().teleport(this.location);
  }

  public CSArena.Teams getTeam() {
    return this.team;
  }

  public Location getLocation() {
    return this.location.clone().add(0.5D, 1.0D, 0.5D);
  }

  public boolean canSpawnAt() {
    return this.canSpawnAt;
  }
}