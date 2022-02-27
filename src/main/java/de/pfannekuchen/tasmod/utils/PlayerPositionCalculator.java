package de.pfannekuchen.tasmod.utils;

import java.util.List;

import de.pfannekuchen.tasmod.events.CameraInterpolationEvents;
import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.mixin.accessors.AccessorEntity;
import de.scribble.lp.tasmod.mixin.accessors.AccessorRunStuff;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class PlayerPositionCalculator {

	public static double xNew = 0f;
	public static double yNew = 0f;
	public static double zNew = 0f;
	
	public static void calculateNextPosition(Minecraft mc, EntityPlayer source) {
		bb = source.getEntityBoundingBox();
		float f6 = 0.91F;

		float forward = ClientProxy.virtual.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) ? .98f : ClientProxy.virtual.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()) ? -.98f : 0f;
		float up = source.moveVertical;
		float strafe = ClientProxy.virtual.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) ? .98f : ClientProxy.virtual.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()) ? -.98f : 0f;
		
		double motionX = source.motionX;
		double motionY = source.motionY;
		double motionZ = source.motionZ;

		BlockPos.PooledMutableBlockPos footBlock = BlockPos.PooledMutableBlockPos.retain(source.posX,
				getEntityBoundingBox().minY - 1.0D, source.posZ);

		if (source.onGround) {
			IBlockState underState = source.world.getBlockState(footBlock);
			f6 = underState.getBlock().getSlipperiness(underState, source.world, footBlock, source) * 0.91F;
		}

		// moveRelative
		float f = strafe * strafe + up * up + forward * forward;
		if (f >= 1.0E-4F) {
			f = MathHelper.sqrt(f);
			if (f < 1.0F)
				f = 1.0F;
			f = f6 / f;
			strafe = strafe * f;
			up = up * f;
			forward = forward * f;
			float f1 = MathHelper.sin((CameraInterpolationEvents.rotationYaw + 180f) * 0.017453292F);
			float f2 = MathHelper.cos((CameraInterpolationEvents.rotationYaw + 180f) * 0.017453292F);
			motionX += (double) (strafe * f2 - forward * f1);
			motionY += (double) up;
			motionZ += (double) (forward * f2 + strafe * f1);
		}

		f6 = 0.91F;

		if (source.onGround) {
			IBlockState underState = source.world.getBlockState(
					footBlock.setPos(source.posX, source.getEntityBoundingBox().minY - 1.0D, source.posZ));
			f6 = underState.getBlock().getSlipperiness(underState, source.world, footBlock, source) * 0.91F;
		}

		// move

		double d2 = motionX;
		double d3 = motionY;
		double d4 = motionZ;

		if (source.onGround && source.isSneaking()) {
			for (; motionX != 0.0D && source.world
					.getCollisionBoxes(source,
							source.getEntityBoundingBox().offset(motionX, (double) (-source.stepHeight), 0.0D))
					.isEmpty(); d2 = motionX) {
				if (motionX < 0.05D && motionX >= -0.05D) {
					motionX = 0.0D;
				} else if (motionX > 0.0D) {
					motionX -= 0.05D;
				} else {
					motionX += 0.05D;
				}
			}

			for (; motionZ != 0.0D && source.world
					.getCollisionBoxes(source,
							source.getEntityBoundingBox().offset(0.0D, (double) (-source.stepHeight), motionZ))
					.isEmpty(); d4 = motionZ) {
				if (motionZ < 0.05D && motionZ >= -0.05D) {
					motionZ = 0.0D;
				} else if (motionZ > 0.0D) {
					motionZ -= 0.05D;
				} else {
					motionZ += 0.05D;
				}
			}

			for (; motionX != 0.0D && motionZ != 0.0D
					&& source.world.getCollisionBoxes(source,
							source.getEntityBoundingBox().offset(motionX, (double) (-source.stepHeight), motionZ))
							.isEmpty(); d4 = motionZ) {
				if (motionX < 0.05D && motionX >= -0.05D) {
					motionX = 0.0D;
				} else if (motionX > 0.0D) {
					motionX -= 0.05D;
				} else {
					motionX += 0.05D;
				}

				d2 = motionX;

				if (motionZ < 0.05D && motionZ >= -0.05D) {
					motionZ = 0.0D;
				} else if (motionZ > 0.0D) {
					motionZ -= 0.05D;
				} else {
					motionZ += 0.05D;
				}
			}
		}

		List<AxisAlignedBB> list1 = source.world.getCollisionBoxes(source,
				getEntityBoundingBox().expand(motionX, motionY, motionZ));
		AxisAlignedBB axisalignedbb = getEntityBoundingBox();

		if (motionY != 0.0D) {
			int k = 0;

			for (int l = list1.size(); k < l; ++k) {
				motionY = ((AxisAlignedBB) list1.get(k)).calculateYOffset(getEntityBoundingBox(), motionY);
			}

			setEntityBoundingBox(getEntityBoundingBox().offset(0.0D, motionY, 0.0D));
		}

		if (motionX != 0.0D) {
			int j5 = 0;

			for (int l5 = list1.size(); j5 < l5; ++j5) {
				motionX = ((AxisAlignedBB) list1.get(j5)).calculateXOffset(getEntityBoundingBox(), motionX);
			}

			if (motionX != 0.0D) {
				setEntityBoundingBox(getEntityBoundingBox().offset(motionX, 0.0D, 0.0D));
			}
		}

		if (motionZ != 0.0D) {
			int k5 = 0;

			for (int i6 = list1.size(); k5 < i6; ++k5) {
				motionZ = ((AxisAlignedBB) list1.get(k5)).calculateZOffset(getEntityBoundingBox(), motionZ);
			}

			if (motionZ != 0.0D) {
				setEntityBoundingBox(getEntityBoundingBox().offset(0.0D, 0.0D, motionZ));
			}
		}

		boolean flag = source.onGround || d3 != motionY && d3 < 0.0D;

		if (source.stepHeight > 0.0F && flag && (d2 != motionX || d4 != motionZ)) {
			double d14 = motionX;
			double d6 = motionY;
			double d7 = motionZ;
			AxisAlignedBB axisalignedbb1 = getEntityBoundingBox();
			setEntityBoundingBox(axisalignedbb);

			motionY = (double) source.stepHeight;
			List<AxisAlignedBB> list = source.world.getCollisionBoxes(source,
					getEntityBoundingBox().expand(d2, motionY, d4));
			AxisAlignedBB axisalignedbb2 = getEntityBoundingBox();
			AxisAlignedBB axisalignedbb3 = axisalignedbb2.expand(d2, 0.0D, d4);

			double d8 = motionY;
			int j1 = 0;

			for (int k1 = list.size(); j1 < k1; ++j1) {
				d8 = ((AxisAlignedBB) list.get(j1)).calculateYOffset(axisalignedbb3, d8);
			}

			axisalignedbb2 = axisalignedbb2.offset(0.0D, d8, 0.0D);
			double d18 = d2;
			int l1 = 0;

			for (int i2 = list.size(); l1 < i2; ++l1) {
				d18 = ((AxisAlignedBB) list.get(l1)).calculateXOffset(axisalignedbb2, d18);
			}

			axisalignedbb2 = axisalignedbb2.offset(d18, 0.0D, 0.0D);
			double d19 = d4;
			int j2 = 0;

			for (int k2 = list.size(); j2 < k2; ++j2) {
				d19 = ((AxisAlignedBB) list.get(j2)).calculateZOffset(axisalignedbb2, d19);
			}

			axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d19);
			AxisAlignedBB axisalignedbb4 = getEntityBoundingBox();
			double d20 = motionY;
			int l2 = 0;

			for (int i3 = list.size(); l2 < i3; ++l2) {
				d20 = ((AxisAlignedBB) list.get(l2)).calculateYOffset(axisalignedbb4, d20);
			}

			axisalignedbb4 = axisalignedbb4.offset(0.0D, d20, 0.0D);
			double d21 = d2;
			int j3 = 0;

			for (int k3 = list.size(); j3 < k3; ++j3) {
				d21 = ((AxisAlignedBB) list.get(j3)).calculateXOffset(axisalignedbb4, d21);
			}

			axisalignedbb4 = axisalignedbb4.offset(d21, 0.0D, 0.0D);
			double d22 = d4;
			int l3 = 0;

			for (int i4 = list.size(); l3 < i4; ++l3) {
				d22 = ((AxisAlignedBB) list.get(l3)).calculateZOffset(axisalignedbb4, d22);
			}

			axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d22);
			double d23 = d18 * d18 + d19 * d19;
			double d9 = d21 * d21 + d22 * d22;

			if (d23 > d9) {
				motionX = d18;
				motionZ = d19;
				motionY = -d8;
				setEntityBoundingBox(axisalignedbb2);
			} else {
				motionX = d21;
				motionZ = d22;
				motionY = -d20;
				setEntityBoundingBox(axisalignedbb4);
			}

			int j4 = 0;

			for (int k4 = list.size(); j4 < k4; ++j4) {
				motionY = ((AxisAlignedBB) list.get(j4)).calculateYOffset(getEntityBoundingBox(), motionY);
			}

			setEntityBoundingBox(getEntityBoundingBox().offset(0.0D, motionY, 0.0D));

			if (d14 * d14 + d7 * d7 >= motionX * motionX + motionZ * motionZ) {
				motionX = d14;
				motionY = d6;
				motionZ = d7;
				setEntityBoundingBox(axisalignedbb1);
			}
		}
		
        AxisAlignedBB bb2 = getEntityBoundingBox();
        double posX = (bb2.minX + bb2.maxX) / 2.0D;
        double posY = bb2.minY;
        double posZ = (bb2.minZ + bb2.maxZ) / 2.0D;
        
        int j6 = MathHelper.floor(posX);
        int i1 = MathHelper.floor(posY - 0.20000000298023224D);
        int k6 = MathHelper.floor(posZ);
        BlockPos blockpos = new BlockPos(j6, i1, k6);
        IBlockState iblockstate = source.world.getBlockState(blockpos);

        if (iblockstate.getMaterial() == Material.AIR)
        {
            BlockPos blockpos1 = blockpos.down();
            IBlockState iblockstate1 = source.world.getBlockState(blockpos1);
            Block block1 = iblockstate1.getBlock();

            if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate)
            {
                iblockstate = iblockstate1;
                blockpos = blockpos1;
            }
        }

        if (d2 != motionX)
        {
            motionX = 0.0D;
        }

        if (d4 != motionZ)
        {
            motionZ = 0.0D;
        }
		
		GlStateManager.disableAlpha();
        Vec3d vec3d;
        
        float fF = 1.62F;

        if (source.isPlayerSleeping())
        {
        	fF = 0.2F;
        }
        else if (!source.isSneaking() && source.height != 1.65F)
        {
            if (source.isElytraFlying() || source.height == 0.6F)
            {
            	fF = 0.4F;
            }
        }
        else
        {
        	fF -= 0.08F;
        }
        

        float partialTicks = ((AccessorRunStuff) Minecraft.getMinecraft()).timer().renderPartialTicks;
        if (partialTicks == 1.0F)
        {
            vec3d = new Vec3d(posX, posY + (double)fF, posZ);
        }
        else
        {
            double d02 = source.posX + (posX - source.posX) * (double)partialTicks;
            double d12 = source.posY + (posY - source.posY) * (double)partialTicks + (double)fF;
            double d22 = source.posZ + (posZ - source.posZ) * (double)partialTicks;
            vec3d = new Vec3d(d02, d12, d22);
        }
        
        Vec3d vec3d1;
        
        if (partialTicks == 1.0F)
        {
            vec3d1 = ((AccessorEntity) source).invokeGetVectorForRotation(CameraInterpolationEvents.rotationPitch, source.rotationYawHead);
        }
        else
        {
            float f2 = CameraInterpolationEvents.rotationPitch;
            float f1 = CameraInterpolationEvents.rotationYaw + 180f;
            vec3d1 = ((AccessorEntity) source).invokeGetVectorForRotation(f2, f1);
        }
        
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * mc.playerController.getBlockReachDistance(), vec3d1.y * mc.playerController.getBlockReachDistance(), vec3d1.z * mc.playerController.getBlockReachDistance());
        
        RayTraceResult result = source.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
        
        xNew = posX;
        yNew = posY;
        zNew = posZ;
        
        Minecraft.getMinecraft().renderGlobal.drawSelectionBox(source, result, 0, Minecraft.getMinecraft().getRenderPartialTicks());
		GlStateManager.enableAlpha();
	}

	private static AxisAlignedBB bb;

	private static void setEntityBoundingBox(AxisAlignedBB nbb) {
		bb = nbb;
	}

	private static AxisAlignedBB getEntityBoundingBox() {
		return bb;
	}

}
