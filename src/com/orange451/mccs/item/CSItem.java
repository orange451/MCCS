package com.orange451.mccs.item;

import com.orange451.mccs.CSPlayer;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class CSItem
{
	private int itemID;
	private int amount;
	private int cost;
	private String name;
	private String type;
	private boolean isSpecial = false;

	public CSItem(int itemId, int amount, int cost) {
		this.itemID = itemId;
		this.amount = amount;
		this.cost = cost;
	}

	public CSItem setSpecial(boolean b) {
		this.isSpecial = b;
		return this;
	}

	public boolean isSpecial() {
		return this.isSpecial;
	}

	public boolean canBuy(int money) {
		if (money >= this.cost)
			return true;
		return false;
	}

	public ItemStack getItemStack() {
		ItemStack ret = new ItemStack(this.itemID, 1);
		setName(ret, getDisplayName());
		return ret;
	}

	private ItemStack setName(ItemStack item, String name) {
		ItemMeta im = item.getItemMeta();
		ArrayList<String> desc = new ArrayList<String>();
		desc.add(ChatColor.GRAY + this.type.toUpperCase().replace("GUN_", ""));
		desc.add(ChatColor.GREEN + Integer.toString(this.cost));
		im.setLore(desc);
		im.setDisplayName(name);
		item.setItemMeta(im);

		return item;
	}

	public void giveItem(CSPlayer player) {
		ItemStack itm = getItemStack();
		player.getPlayer().getInventory().addItem(new ItemStack[] { itm });
	}

	public CSItem setName(String name) {
		this.name = name;
		return this;
	}

	public CSItem setType(String type) {
		this.type = type;
		return this;
	}

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

	public int getCost() {
		return this.cost;
	}

	public String getDisplayName() {
		return ChatColor.GREEN + this.name;
	}

	public int getAmount() {
		return this.amount;
	}
	
	public CSItem clone() {
		CSItem item = new CSItem(itemID, amount, cost); 
		item.setName(name);
		item.setType(type);
		item.setSpecial(isSpecial);
		
		return item;
	}

	public void setAmount(int ammo) {
		this.amount = ammo;
	}
}