package com.orange451.mccs;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftFirework;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class CSExplosion {
	private Location location;

	public CSExplosion(Location location) {
		this.location = location;
	}

	public void explode() {
		net.minecraft.server.v1_7_R4.World world = ((CraftWorld)this.location.getWorld()).getHandle();
		Firework bfirework = (Firework)this.location.getWorld().spawn(this.location, Firework.class);
		bfirework.setFireworkMeta((FireworkMeta)getFirework().getItemMeta());
		
		
		CraftFirework a = (CraftFirework)bfirework;
		world.broadcastEntityEffect(a.getHandle(), (byte)17);
		bfirework.remove();
	}

	public ItemStack getFirework() {
        Random rand = new Random();
		FireworkEffect.Type type = FireworkEffect.Type.BALL_LARGE;
		if (rand.nextInt(2) == 0)
			type = FireworkEffect.Type.BURST;
		ItemStack i = new ItemStack(Material.FIREWORK, 1);
		FireworkMeta fm = (FireworkMeta)i.getItemMeta();
		ArrayList c = new ArrayList();
		c.add(Color.RED);
		c.add(Color.RED);
		c.add(Color.RED);
		c.add(Color.ORANGE);
		c.add(Color.ORANGE);
		c.add(Color.ORANGE);
		c.add(Color.BLACK);
		c.add(Color.GRAY);
		FireworkEffect e = FireworkEffect.builder()
				.flicker(true)
				.withColor(c)
				.withFade(c)
				.with(type)
				.trail(true)
				.build();
		fm.addEffect(e);
		fm.setPower(3);
		i.setItemMeta(fm);

		return i;
	}
}