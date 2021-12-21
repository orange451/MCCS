package com.orange451.mccs.music;

import java.util.ArrayList;
import org.bukkit.Sound;

public class SongBuy extends Song
{
  public SongBuy()
  {
    for (int i = 0; i < 2; i++) {
      float tone = 1.0F;
      float volume = 1.0F - i / 4.0F;
      int rep = i * 70;

      makeNote(rep, tone - 0.1F, volume);
      makeNote(rep + 4, tone - 0.1F, volume);

      makeNote(rep + 18, tone - 0.5F, volume);
      makeNote(rep + 22, tone - 0.5F, volume);

      makeNote(rep + 36, tone - 0.25F, volume);
      makeNote(rep + 40, tone - 0.25F, volume);

      makeNote(rep + 54, tone - 0.25F, volume);
      makeNote(rep + 58, tone - 0.3F, volume);
      makeNote(rep + 62, tone - 0.25F, volume);
      makeNote(rep + 66, tone - 0.2F, volume);
    }
  }

  public void makeNote(int tick, float pitch, float volume) {
    this.notes.add(new Note(tick, Sound.NOTE_BASS).setPitch(pitch).setVolume(volume));
    this.notes.add(new Note(tick, Sound.NOTE_PIANO).setPitch(pitch).setVolume(volume));
    this.notes.add(new Note(tick, Sound.NOTE_SNARE_DRUM).setPitch(pitch + 2.0F).setVolume(volume));
  }
}