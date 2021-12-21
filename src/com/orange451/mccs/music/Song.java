package com.orange451.mccs.music;

import java.util.ArrayList;

public class Song
{
  public ArrayList<Note> notes = new ArrayList();
  public int ticks;
  public boolean finished = false;

  public void tick() {
    int amtLeft = 0;
    for (int i = 0; i < this.notes.size(); i++) {
      ((Note)this.notes.get(i)).tick(this.ticks);
      if (!((Note)this.notes.get(i)).hasPlayed()) {
        amtLeft++;
      }
    }

    if (amtLeft == 0) {
      this.finished = true;
    }

    this.ticks += 1;
  }

  public void reset() {
    for (int i = 0; i < this.notes.size(); i++) {
      ((Note)this.notes.get(i)).restart();
    }

    this.finished = false;
    this.ticks = 0;
  }
}