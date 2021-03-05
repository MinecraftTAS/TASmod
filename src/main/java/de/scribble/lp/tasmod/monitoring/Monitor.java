package de.scribble.lp.tasmod.monitoring;

import java.lang.reflect.Field;

import net.minecraft.entity.player.EntityPlayer;

public class Monitor {
	
	
	private static long cooldown;
	
	public static void printFields(Object monitoredObject) {

		if (monitoredObject == null) {
			System.out.println("------------" + " null " + "------------\n");
			return;
		}

		Class monitoredClass = monitoredObject.getClass();

		Field[] fields = monitoredClass.getDeclaredFields();

		String out = "\n============= " + monitoredClass.getName() + " =============\n" + "\n";

		for (Field field : fields) {
			String name = field.getName();
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(monitoredObject);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			out = out.concat(name + ": " + value + "\n\n");
		}
		out = out.concat("------------------------");
		System.out.println(out);
	}
	
	public static boolean shouldPrint(long cooldownTime) {
		if(cooldown<=0)	{
			cooldown=cooldownTime;
			return true;
		} else {
			cooldown--;
			return false;
		}
	}
	
	public static Object accessField(Object objectToAccess, String fieldname) {
		Field field = null;
		try {
			field = objectToAccess.getClass().getDeclaredField(fieldname);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		field.setAccessible(true);
		Object out=null;
		try {
			out=field.get(objectToAccess);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return out;
	}
	
}
