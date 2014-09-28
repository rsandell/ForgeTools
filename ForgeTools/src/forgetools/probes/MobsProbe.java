package forgetools.probes;

import forgetools.Graphite;
import forgetools.GraphiteConfig;
import forgetools.logic.Functions;
import forgetools.logic.MobData;
import forgetools.logic.MobType;
import forgetools.logic.WorldMobsVisitor;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class MobsProbe extends Probe {

    public MobsProbe(GraphiteConfig.Metric metric) {
        super(metric);
    }

    @Override
    public void run() {
        MobData total = Functions.calcMobs(null, null, true, false, MobType.all, 1, new HashMap<Chunk, Integer>(), new WorldMobsVisitor() {
            @Override
            public void visit(ICommandSender sender, boolean playerInWorld, MobData worldData, WorldServer s, boolean details, boolean kill, MobType type) {
                Graphite.sendDimData(metric, String.valueOf(s.provider.dimensionId), "hostile", worldData.getAmtHos());
                Graphite.sendDimData(metric, String.valueOf(s.provider.dimensionId), "npc", worldData.getAmtNPC());
                Graphite.sendDimData(metric, String.valueOf(s.provider.dimensionId), "passive", worldData.getAmtPas());
                Graphite.sendDimData(metric, String.valueOf(s.provider.dimensionId), "total", worldData.getTotal());
            }
        });
        Graphite.sendTotalData(metric, "hostile", total.getAmtHos());
        Graphite.sendTotalData(metric, "npc", total.getAmtNPC());
        Graphite.sendTotalData(metric, "passive", total.getAmtPas());
        Graphite.sendTotalData(metric, "total", total.getTotal());
    }
}
