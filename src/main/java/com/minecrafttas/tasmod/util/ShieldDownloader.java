package com.minecrafttas.tasmod.util;

import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventOtherPlayerJoinedClientSide;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventPlayerJoinedClientSide;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/**
 * Stub ShieldDownloader - texture downloading needs rewrite for modern API
 */
public class ShieldDownloader implements EventPlayerJoinedClientSide, EventOtherPlayerJoinedClientSide {

	private final Identifier bottleshield;
	{
		try {
			// Identifier constructor is private in 1.22+, use reflection
			java.lang.reflect.Constructor<Identifier> c = Identifier.class.getDeclaredConstructor(String.class, String.class);
			c.setAccessible(true);
			bottleshield = c.newInstance("tasmod", "textures/shields/bottleshield.png");
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Identifier", e);
		}
	}

	@Override
	public void onPlayerJoinedClientSide(LocalPlayer player) {
		// Stub
	}

	@Override
	public void onOtherPlayerJoinedClientSide(GameProfile profile) {
		// Stub
	}

	public Identifier getIdentifier(LivingEntity entitylivingbaseIn) {
		// Stub
		return bottleshield;
	}
}