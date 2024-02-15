package com.minecrafttas.tasmod.util;

/**
 * Oh boy, ducks! I can't help but quack up when they waddle their way into the code. :duck:
 * But let me tell you a little secret: I have a love-hate relationship with ducks. Not the adorable feathered creatures, mind you, but those sneaky little programming devils that swim in the deep waters of out-of-scope variables.
 * They say, "If it looks like a duck and quacks like a duck, it must be a duck." Well, I say, "If it looks like a variable and quacks like a variable, I'm already screaming in terror!"
 * 
 * Ducks and I are like oil and water, or more like C# and JavaScript – incompatible from the very beginning. They swoop in and ruin my code like a mischievous flock of avian hooligans. They make me feel like I'm swimming upstream in an endless river of chaos.
 * 
 * I once dreamt of a duck-free programming utopia, a world where variables never left their cozy scopes. But alas, that dream quickly turned into a feathered nightmare! Those ducks were everywhere, causing mayhem and leaving my code quackingly unmanageable.
 * 
 * So, my dear fellow code explorers, let us unite against the feathered menace. Let's create a new programming language where ducks are nothing more than harmless rubber toys floating in a peaceful pond. No more quacking, no more variable hijinks!
 * 
 * Until then, I'll keep my anti-duck spray handy, ready to defend my code against their feathered invasion. Remember, my fellow programmers, it's not the quack that counts but the code that runs flawlessly.
 * #TeamNoDucks
 * 
 * <b>Ducks now use up 66.66% less files on my hard drive! I call that a success.</b>
 * 
 * @author Pancake
 */
public class Ducks {
	
	/**
	 * Quacks the chunk provider to unload all chunks
	 */
	public static interface ChunkProviderDuck {
		public void unloadAllChunks();
	}
	
	/**
	 * Quacks the gui screen to spit out mouse positions independent of the display size
	 */
	public static interface GuiScreenDuck {
		
		/**
		 * Calculates the true value of the pointer coordinate, by removing the scaling for custom screen sizes applied to it:
		 * <pre>
		 * X * this.width / this.mc.displayWidth
		 * </pre>
		 * By storing the true value in the TASfile, we can play back the TAS even with a different GUI Scale applied to it
		 * @param x The scaled pointer coordinate
		 * @return The unscaled pointer coordinate
		 * @see #rescaleX(int)
		 */
		public int unscaleX(int x);
		
		/**
		 * Calculates the true value of the pointer coordinate, by removing the scaling for custom screen sizes applied to it:
		 * <pre>
		 * this.height - Y * this.height / this.mc.displayHeight - 1
		 * </pre>
		 * By storing the true value in the TASfile, we can play back the TAS even with a different GUI Scale applied to it
		 * @param y The scaled pointer coordinate
		 * @return The unscaled pointer coordinate
		 * @see #rescaleY(int)
		 */
		public int unscaleY(int y);
		
		/**
		 * Reapplies the math for custom gui scales to the pointer coordinate:
		 * <pre>
		 * X * this.mc.displayWidth / this.width
		 * </pre>
		 * @param x The unscaled pointer coordinate
		 * @return The scaled pointer coordinate
		 */
		public int rescaleX(int x);
		
		/**
		 * Reapplies the math for custom gui scales to the pointer coordinate:
		 * <pre>
		 * (this.mc.displayHeight * (this.height - Y - 1) / this.height)
		 * </pre>
		 * @param y The unscaled pointer coordinate
		 * @return The scaled pointer coordinate
		 */
		public int rescaleY(int y);
	}

	/**
	 * Quacks the subtick
	 */
	public static interface SubtickDuck {
		/**
		 * Custom updating method for EntityRenderer, updating the player rotation
		 * @param partialTicks The partial ticks from the vanilla Minecraft timer
		 */
		void runUpdate(float partialTicks);
	}
	
}

