package de.scribble.lp.tasmod.savestates.playerloading;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SavestatePlayerLoadingPacket implements IMessage{
	private double motionX=0;
	private double motionY=0;
	private double motionZ=0;
	private double posX=0;
	private double posY=0;
	private double posZ=0;
	private float rotationYaw=0;
	private float rotationPitch=0;

	public SavestatePlayerLoadingPacket() {
	}
	public SavestatePlayerLoadingPacket(NBTTagCompound nbttagcompound) {
		NBTTagList nbttaglist = nbttagcompound.getTagList("Pos", 6);
        NBTTagList nbttaglist2 = nbttagcompound.getTagList("Motion", 6);
        NBTTagList nbttaglist3 = nbttagcompound.getTagList("Rotation", 5);
        
        this.motionX = nbttaglist2.getDoubleAt(0);
        this.motionY = nbttaglist2.getDoubleAt(1);
        this.motionZ = nbttaglist2.getDoubleAt(2);
        this.posX = nbttaglist.getDoubleAt(0);
        this.posY = nbttaglist.getDoubleAt(1);
        this.posZ = nbttaglist.getDoubleAt(2);
        this.rotationYaw = nbttaglist3.getFloatAt(0);
        this.rotationPitch = nbttaglist3.getFloatAt(1);
	};
	
	@Override
	public void fromBytes(ByteBuf buf) {
		motionX=buf.readDouble();
		motionY=buf.readDouble();
		motionZ=buf.readDouble();
		posX=buf.readDouble();
		posY=buf.readDouble();
		posZ=buf.readDouble();
		rotationYaw=buf.readFloat();
		rotationPitch=buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(motionX);
		buf.writeDouble(motionY);
		buf.writeDouble(motionZ);
		buf.writeDouble(posX);
		buf.writeDouble(posY);
		buf.writeDouble(posZ);
		buf.writeFloat(rotationYaw);
		buf.writeFloat(rotationPitch);
	}
	
	public NBTTagCompound getNbtTagCompound() {
		NBTTagCompound compound= new NBTTagCompound();
		compound.setTag("Pos", this.newDoubleNBTList(this.posX, this.posY, this.posZ));
        compound.setTag("Motion", this.newDoubleNBTList(this.motionX, this.motionY, this.motionZ));
        compound.setTag("Rotation", this.newFloatNBTList(this.rotationYaw, this.rotationPitch));
        return compound;
	}
	
	private NBTTagList newDoubleNBTList(double... numbers)
    {
        NBTTagList nbttaglist = new NBTTagList();
        for (double d0 : numbers)
        {
            nbttaglist.appendTag(new NBTTagDouble(d0));
        }
        return nbttaglist;
    }

	private NBTTagList newFloatNBTList(float... numbers)
    {
        NBTTagList nbttaglist = new NBTTagList();
        for (float f : numbers)
        {
            nbttaglist.appendTag(new NBTTagFloat(f));
        }
        return nbttaglist;
    }
}
