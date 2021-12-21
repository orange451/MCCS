package com.orange451.mccs;

import org.bukkit.entity.Item;
import com.orange451.mccs.item.CSItem;

public class CSGun {
	private Item myWorldItem;
	private CSItem myCSItem;
	
	public CSGun(Item item, int ammo) {
		this.myWorldItem = item;
		this.myCSItem = MCCS.getCSPlugin().getItem(item.getItemStack()).clone();
		this.myCSItem.setAmount(ammo);
	}
	
	public boolean onPickup(CSPlayer player) {
		if (player.canGiveGun(myCSItem)) {
			myWorldItem.remove();
			player.giveGun(myCSItem.getItemStack(), myCSItem);
			return true;
		}
		return false;
	}

	public Item getWorldItem() {
		return this.myWorldItem;
	}
}
