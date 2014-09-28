package net.joinedminds.mc.forgetools.probes;

import net.joinedminds.mc.forgetools.Graphite;
import net.joinedminds.mc.forgetools.GraphiteConfig;
import net.joinedminds.mc.forgetools.logic.Functions;
import net.joinedminds.mc.forgetools.logic.WorldDropsVisitor;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class DropsProbe extends Probe {

    public DropsProbe(GraphiteConfig.Metric metric) {
        super(metric);
    }

    @Override
    public void run() {
        int total = Functions.countDrops(null, null, new HashMap<Chunk, Integer>(), true, false, false, 1, new WorldDropsVisitor() {
            @Override
            public void visit(ICommandSender sender, EntityPlayerMP player, boolean playerInWorld, boolean kill, boolean killall, boolean details, int worldItemCount, int itemsDeleted, WorldServer s) {
                Graphite.sendDimData(metric, String.valueOf(s.provider.dimensionId), "count", worldItemCount);
            }
        });
        Graphite.sendTotalData(metric, "count", total);
    }
}
