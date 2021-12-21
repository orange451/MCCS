package com.orange451.mccs.music;

import java.util.ArrayList;
import org.bukkit.Sound;

public class SongStart extends Song
{
  public SongStart()
  {
    for (int i = 0; i < 4; i++) {
      float tone = 0.5F;
      float volume = 1.0F - i / 4.0F;
      int rep = i * 46;

      makeNote(rep + 1, tone, volume);
      makeNote(rep + 8, tone, volume);
      makeNote(rep + 13, tone, volume);
      makeNote(rep + 21, tone, volume);
      makeNote(rep + 29, tone, volume);
      makeNote(rep + 33, tone, volume);
      makeNote(rep + 39, tone - 0.1F, volume);
    }
  }

  public void makeNote(int tick, float pitch, float volume) {
    this.notes.add(new Note(tick, Sound.NOTE_BASS).setPitch(pitch).setVolume(volume));
    this.notes.add(new Note(tick, Sound.NOTE_PIANO).setPitch(pitch).setVolume(volume));
    this.notes.add(new Note(tick, Sound.NOTE_SNARE_DRUM).setPitch(pitch + 2.0F).setVolume(volume));
  }
}