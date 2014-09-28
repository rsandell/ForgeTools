package net.joinedminds.mc.forgetools.probes;

import net.joinedminds.mc.forgetools.ForgeTools;
import net.joinedminds.mc.forgetools.Graphite;
import net.joinedminds.mc.forgetools.GraphiteConfig;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class PlayersProbe extends Probe {

    public PlayersProbe(GraphiteConfig.Metric metric) {
        super(metric);
    }

    @Override
    public void run() {
        int total = ForgeTools.server.getCurrentPlayerCount();
        Graphite.sendTotalData(metric, "count", total);
    }
}
