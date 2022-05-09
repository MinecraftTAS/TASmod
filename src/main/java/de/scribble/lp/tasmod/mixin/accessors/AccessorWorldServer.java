package de.scribble.lp.tasmod.mixin.accessors;

import java.util.Set;
import java.util.TreeSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;

@Mixin(WorldServer.class)
public interface AccessorWorldServer {
	@Accessor("pendingTickListEntriesHashSet")
	public Set<NextTickListEntry> getTickListEntries();
	
	@Accessor("pendingTickListEntriesTreeSet")
	public TreeSet<NextTickListEntry> getTreeListEntries();
}
