package forgetools.logic;

import net.minecraft.command.ICommandSender;
import net.minecraft.world.WorldServer;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public interface WorldMobsVisitor {
    void visit(ICommandSender sender, boolean playerInWorld, MobData worldData, WorldServer s, boolean details, boolean kill, MobType type);
}
