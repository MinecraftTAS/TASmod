package de.scribble.lp.tasmod.savestates.server.playerloading;

import de.scribble.lp.tasmod.savestates.server.chunkloading.SavestatesChunkControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Reads the playerdata coming from the server and also applies motion,
 * relative motion and other things from the player
 * 
 * @author ScribbleLP
 *
 */
public class SavestatePlayerLoadingPacketHandler implements IMessageHandler<SavestatePlayerLoadingPacket, IMessage> {

	@Override
	public IMessage onMessage(SavestatePlayerLoadingPacket message, MessageContext ctx) {
		if (ctx.side.isClient()) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				
				EntityPlayerSP player = Minecraft.getMinecraft().player;
				NBTTagCompound compound = message.getNbtTagCompound();
				
				player.readFromNBT(compound);
				NBTTagCompound motion = compound.getCompoundTag("clientMotion");
				
				double x = motion.getDouble("x");
				double y = motion.getDouble("y");
				double z = motion.getDouble("z");
				player.motionX = x;
				player.motionY = y;
				player.motionZ = z;

				float rx = motion.getFloat("RelativeX");
				float ry = motion.getFloat("RelativeY");
				float rz = motion.getFloat("RelativeZ");
				player.moveForward = rx;
				player.moveVertical = ry;
				player.moveStrafing = rz;

				boolean sprinting = motion.getBoolean("Sprinting");
				float jumpVector = motion.getFloat("JumpFactor");
				player.setSprinting(sprinting);
				player.jumpMovementFactor = jumpVector;

				SavestatesChunkControl.keepPlayerInLoadedEntityList(player);
				SavestatePlayerLoading.wasLoading = true;
			});
		}
		return null;
	}

}
