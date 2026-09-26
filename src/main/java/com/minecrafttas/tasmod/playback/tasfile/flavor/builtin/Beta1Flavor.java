package com.minecrafttas.tasmod.playback.tasfile.flavor.builtin;

import com.minecrafttas.tasmod.playback.tasfile.flavor.SerialiserFlavorBase;

public class Beta1Flavor extends SerialiserFlavorBase {

	@Override
	public String getExtensionName() {
		return "beta1";
	}

	@Override
	public SerialiserFlavorBase clone() {
		return new Beta1Flavor();
	}
}

