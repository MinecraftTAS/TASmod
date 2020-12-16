package de.scribble.lp.tasmod;

import de.scribble.lp.tasmod.tutorial.TutorialHandler;
import net.minecraftforge.common.config.Configuration;

public class Config {
	public static void reloadServerConfig(Configuration serverconfig) {
		
	}
	public static void reloadClientConfig(Configuration clientconfig) {
		clientconfig.load();
		TutorialHandler.istutorial=clientconfig.get("Tutorial","Enabled",true,"If the tutorial is enabled").getBoolean();
		clientconfig.save();
	}
}
