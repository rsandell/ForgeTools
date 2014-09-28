package forgetools.probes;

import forgetools.Graphite;
import forgetools.GraphiteConfig;
import forgetools.logic.Functions;
import forgetools.logic.LoadedChunksVisitor;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class LoadedChunksProbe extends Probe {
    public LoadedChunksProbe(GraphiteConfig.Metric metric) {
        super(metric);
    }

    @Override
    public void run() {
        int total = Functions.countLoadedChunks(null, null, true, new LoadedChunksVisitor() {
            @Override
            public void visit(ICommandSender sender, EntityPlayerMP player, boolean details, boolean playerInWorld, WorldServer s, int chunkSize) {
                Graphite.sendDimData(metric, String.valueOf(s.provider.dimensionId), "count", chunkSize);
            }
        });
        Graphite.sendTotalData(metric, "count", total);
    }
}
