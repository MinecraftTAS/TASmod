package de.pfannekuchen.killtherng.networking;

import de.pfannekuchen.killtherng.utils.EntityRandom;
import de.pfannekuchen.killtherng.utils.ItemRandom;
import de.pfannekuchen.killtherng.utils.WorldRandom;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateSeedPacketHandler implements IMessageHandler<UpdateSeedPacket, IMessage> {

	/**
	 * Stuff needed to calculate the next seed
	 * @author Pancake
	 */
    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;
	
    /**
     * Update the seed for every Keyboard Input
     * @author Pancake
     */
	@Override
	public IMessage onMessage(UpdateSeedPacket message, MessageContext ctx) {
		WorldRandom.update.set(true);
		EntityRandom.currentSeed.set((EntityRandom.currentSeed.get() * multiplier + addend) & mask);
		ItemRandom.currentSeed.set((ItemRandom.currentSeed.get() * multiplier + addend) & mask);
		return null;
	}

}
