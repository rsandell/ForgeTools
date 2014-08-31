package forgetools.logic;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

/**
* Description
*
* @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
*/
public interface WorldDropsVisitor {
    void visit(ICommandSender sender, EntityPlayerMP player, boolean playerInWorld, boolean kill, boolean killall,
               boolean details, int worldItemCount, int itemsDeleted, WorldServer s);
}
