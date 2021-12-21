package com.orange451.mccs;

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityLiving;

public class ReflectionHelper {
	public static void setVariableValue(Object o, String variable, Object value) {
		try{
			Field t = PacketPlayOutSpawnEntityLiving.class.getDeclaredField(variable);
			
			t.setAccessible(true);
			t.set(o, value);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
