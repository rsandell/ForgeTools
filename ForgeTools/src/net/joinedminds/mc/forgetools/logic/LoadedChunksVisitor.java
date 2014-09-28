package net.joinedminds.mc.forgetools.logic;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

/**
* Description
*
* @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
*/
public interface LoadedChunksVisitor {
    void visit(ICommandSender sender, EntityPlayerMP player, boolean details, boolean playerInWorld, WorldServer s, int chunkSize);
}
