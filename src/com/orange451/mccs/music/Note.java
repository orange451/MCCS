package com.orange451.mccs.music;

import com.orange451.mccs.MCCS;
import java.util.ArrayList;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Note
{
  private ArrayList<Player> listeners = new ArrayList();
  private int myTick = 0;
  private Sound sound;
  private float volume = 1.0F;
  private float pitch = 1.0F;
  private boolean played;

  public Note(int myTick, Sound sound)
  {
    this.myTick = myTick;
    this.sound = sound;
    getListeners();
  }

  private void getListeners() {
    this.listeners.clear();
    this.listeners = MCCS.getCSPlugin().getPlayers();
  }

  public Note setVolume(float f) {
    this.volume = f;
    return this;
  }

  public Note setPitch(float f) {
    this.pitch = f;
    return this;
  }

  public void tick(int ticks) {
    if (ticks == this.myTick)
      release();
  }

  public void release() {
    for (int i = 0; i < this.listeners.size(); i++) {
      if (((Player)this.listeners.get(i)).isOnline()) {
        ((Player)this.listeners.get(i)).playSound(((Player)this.listeners.get(i)).getLocation(), this.sound, this.volume, this.pitch);
      }
    }
    this.listeners.clear();
  }

  public boolean hasPlayed() {
    return this.played;
  }

  public void restart() {
    this.played = false;
    getListeners();
  }
}