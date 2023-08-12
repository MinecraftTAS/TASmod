package com.minecrafttas.tasmod.savestates.server.playerloading;

import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.savestates.server.chunkloading.SavestatesChunkControl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.GameType;

/**
 * Reads the playerdata coming from the server and also applies motion, relative
 * motion and other things from the player
 * 
 * @author Scribble
 *
 */
public class SavestatePlayerLoadingPacket implements PacketID {
	
	private NBTTagCompound compound;

	public SavestatePlayerLoadingPacket() {
	}

	public SavestatePlayerLoadingPacket(NBTTagCompound nbttagcompound) {
		compound = nbttagcompound;
	};


	@Override
	public void handle(PacketSide side, EntityPlayer playerz) {
		if(side.isClient()) {
			EntityPlayerSP player = (EntityPlayerSP)playerz;

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

			// #86
			int gamemode = compound.getInteger("playerGameType");
			GameType type = GameType.getByID(gamemode);
			Minecraft.getMinecraft().playerController.setGameType(type);

			// #?? Player rotation does not change when loading a savestate
//			CameraInterpolationEvents.rotationPitch = player.rotationPitch;
//			CameraInterpolationEvents.rotationYaw = player.rotationYaw + 180f;

			SavestatesChunkControl.keepPlayerInLoadedEntityList(player);
			SavestatePlayerLoading.wasLoading = true;
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeCompoundTag(compound);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		compound = buf.readCompoundTag();
	}

}
