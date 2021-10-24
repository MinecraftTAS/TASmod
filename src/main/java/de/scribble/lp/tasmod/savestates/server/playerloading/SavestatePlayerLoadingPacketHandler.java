package de.scribble.lp.tasmod.savestates.server.playerloading;

import de.pfannekuchen.tasmod.events.CameraInterpolationEvents;
import de.scribble.lp.tasmod.savestates.server.chunkloading.SavestatesChunkControl;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
				workaround(message);
			});
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	private void workaround(SavestatePlayerLoadingPacket message) {
		net.minecraft.client.entity.EntityPlayerSP player = Minecraft.getMinecraft().player;
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
		
		//TAS#86 Savestates don't change the gamemode clientside
		int gamemode=compound.getInteger("playerGameType");
		GameType type=GameType.getByID(gamemode);
		Minecraft.getMinecraft().playerController.setGameType(type);
		
		//TAS#?? Player rotation does not change when loading a savestate
		CameraInterpolationEvents.rotationPitch=player.rotationPitch;
		CameraInterpolationEvents.rotationYaw=player.rotationYaw+180f;
		
		SavestatesChunkControl.keepPlayerInLoadedEntityList(player);
		SavestatePlayerLoading.wasLoading = true;
	}
}
