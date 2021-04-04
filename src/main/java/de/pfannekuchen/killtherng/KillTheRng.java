package de.pfannekuchen.killtherng;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;

import de.pfannekuchen.killtherng.utils.EntityRandom;
import de.pfannekuchen.killtherng.utils.WorldRandom;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

/**
 * This is the Mod for the Forge Loader
 * @author Pancake
 */
public class KillTheRng {
	
	/** Is Mod disabled */
	public static boolean ISDISABLED;
	
	/**
	 * 
	 * Do Stuff as Initialization of the Mod
	 * 
	 * @author Pancake
	 * @param event Initialization Event given by Forge
	 */	
	public static void init() {
		/* Override Math.random() and more */
		changeField("java.lang.Math$RandomNumberGeneratorHolder", "randomNumberGenerator", new EntityRandom(), true);
		changeField("net.minecraft.inventory.InventoryHelper", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "RANDOM" : "field_180177_a", new EntityRandom(), true);
		changeField("net.minecraft.tileentity.TileEntityDispenser", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "RNG" : "field_174913_f", new WorldRandom(), true);
		changeField("net.minecraft.util.math.MathHelper", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "RANDOM" : "field_188211_c", new EntityRandom(), true);
		changeField("net.minecraft.tileentity.TileEntityEnchantmentTable", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "rand" : "field_145923_r", new WorldRandom(), true);
		changeField("net.minecraft.util.datafix.fixes.ZombieProfToType", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "RANDOM" : "field_190049_a", new EntityRandom(), true);
		changeField("net.minecraft.item.Item", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "itemRand" : "field_77697_d", new EntityRandom(), false);
	}
	
	public static void changeField(String clazz, String name, Random rng, boolean isFinal) {
		try {
			Field field = Class.forName(clazz).getDeclaredField(name);			
			field.setAccessible(true);
			if (isFinal) {
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			}
			field.set(null, rng);
		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException
				| IllegalAccessException e) {
			e.printStackTrace();
			System.err.println("\n\nCouldn't hack " + clazz + ":" + name + " #7\n\n");
			FMLCommonHandler.instance().exitJava(-2, true);
		}
	}
	
}
