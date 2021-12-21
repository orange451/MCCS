package com.orange451.mccs.item;

import com.orange451.mccs.MCCS;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class ItemLoader
{
  public ItemLoader()
  {
    String path = MCCS.getCSPlugin().getDataFolder().getAbsolutePath() + "/items";
    File dir = new File(path);
    String[] children = dir.list();
    if (children != null)
      for (int i = 0; i < children.length; i++) {
        String filename = children[i];
        loadItem(filename);
      }
  }

  public void loadItem(String filename)
  {
    String path = MCCS.getCSPlugin().getDataFolder().getAbsolutePath() + "/items/" + filename;
    boolean isSpecial = false;
    int cost = 100;
    int itemId = 300;
    int amount = 1;
    String name = "Item";
    String type = "";
    FileInputStream fstream = null;
    DataInputStream in = null;
    BufferedReader br = null;
    try {
      fstream = new FileInputStream(path);
      in = new DataInputStream(fstream);
      br = new BufferedReader(new InputStreamReader(in));

      name = br.readLine();
      type = br.readLine();
      String itm = br.readLine();
      String amt = br.readLine();
      String cst = br.readLine();
      String spc = br.readLine();

      itemId = Integer.parseInt(itm);
      amount = Integer.parseInt(amt);
      cost = Integer.parseInt(cst);
      isSpecial = Boolean.parseBoolean(spc);
    }
    catch (Exception e) {
      System.err.print("ERROR READING CS ITEM");
      e.printStackTrace();
    }try {
      br.close(); } catch (Exception localException1) {
    }try { in.close(); } catch (Exception localException2) {
    }try { fstream.close(); } catch (Exception localException3) {
    }
    CSItem item = new CSItem(itemId, amount, cost).setSpecial(isSpecial).setName(name).setType(type);
    MCCS.getCSPlugin().addItem(item);
  }
}